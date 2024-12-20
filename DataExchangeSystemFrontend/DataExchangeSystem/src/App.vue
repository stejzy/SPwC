<template>
  <div id="app">
    <BNavbar v-b-color-mode="'dark'" toggleable="lg" variant="secondary">
      <BNavbarBrand>
        <BLink to="/" class="text-white fs-4 text-decoration-none">DataExchangeSystem</BLink>
      </BNavbarBrand>

      <BNavbarToggle target="nav-collapse" />

      <BCollapse id="nav-collapse" is-nav>
        <BNavbarNav class="ms-auto mb-2 mb-lg-0">
          <BNavItemDropdown v-if="!isLoggedIn" right>
            <template #button-content>
              <em class="text-white">Log In</em>
            </template>
            <BDropdownItem to="/register">Register</BDropdownItem>
            <BDropdownItem to="/login">Login</BDropdownItem>
          </BNavItemDropdown>

          <BNavItemDropdown v-else right>
            <template #button-content>
              <em class="text-white">{{this.username}}</em>
            </template>
            <BDropdownItem @click="logout">Logout</BDropdownItem>
          </BNavItemDropdown>
        </BNavbarNav>
      </BCollapse>
    </BNavbar>

    <div class="container mt-5">
      <RouterView @login="login"></RouterView>
    </div>
  </div>
</template>

<script>
import { BNavbar, BNavbarBrand, BNavbarToggle, BCollapse, BNavbarNav, BNavItemDropdown, BDropdownItem, BLink } from 'bootstrap-vue-next';
import { RouterLink } from 'vue-router';
import {jwtDecode} from "jwt-decode";

export default {
  name: 'App',
  components: {
    BNavbar,
    BNavbarBrand,
    BNavbarToggle,
    BCollapse,
    BNavbarNav,
    BNavItemDropdown,
    BDropdownItem,
    RouterLink,
    BLink
  },
  data() {
    return {
      isLoggedIn: !!localStorage.getItem('token'),
      username: ''
    };
  },
  methods: {
    logout() {
      localStorage.removeItem('token');
      this.isLoggedIn = false;
      this.username = '';
      this.$router.push('/login');
    },
    login(token) {
      localStorage.setItem('token', token);
      const decodedToken = jwtDecode(token);
      this.username = decodedToken.sub || 'User';

      this.isLoggedIn = true;
      this.$router.push('/');
    }
  },
  watch: {
    isLoggedIn(newValue) {
      if (!newValue) {
        this.$router.push('/login');
      }
    }
  },
  mounted() {
    this.isLoggedIn = !!localStorage.getItem('token');
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        this.username = decodedToken.sub || 'User';
      } catch (error) {
        console.error('Invalid token on mount', error);
        this.username = '';
      }
    }
  }
};
</script>

<style scoped>
</style>
