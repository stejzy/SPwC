<template>
  <div class="d-flex justify-content-center align-items-center">
    <div class="card p-4 shadow-lg" style="max-width: 400px; width: 100%;">
      <h3 class="text-center mb-4">Register</h3>
      <RegisterForm />
    </div>
  </div>
</template>

<script>
import RegisterForm from '@/components/RegisterForm.vue';
import LoginForm from "@/components/LoginForm.vue";

export default {
  name: 'RegisterView',
  components: {
    LoginForm,
    RegisterForm
  }
};
</script>
