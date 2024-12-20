<template>
  <div class="d-flex justify-content-center align-items-center">
    <div class="card p-4 shadow-lg" style="max-width: 400px; width: 100%;">
      <h3 class="text-center mb-4">Log In</h3>
      <LoginForm @login="login"/>
    </div>
  </div>
</template>

<script>
import LoginForm from '../components/LoginForm.vue';

export default {
  name: 'LoginView',
  components: {
    LoginForm
  },
  methods: {
    login(token) {
      this.$emit('login', token);
    }
  }
};
</script>

<style scoped>
</style>
