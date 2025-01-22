<script setup>
import {RouterLink, useRoute} from "vue-router";
import {useAuthStore} from "@/stores/auth.js";
import {computed} from "vue";
import router from '@/router/index.js'

const isActiveLink = (routePath) => {
  const route = useRoute();
  return route.path === routePath;
}

const authStore = useAuthStore();

const isLoggedIn = computed(() => {
  return authStore.isAuthenticated;
});

const logout = () => {
  authStore.logout();
  router.push('/login');
}
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid">
      <RouterLink class="navbar-brand" to="/">DataExchangeSystem</RouterLink>
      <div class="d-flex align-items-center">
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto">
          <li class="nav-item">
            <RouterLink
              class="nav-link"
              :class="{ active: isActiveLink('/files') }"
              to="/files"
            >
              Pliki
            </RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink
              class="nav-link"
              :class="{ active: isActiveLink('/send') }"
              to="/send"
            >
              Prześlij plik
            </RouterLink>
          </li>
        </ul>
        <ul class="navbar-nav">
          <template v-if="!isLoggedIn">
            <li class="nav-item">
              <RouterLink
                class="nav-link"
                :class="{ active: isActiveLink('/login') }"
                to="/login"
              >
                Zaloguj
              </RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink
                class="nav-link"
                :class="{ active: isActiveLink('/register') }"
                to="/register"
              >
                Zarejestruj
              </RouterLink>
            </li>
          </template>
          <template v-else>
            <li class="nav-item">
              <span class="nav-link">Witaj, {{authStore.user.username}}</span>
            </li>
            <li class="nav-item">
              <button
                @click="logout"
                class="btn btn-danger"
              >
                Wyloguj
              </button>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>
