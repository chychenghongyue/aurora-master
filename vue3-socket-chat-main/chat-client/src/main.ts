import {createApp} from "vue"
import App from "./App.vue"
import router from "./router"
import store from './store'

import "@/styles/index.scss"
import 'uno.css'
import 'nprogress/nprogress.css'

// If you want to use ElMessage, import it.
import "element-plus/theme-chalk/src/message.scss"
import "element-plus/theme-chalk/src/notification.scss"
import {login} from "@/api/user";
import {useMainStore} from "@/store/main";

const app = createApp(App)
// app.use(ElementPlus)
app.use(router).use(store).mount("#app")
const form = {
    username: '',
    password: ''
};
const mainStore = useMainStore()
// 监听来自父页面的消息
window.addEventListener('message', (event) => {
    console.log('接收到消息',event.data)
    if (event.data.action === 'callLocalMethod') {
        const data = event.data.data; // 接收的数据
        console.log(data);
        form.username = data;
        form.password = '123456'
        login(form).then((res: any) => {
            mainStore.setToken(res?.data)
            router.push({path: '/chat'})
        });
    }
});

