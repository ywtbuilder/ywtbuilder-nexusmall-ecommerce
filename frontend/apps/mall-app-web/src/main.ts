import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'vant/lib/index.css'
import './styles/theme.css'
import App from './App.vue'
import router from './router/index'
import { initRum } from './utils/rum'

initRum()

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')


