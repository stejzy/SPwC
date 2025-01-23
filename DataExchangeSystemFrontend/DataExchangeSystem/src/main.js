import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Toast from "vue-toastification";
import "vue-toastification/dist/index.css";
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import 'primeicons/primeicons.css'

import App from './App.vue'
import router from './router'

const app = createApp(App);

app.use(createPinia());
app.use(Toast);
app.use(router);

app.mount('#app');

import { useAuthStore } from './stores/auth';
const authStore = useAuthStore();
authStore.setupAxiosInterceptors();
