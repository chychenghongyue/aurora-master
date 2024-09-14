<template>
  <el-card class="gai-main-card">
    <div>
      <div class="article-status-menu">
        <h1>状态</h1>
        <span :class="isActive('all')" @click="changeStatus('all')">全部</span>
        <span :class="isActive('public')" @click="changeStatus('public')"> 公开 </span>
        <span :class="isActive('private')" @click="changeStatus('private')"> 私密 </span>
        <span :class="isActive('team')" @click="changeStatus('team')"> 团队 </span>
        <span :class="isActive('delete')" @click="changeStatus('delete')"> 回收站 </span>
      </div>
      <div style="height: 10px;"></div>
      <div class="operation-container">
        <el-button
            v-if="isDelete === 0"
            :disabled="articleIds.length === 0"
            class="head-button"
            icon="el-icon-delete"
            size="small"
            type="danger"
            @click="updateIsDelete = true">
          <label>批量删除&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
        </el-button>
        <el-button
            v-else
            :disabled="articleIds.length === 0"
            class="head-button"
            icon="el-icon-delete"
            size="small"
            style="margin-right: 1rem;"
            type="danger"
            @click="remove = true">
          <label>批量删除&nbsp;&nbsp;&nbsp;&nbsp;</label>
        </el-button>
        <el-button
            :disabled="articleIds.length === 0"
            class="head-button"
            icon="el-icon-download"
            size="small"
            style="margin-right: 1rem"
            type="success"
            @click="isExport = true"><label>批量导出&nbsp;&nbsp;&nbsp;&nbsp;</label></el-button>
        <el-upload
            :headers="uploadHeaders"
            :limit="9"
            :on-success="uploadArticle"
            :show-file-list="false"
            action="/api/articles/import"
            multiple
            style="margin-right: 1rem;">
          <el-button class="head-button" icon="el-icon-upload" size="small" type="primary"><label>批量导入&nbsp;&nbsp;&nbsp;&nbsp;</label>
          </el-button>
        </el-upload>
        <div style="width: 150px"></div>
        <el-select
            v-model="categoryId"
            clearable
            filterable
            placeholder="请选择分类"
            size="default"
            style="margin-right: 1rem; width: 180px;">
          <el-option label="全部" value=""/>
          <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id"/>
        </el-select>
        <el-select
            v-model="tagId"
            clearable
            filterable
            placeholder="请选择标签"
            size="default"
            style=" width: 180px;">
          <el-option label="全部" value=""/>
          <el-option v-for="item in tags" :key="item.id" :label="item.tagName" :value="item.id"/>
        </el-select>
        <div style="width: 35px"></div>
        <el-input
            v-model="keywords"
            clearable
            placeholder="请输入文章名"
            prefix-icon="el-icon-search"
            size="default"
            style="width: 180px; height: 35px; margin-right: 1rem; background-color: #ffffff !important;"
            @keyup.enter.native="searchArticles"/>
        <el-button class="small-button" icon="el-icon-search" size="small" type="primary" @click="searchArticles">
          <label>搜索&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
        </el-button>
        <el-button class="small-button" icon="el-icon-search" size="small" style="background-color: #f53c3c"
                   type="primary" @click="creat">
          <label>创建文章&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>
        </el-button>
      </div>
      <el-table :data="articles" :loading="loading" border @selection-change="selectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column :selectable="selectable" align="center" label="文章封面" prop="articleCover" width="180">
          <template #default="scope">
            <div v-if="scope">
              <el-image
                  :src="scope.row.articleCover"
                  class="article-cover"/>
              <i v-if="scope.row.status === 1" class="iconfont el-icon-mygongkai article-status-icon"/>
              <i v-if="scope.row.status === 2" class="iconfont el-icon-mymima article-status-icon"/>
              <i v-if="scope.row.status === 3" class="iconfont el-icon-mycaogaoxiang article-status-icon"/>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="标题" prop="articleTitle" width="300"></el-table-column>
        <el-table-column align="center" label="分类" prop="categoryName" width="200"/>
        <el-table-column align="center" label="标签" prop="tagDTOs" width="170">
          <template #default="scope">
            <div v-if="scope">
              <el-tag v-for="item of scope.row.tagDTOs" :key="item.tagId"
                      style="margin-right: 0.2rem; margin-top: 0.2rem">
                {{ item.tagName }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="浏览量" prop="viewsCount" width="70">
          <template #default="scope">
            <div v-if="scope">
              <span v-if="scope.row.viewsCount">
                {{ scope.row.viewsCount }}
              </span>
              <span v-else>0</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="发表时间" prop="createTime" width="200">
          <template #default="scope">
            <div v-if="scope">
              <i class="el-icon-time" style="margin-right: 5px"/>
              {{ formatDate(scope.row.createTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作" width="230">
          <template #default="scope">
            <div v-if="scope">
              <el-button v-if="scope.row.isDelete === 0" class="edit-button"
                         size="small" type="primary"
                         @click="editArticle(scope.row)">
                编辑
              </el-button>
              <el-button v-if="scope.row.isDelete === 0 && useId === scope.row.userId && scope.row.status === 3"
                         class="edit-button"
                         size="small" type="primary"
                         @click="editTeam(scope.row)">
                团队编辑
              </el-button>
              <el-popconfirm
                  v-if="scope.row.isDelete === 0 && useId === scope.row.userId"
                  cancel-button-text="取消"
                  confirm-button-text="确定"
                  popper-class="my-popconfirm"
                  style="margin-left: 10px"
                  title="确定删除吗？"
                  width="330px"
                  @confirm="updateArticleDelete(scope.row.id)">
                <template #reference style="width: 60px">
                  <el-button slot="reference" class="edit-button" size="small" type="danger"> 删除</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                  v-if="scope.row.isDelete === 1"
                  popper-class="my-popconfirm"
                  title="确定恢复吗？"
                  @confirm="updateArticleDelete(scope.row.id)">
                <template #reference>
                  <el-button slot="reference" class="edit-button" size="small" type="success"> 恢复</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm
                  v-if="scope.row.isDelete === 1"
                  popper-class="my-popconfirm"
                  style="margin-left: 10px"
                  title="确定彻底删除吗？"
                  width="200px"
                  @confirm="deleteArticles(scope.row.id)">
                <template #reference>
                  <el-button slot="reference" class="edit-button" size="small" type="danger"> 删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="updateIsDelete" width="30%">
        <div slot="title" class="dialog-title-container"><i class="el-icon-warning" style="color: #ff9900"/>提示</div>
        <div style="font-size: 1rem">是否删除选中项？</div>
        <template #footer>
          <div slot="footer">
            <el-button class="edit-button" @click="updateIsDelete = false">取 消</el-button>
            <el-button class="edit-button" type="primary" @click="updateArticleDelete(null)"> 确 定</el-button>
          </div>
        </template>
      </el-dialog>
      <el-dialog v-model="remove" width="30%">
        <div slot="title" class="dialog-title-container"><i class="el-icon-warning" style="color: #ff9900"/>提示</div>
        <div style="font-size: 1rem">是否彻底删除选中项？</div>
        <template #footer>
          <div slot="footer">
            <el-button class="edit-button" @click="remove = false">取 消</el-button>
            <el-button class="edit-button" type="primary" @click="deleteArticles(null)"> 确 定</el-button>
          </div>
        </template>
      </el-dialog>
      <el-dialog v-model="isExport" width="30%">
        <div slot="title" class="dialog-title-container"><i class="el-icon-warning" style="color: #ff9900"/>提示</div>
        <div style="font-size: 1rem">是否导出选中文章？</div>
        <div slot="footer">
          <el-button class="edit-button" @click="isExport = false">取 消</el-button>
          <el-button class="edit-button" type="primary" @click="exportArticles(null)"> 确 定</el-button>
        </div>
      </el-dialog>
      <div style="float: right;">
        <el-pagination
            :current-page="current"
            :page-size="size"
            :page-sizes="[5,10, 20]"
            :total="count"
            background
            class="pagination-container"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="sizeChange"
            @current-change="currentChange">
        </el-pagination>
      </div>
    </div>
  </el-card>
</template>


<script setup>
import {ref, onMounted, watch, toRef} from 'vue';
import axios from "axios";
import {useRouter} from "vue-router";
import {ElNotification} from 'element-plus'; // 引入通知插件
import {useUserStore} from '@/stores/user'

const router = useRouter();
const uploadHeaders = {Authorization: 'Bearer ' + sessionStorage.getItem('token')};
const loading = ref(true);
const updateIsDelete = ref(false);
const remove = ref(false);
const activeStatus = ref('all');
const articles = ref([]);
const articleIds = ref([]);
const categories = ref([]);
const tags = ref([]);
const keywords = ref(null);
const type = ref(null);
const categoryId = ref(null);
const tagId = ref(null);
const isDelete = ref(0);
const isExport = ref(false);
const status = ref(null);
const current = ref(1);
const size = ref(10);
const count = ref(0);
const useId = ref(useUserStore().userInfo.id);
onMounted(() => {
  listArticles();
  listTags();
  listCategories();
});

watch([type, categoryId, tagId, status, isDelete], () => {
  current.value = 1;
  listArticles();
});
const creat = () => {
  router.push({path: '/articleManage'});
};
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
};

const selectionChange = (articles) => {
  articleIds.value = [];
  articles.forEach((item) => {
    articleIds.value.push(item.id);
  });
};

const searchArticles = () => {
  current.value = 1;
  listArticles();
};
const selectable = (row, index) => {
  console.log('row=', row.userId, useId)
  if (row.userId !== useId.value) {
    return false
  } else {
    return true
  }
}
const editArticle = (row) => {
  const id = row.id;
  if (row.status === 3 && row.userId !== useId.value) {
    router.push({name: 'editor', query: {id}})
  } else router.push({name: 'articleManage', query: {id}})
};
const editTeam = (row) => {
  const id = row.id;
  router.push({name: 'editor', query: {id}})
};


const updateArticleDelete = (id) => {
  let param = {};
  if (id !== null) {
    param.ids = [id];
  } else {
    param.ids = articleIds.value;
  }
  param.isDelete = isDelete.value === 0 ? 1 : 0;
  axios.put('/api/articles', param).then(({data}) => {
    if (data.flag) {
      ElNotification.success({
        title: '成功',
        message: data.message
      });
      listArticles();
    } else {
      ElNotification.error({
        title: '失败',
        message: data.message
      });
    }
    updateIsDelete.value = false;
  });
};

const deleteArticles = (id) => {
  let param = {};
  if (id === null) {
    param = {data: articleIds.value};
  } else {
    param = {data: [id]};
  }
  axios.delete('/api/articles/delete',param).then(({data}) => {
    if (data.flag) {
      ElNotification.success({
        title: '成功',
        message: data.message
      });
      listArticles();
    } else {
      ElNotification.error({
        title: '失败',
        message: data.message
      });
    }
    remove.value = false;
  });
};

const exportArticles = (id) => {
  let param = {};
  if (id === null) {
    param = articleIds.value;
  } else {
    param = [id];
  }
  axios.post('/api/articles/export', param).then(({data}) => {
    if (data.flag) {
      ElNotification.success({
        title: '成功',
        message: data.message
      });
      data.data.forEach((item) => {
        downloadFile(item);
      });
      listArticles();
    } else {
      ElNotification.error({
        title: '失败',
        message: data.message
      });
    }
    isExport.value = false;
  });
};

const downloadFile = (url) => {
  const iframe = document.createElement('iframe');
  iframe.style.display = 'none';
  iframe.style.height = 0;
  iframe.src = url;
  document.body.appendChild(iframe);
  setTimeout(() => {
    iframe.remove();
  }, 5 * 60 * 1000);
};

const uploadArticle = (data) => {
  if (data.flag) {
    ElNotification.success({
      title: '成功',
      message: '导入成功'
    });
    listArticles();
  } else {
    ElNotification.error({
      title: '失败',
      message: data.message
    });
  }
};

const sizeChange = (newSize) => {
  size.value = newSize;
  current.value = 1;
  listArticles();
};

const currentChange = (currentPage) => {
  current.value = currentPage;
  listArticles();
};

const changeStatus = (newStatus) => {
  switch (newStatus) {
    case 'all':
      isDelete.value = 0;
      status.value = null;
      break;
    case 'public':
      isDelete.value = 0;
      status.value = 1;
      break;
    case 'private':
      isDelete.value = 0;
      status.value = 2;
      break;
    case 'team':
      isDelete.value = 0;
      status.value = 3;
      break;
    case 'delete':
      isDelete.value = 1;
      status.value = null;
      break;
  }
  current.value = 1;
  activeStatus.value = newStatus;
  listArticles();
};

const listArticles = () => {
  loading.value = true;
  const params = {
    keywords: keywords.value,
    type: type.value,
    categoryId: categoryId.value,
    tagId: tagId.value,
    status: status.value,
    isDelete: isDelete.value,
    current: current.value,
    size: size.value,
  };
  axios.get('/api/articlesById', {params}).then(({data}) => {
    articles.value = data.data.records;
    count.value = data.data.count;
    loading.value = false;
  }).finally(() => {
    loading.value = false;
  });
};

const listCategories = () => {
  axios.get('/api/categories/search').then(({data}) => {
    categories.value = data.data;
  });
};

const listTags = () => {
  axios.get('/api/tags/search').then(({data}) => {
    tags.value = data.data;
  });
};

const isActive = (status) => {
  return activeStatus.value === status ? "active-status" : "status";
};

</script>

<style scoped>
.operation-container {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  align-items: center; /* 垂直居中 */
  gap: 10px; /* 元素之间的间距 */
}

.article-status-menu {
  font-size: 14px;
  margin-top: 40px;
  color: #999;
}

.article-status-menu span {
  margin-right: 24px;
}

.status {
  cursor: pointer;
}

.active-status {
  cursor: pointer;
  color: #333;
  font-weight: bold;
}

.article-cover {
  position: relative;
  width: 100%;
  height: 90px;
  border-radius: 4px;
}

.article-cover::after {
  content: '';
  background: rgba(0, 0, 0, 0.3);
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
}

.article-status-icon {
  color: #fff;
  font-size: 1.5rem;
  position: absolute;
  right: 1rem;
  bottom: 1.4rem;
}

.gai-main-card {
  min-height: calc(100vh - 126px);
  position: relative;
}

.edit-button {
  width: 60px;
}

.head-button {
  width: 100px;
  height: 35px;
  text-align: center;
}

.small-button {
  width: 90px;
  height: 35px;
  text-align: center;
}
</style>
