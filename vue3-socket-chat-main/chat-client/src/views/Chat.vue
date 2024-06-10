<!--
 * @Author: Mr.Yu
 * @Description: 最好的我们
 * @Date: 2022-06-27 17:02:20
 * @LastEditTime: 2022-07-01 15:31:24
-->
<template>
  <!--接收信息提示音-->
  <audio id="tip-music">
    <source src="@/assets/message/notice.mp3" type="audio/mp3"/>
  </audio>
  <div absolute right-30px top-15px>
    <el-switch
        v-model="store.openMusic"
        active-text="开提示"
        class="chat-switch-music"
        inactive-text="关提示"
        inline-prompt
    />
  </div>
  <el-row align="middle" justify="center" type="flex">
    <el-card class="lg:w-1200px w-full mt-30px chat-container" shadow="hover">
      <ChatContainer></ChatContainer>
    </el-card>
  </el-row>
</template>

<script lang="ts" setup>
import {watch} from "vue";
import {useMainStore} from "@/store/main"

const store = useMainStore()
// 监听声音开启
watch(
    () => store.openMusic,
    (newVal, oldVal) => {
      if (newVal) {
        store.tipMusic = document.getElementById("tipMusic") as HTMLMediaElement
      }
    }
)
</script>

<style lang="scss" scoped>
.chat-switch-music {
  :deep(.ep-switch__core) {
    width: 100px;
  }
}

.chat-container {
  :deep(.ep-card__body) {
    padding-left: 0 !important;
    @media (max-width: 1024px) {
      padding-right: 0 !important;
    }
  }
}
</style>
