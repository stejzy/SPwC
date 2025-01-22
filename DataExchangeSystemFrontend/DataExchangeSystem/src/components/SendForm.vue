<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'
import { useAuthStore } from '@/stores/auth.js'

const authStore = useAuthStore();

const toast = useToast();

const file = ref(null);

const send = async () => {
  const formData = new FormData();
  console.log(file.value);
  formData.append("file", file.value);
  formData.append('username', authStore.user.username);

  try {
    await axios.post('/api/uploadFile', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    toast.success("Pomyślnie udało się wysłać plik!")
  } catch (error) {
    if (error.status === 400) {
      toast.error(error.response.data);
    } else {
      toast.error("Wystąpił błąd przy przesyłaniu!")
    }
    console.error(error);
  }
}
</script>

<template>
  <div class="container mt-5">
    <h3>Wybierz plik do wysłania</h3>
    <form @submit.prevent="send">
      <div class="mb-3">
        <label for="fileInput" class="form-label">Wybierz plik</label>
        <input type="file" id="fileInput" class="form-control" @change="e => file = e.target.files[0]" required />
      </div>
      <button type="submit" class="btn btn-primary">Wyślij plik</button>
    </form>
  </div>
</template>

<style scoped>
.container {
  max-width: 500px;
  margin: 0 auto;
}

.form-label {
  font-size: 1.1rem;
}

.btn-primary {
  width: 100%;
  padding: 10px;
  font-size: 1.1rem;
}
</style>
