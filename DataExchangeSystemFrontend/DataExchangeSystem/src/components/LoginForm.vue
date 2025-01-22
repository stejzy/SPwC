<script setup>
import { ref } from "vue";
import { useAuthStore } from "@/stores/auth.js";
import router from "@/router/index.js";

const username = ref("");
const password = ref("");
const error = ref("");

const authStore = useAuthStore();

const login = async () => {
  const success = await authStore.login({ username: username.value, password: password.value });

  if (success) {
    await router.push("/");
  } else {
    error.value = "Nie udało się zalogować. Sprawdź dane logowania.";
  }
};
</script>

<template>
  <div class="container d-flex justify-content-center align-items-center mt-5">
    <div class="w-50 p-4 border rounded shadow">
      <h1 class="text-center">Zaloguj się</h1>

      <div v-if="error" class="alert alert-danger text-center" role="alert">
        {{ error }}
      </div>

      <form @submit.prevent="login">
        <div class="form-group mt-5">
          <label for="username">Nazwa użytkownika</label>
          <input
            type="text"
            v-model="username"
            class="form-control"
            id="username"
            placeholder="Wpisz nazwę użytkownika"
          />
        </div>
        <div class="form-group mt-3">
          <label for="password">Hasło</label>
          <input
            type="password"
            v-model="password"
            class="form-control"
            id="password"
            placeholder="Wpisz hasło"
          />
        </div>
        <button type="submit" class="btn btn-primary w-100 mt-3">Zaloguj się</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.alert {
  margin-bottom: 20px;
}
</style>
