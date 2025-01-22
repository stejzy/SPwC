<script setup>
import { ref } from 'vue';
import axios from 'axios';
import {useToast} from "vue-toastification";

const toast = useToast();

const username = ref('');
const password = ref('');
const password2 = ref('');

const usernameError = ref('');
const passwordError = ref('');

const register = async () => {
  usernameError.value = '';
  passwordError.value = '';

  const usernameRegex = /^[a-zA-Z0-9]{4,}$/;
  if (!usernameRegex.test(username.value)) {
    usernameError.value = "Nazwa użytkownika musi mieć co najmniej 4 znaki i składać się tylko z liter i cyfr.";
    return;
  }

  const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
  if (!passwordRegex.test(password.value)) {
    passwordError.value = "Hasło musi zawierać co najmniej 8 znaków w tym 1 literę, 1 cyfrę i 1 znak specjalny.";
    return;
  }

  if (password.value !== password2.value) {
    passwordError.value = "Hasła nie pasują.";
    return;
  }

  try {
    await axios.post('/api/register', {
      username: username.value,
      password: password.value,
      role: "KLIENT",
    });
    toast.success("Pomyślnie utworzono konto.");

  } catch (error) {
    if (error.response && error.response.data && error.response.data.errors) {
      usernameError.value = error.response.data.errors.username;
    } else {
      console.error("Error during registration:", error);
      toast.error("Wystąpił błąd w trakcie rejestracji.");
    }
  }
};
</script>

<template>
  <div class="container d-flex justify-content-center align-items-center mt-5">
    <div class="w-50 p-4 border rounded shadow">
      <h1 class="text-center">Rejestracja</h1>
      <form @submit.prevent="register">
        <div class="form-group mt-3">
          <label for="username">Nazwa użytkownika</label>
          <input
            type="text"
            v-model="username"
            class="form-control"
            id="username"
            aria-describedby="usernameHelp"
            placeholder="Wprowadź nazwę użytkownika"
          />
          <small v-if="usernameError" class="text-danger">{{ usernameError }}</small>
        </div>

        <div class="form-group mt-3">
          <label for="password">Hasło</label>
          <input
            type="password"
            v-model="password"
            class="form-control"
            id="password"
            placeholder="Hasło"
          />
        </div>

        <div class="form-group mt-3">
          <label for="password2">Potwierdź hasło</label>
          <input
            type="password"
            v-model="password2"
            class="form-control"
            id="password2"
            placeholder="Potwierdź hasło"
          />
          <small v-if="passwordError" class="text-danger">{{ passwordError }}</small>
        </div>

        <button type="submit" class="btn btn-primary w-100 mt-3">Zarejestruj się</button>
      </form>
    </div>
  </div>
</template>
