<template>
  <el-card class="main-card">
    <div class="title">{{ '文章编辑' }}</div>
    <div class="article-title-container">
      <el-input v-model="article.articleTitle" placeholder="输入文章标题" size="default"/>
      <el-button
          v-if="article.id === null || article.status === 3"
          class="save-btn"
          size="default"
          type="danger"
          @click="teamButton"
      >
        团队编辑
      </el-button>
      <el-button size="default" style="margin-left: 10px" type="danger" @click="openModel"> 发布文章</el-button>
    </div>
    <mavon-editor ref="md" v-model="article.articleContent" style="height: calc(100vh - 260px)" @imgAdd="uploadImg">
    </mavon-editor>
    <div>
      <el-dialog v-model="teamFlag" modal show-close style="height: 550px;" top="20vh" width="35%">
        <div style="float: left;">
          <el-button
              :disabled="user.length === 0"
              class="head-button"
              icon="el-icon-delete"
              size="small"
              style="width: 100px; height: 40px"
              type="primary"
              @click="enter">
            <label>确定&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
          </el-button>
        </div>
        <div>
          <el-table :data="userList" :loading="teamFlagTable" border @selection-change="selectionChange">
            <el-table-column type="selection" width="55"/>
            <el-table-column align="center" label="头像" prop="avatar" width="80">
              <template #default="scope">
                <div v-if="scope">
                  <el-image
                      :src="scope.row.avatar"
                      class="article-cover"/>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="用户名" prop="email"/>
          </el-table>
          <div>
            <el-pagination
                :current-page="current"
                :page-size="size"
                :page-sizes="[5]"
                :total="count"
                background
                class="pagination-container"
                layout="total, sizes, prev, pager, next, jumper"
                style="margin-top: 5px;"
                @size-change="sizeChange"
                @current-change="currentChange">
            </el-pagination>
          </div>
        </div>
      </el-dialog>
    </div>
    <el-dialog v-model="addOrEdit" modal show-close top="20vh" width="40%">
      <el-form :model="article" label-width="80px" size="default">
        <el-form-item label="文章分类">
          <el-tag
              v-show="article.categoryName"
              :closable="true"
              style="margin: 0 1rem 0 0"
              type="success"
              @close="removeCategory">
            {{ article.categoryName }}
          </el-tag>
          <el-popover v-if="!article.categoryName" placement="bottom-start" trigger="click" width="460">
            <div class="popover-title">分类</div>
            <el-autocomplete
                v-model="categoryName"
                :fetch-suggestions="searchCategories"
                :trigger-on-focus="false"
                placeholder="请输入分类名搜索，enter可添加自定义分类"
                style="width: 100%"
                @select="handleSelectCategories"
                @keyup.enter="saveCategory">
              <template #default="{ item }">
                <div>{{ item.categoryName }}</div>
              </template>
            </el-autocomplete>
            <div class="popover-container">
              <div v-for="item of categorys" :key="item.id" class="category-item" @click="addCategory(item)">
                {{ item.categoryName }}
              </div>
            </div>
            <template #reference>
              <el-button plain size="small" type="success"> 添加分类</el-button>
            </template>
          </el-popover>
        </el-form-item>
        <el-form-item label="文章标签">
          <el-tag
              v-for="(item, index) in article.tagNames"
              :key="index"
              :closable="true"
              style="margin: 0 1rem 0 0"
              @close="removeTag(item)"
          >
            {{ item }}
          </el-tag>
          <el-popover v-if="article.tagNames.length <= 3" placement="bottom-start" trigger="click" width="460">
            <template #reference>
              <el-button plain size="small" type="primary"> 添加标签</el-button>
            </template>
            <div class="popover-title">标签</div>
            <el-autocomplete
                v-model="tagName"
                :fetch-suggestions="searchTags"
                :trigger-on-focus="false"
                placeholder="请输入标签名搜索，enter可添加自定义标签"
                style="width: 100%"
                @select="handleSelectTag"
                @keyup.enter="saveTag"
            >
              <template #default="{ item }">
                <div>{{ item.tagName }}</div>
              </template>
            </el-autocomplete>
            <div class="popover-container">
              <div style="margin-bottom: 1rem">添加标签</div>
              <el-tag v-for="(item, index) in tagList" :key="index" :class="tagClass(item)" @click="addTag(item)">
                {{ item.tagName }}
              </el-tag>
            </div>
          </el-popover>
        </el-form-item>
        <!--          <el-form-item label="文章类型">-->
        <!--            <el-select v-model="article.type" placeholder="请选择类型">-->
        <!--              <el-option v-for="item in typeList" :key="item.type" :label="item.desc" :value="item.type"/>-->
        <!--            </el-select>-->
        <!--          </el-form-item>-->
        <!--          <el-form-item v-if="article.type !== 1" label="原文地址">-->
        <!--            <el-input v-model="article.originalUrl" placeholder="请填写原文链接"/>-->
        <!--          </el-form-item>-->
        <el-form-item label="上传封面">
          <el-upload
              :before-upload="beforeUpload"
              :headers="headers"
              :on-success="uploadCover"
              action="/api/articles/images"
              class="upload-cover"
              drag
              multiple>
            <template v-if="article.articleCover === ''">
              <i class="el-icon-upload"/>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            </template>
            <el-image v-else :src="article.articleCover" style=" height:auto ;width:auto "/>
          </el-upload>
        </el-form-item>
        <!--          <el-form-item label="置顶">-->
        <!--            <el-switch-->
        <!--                v-model="article.isTop"-->
        <!--                :active-value="1"-->
        <!--                :inactive-value="0"-->
        <!--                active-color="#13ce66"-->
        <!--                inactive-color="#F4F4F5"-->
        <!--            />-->
        <!--          </el-form-item>-->
        <!--          <el-form-item label="推荐">-->
        <!--            <el-switch-->
        <!--                v-model="article.isFeatured"-->
        <!--                :active-value="1"-->
        <!--                :inactive-value="0"-->
        <!--                active-color="#13ce66"-->
        <!--                inactive-color="#F4F4F5"-->
        <!--            />-->
        <!--          </el-form-item>-->
        <el-form-item label="发布形式">
          <el-radio-group v-model="article.status">
            <el-radio :label="1">公开</el-radio>
            <el-radio :label="2">密码</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="article.status === 2" label="访问密码">
          <el-input v-model="article.password" placeholder="请填写文章访问密码"/>
        </el-form-item>
        <el-form-item label="文章摘要">
          <el-input v-model="article.articleAbstract" autosize="true" placeholder="默认取文章前500个字符"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div>
          <el-button class="el-button" @click="addOrEdit = false">取 消</el-button>
          <el-button class="el-button" type="danger" @click="saveOrUpdateArticle"> 发 表</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import {ref, onMounted, onUnmounted, getCurrentInstance, reactive} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {ElMessage, ElNotification} from 'element-plus';
