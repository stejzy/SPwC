import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Toast from "vue-toastification";
import "vue-toastification/dist/index.css";
import 'bootstrap/dist/css/bootstrap.css'

import App from './App.vue'
import router from './router'

const app = createApp(App);

app.use(createPinia());
app.use(Toast);
app.use(router);

app.mount('#app');
