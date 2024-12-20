<template>
  <BCard class="p-4" style="max-width: 500px; margin: 0 auto;">
    <BForm @submit.prevent="onSubmit">

      <BFormGroup label="Username" label-for="username">
        <BFormInput
          id="username"
          v-model="form.username"
          type="text"
          required
          placeholder="Enter your username"
        ></BFormInput>
      </BFormGroup>

      <BFormGroup label="Password" label-for="password">
        <BFormInput
          id="password"
          v-model="form.password"
          type="password"
          required
          placeholder="Enter your password"
        ></BFormInput>
      </BFormGroup>

      <BFormGroup label="Confirm Password" label-for="confirm-password">
        <BFormInput
          id="confirm-password"
          v-model="form.confirmPassword"
          type="password"
          required
          placeholder="Confirm your password"
        ></BFormInput>
      </BFormGroup>

      <BButton type="submit" variant="primary" block>Register</BButton>
    </BForm>
  </BCard>
</template>

<script>
import { BCard, BForm, BFormGroup, BFormInput, BButton } from 'bootstrap-vue-next';

export default {
  name: 'RegisterForm',
  components: {
    BCard,
    BForm,
    BFormGroup,
    BFormInput,
    BButton
  },
  data() {
    return {
      form: {
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: ''
      }
    };
  },
  methods: {
    onSubmit() {
      if (this.form.password !== this.form.confirmPassword) {
        alert('Passwords do not match!');
        return;
      }
      //zrobic rejestracje tutaj
      alert('Form submitted successfully!');
    }
  }
}
</script>