import * as imageConversion from 'image-conversion';
import axios from 'axios';
import {useUserStore} from '@/stores/user'

const route = useRoute();
const router = useRouter();
const md=ref(null)
onMounted(() => {
  const articleId = route.query.id;
  console.log("传参")
  console.log(articleId)
  if (articleId) {
    axios.get('/api/articles/tiaozhuan/' + articleId).then(({data}) => {
      console.log(data.data)
      article.value = data.data;
    });
  } else {
    const articleStored = sessionStorage.getItem('article');
    if (articleStored) {
      article.value = JSON.parse(articleStored);
    }
  }
  searchUsers()
});
const teamFlag = ref(false);
const teamFlagTable = ref(false);
const users = ref([]);
const userList = ref([]);
const addOrEdit = ref(false);
const autoSave = ref(true);
const categoryName = ref('');
const tagName = ref('');
const categorys = ref([]);
const tagList = ref([]);
const current = ref(1);
const size = ref(5);
const count = ref();
const temp = ref(useUserStore().userInfo.id);
const user = ref({
  id: null,
  email: '',
  avatar: ''
})
const article = ref({
  id: null,
  userId: null,
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
const headers = ref({Authorization: 'Bearer ' + sessionStorage.getItem('token')});
const enter = () => {
  console.log("list" + users.value)
  axios.post('/api/team/insert', {
    creatId: temp.value,
    userId: users.value,
    articleId: article.value.id
  }).then(({data}) => {
    if (data.flag) {
      router.push({path: '/articleListManage'});
    }
  })
}
const selectionChange = (userList) => {
  users.value = [];
  userList.forEach((item) => {
    users.value.push(item.id);
  });
};
const sizeChange = (newSize) => {
  size.value = newSize;
  current.value = 1;
  searchUsers();
};

const currentChange = (currentPage) => {
  current.value = currentPage;
  searchUsers();
};
const teamButton = () => {
  if (article.value.articleTitle.trim() === '') {
    ElMessage.error('文章标题不能为空');
    return false;
  }
  if (article.value.articleContent.trim() === '') {
    ElMessage.error('文章内容不能为空');
    return false;
  }
  teamFlag.value = true;
  article.value.status = 3;
  article.value.userId = temp;
  console.log("用户：" + temp)
  console.log("用户1：" + temp.value)
  axios.post('/api/articles', article.value).then(({data}) => {
    if (data.flag) {
      sessionStorage.removeItem('article');
      ElNotification.success({title: '成功', message: '保存成功'});
      article.value.id = data.data.id;
    } else {
      ElNotification.error({title: '失败', message: '保存失败'});
    }
  });
  autoSave.value = true;
};

const saveOrUpdateArticle = () => {
  if (article.value.articleTitle.trim() === '') {
    ElMessage.error('文章标题不能为空');
    return false;
  }
  if (article.value.articleContent.trim() === '') {
    ElMessage.error('文章内容不能为空');
    return false;
  }
  if (article.value.categoryName === null) {
    ElMessage.error('文章分类不能为空');
    return false;
  }
  if (article.value.tagNames.length === 0) {
    ElMessage.error('文章标签不能为空');
    return false;
  }
  if (article.value.articleCover.trim() === '') {
    ElMessage.error('文章封面不能为空');
    return false;
  }
  article.value.userId = temp.value;
  axios.post('/api/articles', article.value).then(({data}) => {
    if (data.flag) {
      article.value.id = data.data.id;
      sessionStorage.removeItem('article');
      router.push({path: '/articleListManage'});
      ElNotification.success({title: '成功', message: data.message});
    } else {
      ElNotification.error({title: '失败', message: data.message});
    }
    addOrEdit.value = false;
  });
  // autoSave.value = false;
};

const autoSaveArticle = () => {
  if (
      autoSave.value &&
      article.value.articleTitle.trim() !== '' &&
      article.value.articleContent.trim() !== ''
  ) {
    axios.post('/api/articles', article.value).then(({data}) => {
      console.log(article.value)
      if (data.flag) {
        ElNotification.success({title: '成功', message: '自动保存成功'});
      } else {
        ElNotification.error({title: '失败', message: '自动保存失败'});
      }
    });
  }
  if (autoSave.value && article.value.id === null) {
    sessionStorage.setItem('article', JSON.stringify(article.value));
  }
};

const openModel = () => {
  if (article.value.articleTitle.trim() === '') {
    ElMessage.error('文章标题不能为空');
    return false;
  }
  if (article.value.articleContent.trim() === '') {
    ElMessage.error('文章内容不能为空');
    return false;
  }
  listCategories();
  listTags();
  addOrEdit.value = true;
};

const uploadCover = (response) => {
  article.value.articleCover = response.data;
};

const beforeUpload = (file) => {
  return new Promise((resolve) => {
    if (file.size / 1024 < 200) {
      resolve(file);
    }
    imageConversion.compressAccurately(file, 200).then((res) => {
      resolve(res);
    });
  });
};

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


const listCategories = () => {
  axios.get('/api/categories/search').then(({data}) => {
    categorys.value = data.data;
  });
};

const listTags = () => {
  axios.get('/api/tags/search').then(({data}) => {
    tagList.value = data.data;
  });
};

const searchCategories = (keywords, cb) => {
  axios
      .get('/api/categories/search', {params: {keywords}})
      .then(({data}) => {
        cb(data.data);
      });
};

const handleSelectCategories = (item) => {
  addCategory({categoryName: item.categoryName});
};

const saveCategory = () => {
  if (categoryName.value.trim() !== '') {
    addCategory({categoryName: categoryName.value});
    categoryName.value = '';
  }
};

const addCategory = (item) => {
  article.value.categoryName = item.categoryName;
};

const removeCategory = () => {
  article.value.categoryName = null;
};

const searchTags = (keywords, cb) => {
  axios
      .get('/api/tags/search', {params: {keywords}})
      .then(({data}) => {
        cb(data.data);
      });
};
const searchUsers = () => {
  axios
      .get('/api/userAll', {
        params: {
          current: current.value,
          size: size.value
        }
      })
      .then(({data}) => {
        userList.value = data.data.records;
        count.value = data.data.count;
        console.log(data.data)
        teamFlagTable.value = false;
      });
};

const handleSelectTag = (item) => {
  addTag({tagName: item.tagName});
};


const saveTag = () => {
  if (tagName.value.trim() !== '') {
    addTag({tagName: tagName.value});
    tagName.value = '';
  }
};

const addTag = (item) => {
  if (!article.value.tagNames.includes(item.tagName)) {
    article.value.tagNames.push(item.tagName);
  }
};

const removeTag = (item) => {
  const index = article.value.tagNames.indexOf(item);
  article.value.tagNames.splice(index, 1);
};

const tagClass = (item) => {
  const index = article.value.tagNames.indexOf(item.tagName)
  return index !== -1 ? 'tag-item-select' : 'tag-item'
};


onUnmounted(() => {
  autoSaveArticle();
});
</script>

<style scoped>
.article-title-container {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
  margin-top: 2.25rem;
}

.save-btn {
  margin-left: 0.75rem;
  background: #fff;
  color: #f56c6c;
}

.tag-item {
  margin-right: 1rem;
  margin-bottom: 1rem;
  cursor: pointer;
}

.tag-item-select {
  margin-right: 1rem;
  margin-bottom: 1rem;
  cursor: not-allowed;
  color: #ccccd8 !important;
}

.category-item {
  cursor: pointer;
  padding: 0.6rem 0.5rem;
}

.category-item:hover {
  background-color: #f0f9eb;
  color: #67c23a;
}

.popover-title {
  margin-bottom: 1rem;
  text-align: center;
}

.popover-container {
  margin-top: 1rem;
  height: 260px;
  overflow-y: auto;
}
</style>
