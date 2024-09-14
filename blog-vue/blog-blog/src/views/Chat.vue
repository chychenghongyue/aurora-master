<template>
  <div class="linshi">
    <iframe
        id="embeddedFrame"
        height="700px"
        src="http://localhost:3000"
        width="100%"
    ></iframe>
  </div>
</template>

<script>
import { useUserStore } from '@/stores/user';
import { onMounted } from 'vue';

export default {
  setup() {
    const userStore = useUserStore();

    const onIframeLoad = () => {
      const iframe = document.getElementById('embeddedFrame');
      if (iframe && iframe.contentWindow) {
        const username = userStore.userInfo.email;
        const message = {
          username: username,
          password: '123456'
        };
        // 发送消息到子组件
        iframe.contentWindow.postMessage(message, 'http://localhost:3000');
      }
    };

    onMounted(() => {
      const iframe = document.getElementById('embeddedFrame');
      if (iframe) {
        iframe.addEventListener('load', onIframeLoad);
      }
    });
  }
}
</script>

<style scoped>
.linshi{
  background-color: #ffffff;
}
/* 适当的样式 */
</style>
