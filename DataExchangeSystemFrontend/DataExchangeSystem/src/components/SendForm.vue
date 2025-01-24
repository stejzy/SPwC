<script setup>
import { onBeforeUnmount, ref } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'
import { useAuthStore } from '@/stores/auth.js'


const authStore = useAuthStore();
const toast = useToast();
const files = ref(null);

const send = async () => {
  const formData = new FormData();

  if (files.value.length > 1 && Array.from(files.value).some(file => file.type === "application/x-zip-compressed")) {
    toast.error("Nie można przesłać pliku ZIP razem z innymi plikami!")
    return
  }

  if (files.value.length === 1 && files.value[0].type === "application/x-zip-compressed") { // ZIP
    formData.append('archive', files.value[0]);
    formData.append('username', authStore.user.username);

    try {
      await axios.post('/api/uploadArchive', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
    } catch (error) {
      if (error.status === 400) {
        toast.error(error.response.data);
      } else {
        toast.error("Wystąpił błąd przy przesyłaniu!")
      }
      console.error(error);
    }
} else if (files.value.length > 1) { // PARE PLIKOW
    const fileArray = Array.from(files.value);
    fileArray.forEach(file => {
      formData.append('files', file)
    });
    formData.append('username', authStore.user.username);

    try {
      await axios.post('/api/uploadFiles', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      toast.success("Pomyślnie udało się wysłać pliki!")
    } catch (error) {
      if (error.status === 400) {
        toast.error(error.response.data);
      } else {
        toast.error("Wystąpił błąd przy przesyłaniu!")
      }
      console.error(error);
    }
  } else if (files.value.length === 1) { // JEDEN PLIK
    formData.append("file", files.value[0]);
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
}
</script>

<template>
  <div class="container mt-5">
    <h3>Wybierz pliki do wysłania</h3>
    <form @submit.prevent="send">
      <div class="mb-3">
        <label for="fileInput" class="form-label">Wybierz pliki</label>
        <input type="file" id="fileInput" class="form-control" @change="e => files = e.target.files" multiple required />
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
