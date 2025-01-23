import { defineStore } from 'pinia';
import axios from 'axios';
import { jwtDecode } from "jwt-decode";

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
    user: JSON.parse(localStorage.getItem('user')) || null,
  }),
  actions: {
    async login(credentials) {
      try {
        const response = await axios.post('/api/login', credentials);

        this.accessToken = response.data;

        const decodedToken = jwtDecode(this.accessToken);
        const username = decodedToken.sub;

        this.user = { username };

        localStorage.setItem('accessToken', this.accessToken);
        localStorage.setItem('user', JSON.stringify(this.user));

        axios.defaults.headers.common['Authorization'] = `Bearer ${this.accessToken}`;
        return true;
      } catch (error) {
        console.error('Login failed', error);
        return false;
      }
    },
    logout() {
      this.accessToken = null;
      this.refreshToken = null;
      this.user = null;

      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      delete axios.defaults.headers.common['Authorization'];
    },
    setupAxiosInterceptors() {
      axios.interceptors.response.use(
        response => response,
        async (error) => {
          const originalRequest = error.config;
          originalRequest.headers['Authorization'] = `Bearer ${this.accessToken}`;
          if (error.response && error.response.status === 401 && this.accessToken) {
            try {
              originalRequest.headers['Authorization'] = `Bearer ${this.accessToken}`;
              return axios(originalRequest);
            } catch (refreshError) {
              this.logout();
              return Promise.reject(refreshError);
            }
          }
          return Promise.reject(error);
        }
      );
    },
  },
  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isClient: (state) => state.user?.role.includes('KLIENT') || false,
    isEmployee: (state) => state.user?.role.includes('PRACOWNIK') || false,
  },
});
