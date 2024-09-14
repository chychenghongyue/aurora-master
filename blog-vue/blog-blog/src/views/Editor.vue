<template>
  <el-card v-if="article" class="main-card">
    <div class="avatar-container">
<!--      <div v-for="(avatar, index) in avatars" :key="index" class="avatar-item">-->
<!--        <el-image :src="avatar" alt="User Avatar" class="avatar-image"/>-->
<!--      </div>-->
    </div>
    <div class="article-title-container">
      <mavon-editor ref="md" v-model="article.articleContent" style="height: 80vh;top: 30px" @imgAdd="uploadImg">
      </mavon-editor>
    </div>
  </el-card>
</template>

<script setup>
import {ref, onMounted, watch, reactive, onUnmounted} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import * as imageConversion from 'image-conversion';
import axios from 'axios';
import {useUserStore} from '@/stores/user';
import {ElNotification} from "element-plus";

const avatars = ref([])
const route = useRoute();
const router = useRouter();
const article = reactive({
  id: null,
  articleTitle: '',
  articleContent: '',
  articleAbstract: '',
  articleCover: '',
  categoryName: null,
  tagNames: [],
  isTop: 0,
  type: 1,
  status: '',
});
const autoSave = ref(true);
const articleId = ref(143);
const userId = ref(useUserStore().userInfo.id);
const token = ref(localStorage.getItem('token'));
const md = ref(null)

onMounted(async () => {
  await getArticle();
  initWebSocket();
  startAutoSaveTimer();
});
onUnmounted(() => {
  autoSaveArticle();
});

const HEARTBEAT_INTERVAL = 30000;
const MAX_RECONNECT_ATTEMPTS = 5;
let socket = null;
let heartbeatTimer = null;
let reconnectAttempts = 0;

function initWebSocket() {
  socket = new WebSocket('ws://localhost:8888/websocket/' + articleId.value + '/' + userId.value);

  socket.onopen = () => {
    console.log('WebSocket 连接已建立');
    startHeartbeat();
    reconnectAttempts = 0;
  };

  socket.onclose = () => {
    console.log('WebSocket 连接关闭');
    clearInterval(heartbeatTimer);
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      reconnectAttempts++;
      setTimeout(initWebSocket, 5000);
    } else {
      console.log('已达到最大重连次数，停止重连');
    }
  };

  socket.onerror = (error) => {
    console.error('WebSocket error:', error);
    socket.close();
  };

  socket.onmessage = (event) => {
    const info = JSON.parse(event.data);
    console.log('WebSocket收到消息', event.data);
    if (info.type === 'ping') {
      console.log("心跳重置");
      clearInterval(heartbeatTimer);
    }
    if (info.type === 'updateContent') {
      article.articleContent = info.message;
    }
    if (info.type === 'avatar') {
      console.log("收到用户头像", info.message);
      avatars.value = JSON.parse(info.message);
    }
  };
}

function startHeartbeat() {
  heartbeatTimer = setInterval(() => {
    if (socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({type: 'ping', articleId: articleId.value, userId: userId.value, message: ''}));
      console.warn('发送心跳');
    } else {
      console.warn('WebSocket未打开，无法发送心跳');
    }
  }, HEARTBEAT_INTERVAL);
}

const getArticle = () => {
  const articleId = route.query.id;
  console.log("传参", articleId);
  if (articleId) {
    axios.get('/api/articles/tiaozhuan/' + articleId).then(({data}) => {
      console.log(data.data);
      Object.assign(article, data.data);
    });
  } else {
    const articleStored = sessionStorage.getItem('article');
    if (articleStored) {
      Object.assign(article, JSON.parse(articleStored));
    }
  }
};

watch(() => article.articleContent, (newVal, oldVal) => {
  console.log(`Message changed from "${oldVal}" to "${newVal}"`);
  if (socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify({
      type: 'updateContent',
      articleId: articleId.value,
      userId: userId.value,
      message: newVal
    }));
  }
});

const uploadImg = (pos, file) => {
  const formData = new FormData()
  if (file.size / 1024 < 200) {
    formData.append('file', file)
    axios.post('/api/articles/images', formData).then(({data}) => {
      md.value.$img2Url(pos, data.data)
    })
  } else {
    imageConversion.compressAccurately(file, 200).then((res) => {
      formData.append('file', new window.File([res], file.name, {type: file.type}))
      axios.post('/api/articles/images', formData).then(({data}) => {
        md.value.$img2Url(pos, data.data)
      })
    })
  }
}

const autoSaveArticle = () => {
  if (
      autoSave.value &&
      article.articleTitle.trim() !== '' &&
      article.articleContent.trim() !== '' &&
      article.id !== null
  ) {
    article.status = 3;
    axios.post('/api/articles', article).then(({data}) => {
      console.log("文章自动更新")
      console.log(article)
      if (data.flag) {
        ElNotification.success({title: '成功', message: '自动保存成功'});
      } else {
        ElNotification.error({title: '失败', message: '自动保存失败'});
      }
    });
  } else if (autoSave.value && article.id === null) {
    sessionStorage.setItem('article', JSON.stringify(article));
  }
};

const startAutoSaveTimer = () => {
  setInterval(() => {
    autoSaveArticle();
  }, 60000); // 每分钟自动保存一次
};
</script>
<style scoped>
.avatar-container {
  margin-bottom: 20px;
}

.avatar-list {
  display: flex;
}

.avatar-item {
  margin-right: 10px;
}

.avatar-image {
  width: 20px;
  height: 20px;
  border-radius: 50%; /* Make the avatar circular */
}
</style>
