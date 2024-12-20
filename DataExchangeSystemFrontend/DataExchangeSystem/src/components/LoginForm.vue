<template>
  <BForm @submit.prevent="LoginSubmit">
    <BFormGroup label="Username" label-for="username">
      <BFormInput
        id="username"
        v-model="username"
        type="text"
        required
        placeholder="Enter your username"
      />
    </BFormGroup>

    <BFormGroup label="Password" label-for="password">
      <BFormInput
        id="password"
        v-model="password"
        type="password"
        required
        placeholder="Enter your password"
      />
    </BFormGroup>

    <BButton type="submit" variant="primary" class="w-100">
      Log In
    </BButton>
  </BForm>
</template>

<script>
import {BButton, BForm, BFormGroup, BFormInput} from 'bootstrap-vue-next';
import axios from "axios";

export default {
  name: 'LoginForm',
  components: {
    BForm,
    BFormGroup,
    BFormInput,
    BButton
  },
  data() {
    return {
      username: '',
      password: ''
    };
  },
  methods: {
    async LoginSubmit() {
      try {
        const token = await this.authenticate(this.username, this.password);
        if (token) {
          this.$emit('login', token);
        }
      } catch (e) {
        console.error('Login failed', e);
      }
    },
    async authenticate(username, password) {
      const data = {
        username: username,
        password: password
      }

      const response = await axios.post('http://localhost:8080/api/login', data)

      return response.data;
    }
  }
};
</script>

<style scoped>
</style>
