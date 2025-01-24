<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import axios from 'axios';
import { useToast } from 'vue-toastification';
import { useAuthStore } from '@/stores/auth.js';

const authStore = useAuthStore();
const toast = useToast();
const files = ref(null);

const connection = ref(null);
const progress = ref(0);
const isConnected = ref(false);
const isComputing = ref(false);

const openWebSocketConnection = () => {
  connection.value = new WebSocket("ws://localhost:8080/ws/progress");

  connection.value.onopen = () => {
    toast.success("Połączono WebSocket!", {
      position: 'bottom-left'
    })
    isConnected.value = true;
  };

  connection.value.onmessage = (event) => {
    progress.value = event.data;
    progress.value = parseFloat(parseFloat(progress.value).toFixed(2));

    if (event.data >= 100) {
      progress.value = 0;
      isComputing.value = false;
      toast.success("Przesyłanie zakończone!");
    }
  };

  connection.value.onerror = (error) => {
    console.error("Błąd WebSocket:", error);
    isConnected.value = false;
  };

  connection.value.onclose = () => {
    toast.info("Zamknięto WebSocket!", {
      position: 'bottom-left'
    })
    isConnected.value = false;
  };
};

onMounted(() => {
  openWebSocketConnection();
});

onBeforeUnmount(() => {
  if (connection.value) {
    connection.value.close();
  }
});

const send = async () => {
  const formData = new FormData();
  if (files.value.length > 1 && Array.from(files.value).some(file => file.type === "application/x-zip-compressed")) {
    toast.error("Nie można przesłać pliku ZIP razem z innymi plikami!");
    return;
  }

  isComputing.value = true;
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
      handleUploadError(error);
    }
  } else if (files.value.length > 1) { // Wiele plików
    const fileArray = Array.from(files.value);
    fileArray.forEach(file => {
      formData.append('files', file);
    });
    formData.append('username', authStore.user.username);

    try {
      await axios.post('/api/uploadFiles', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      isComputing.value = false;
      toast.success("Pomyślnie udało się wysłać pliki!");
    } catch (error) {
      handleUploadError(error);
    }
  } else if (files.value.length === 1) { // Jeden plik
    formData.append("file", files.value[0]);
    formData.append('username', authStore.user.username);

    try {
      await axios.post('/api/uploadFile', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      isComputing.value = false;
      toast.success("Pomyślnie udało się wysłać plik!");
    } catch (error) {
      handleUploadError(error);
    }
  }
};

const handleUploadError = (error) => {
  isComputing.value = false;
  if (error.response?.status === 400) {
    toast.error(error.response.data);
  } else {
    toast.error("Wystąpił błąd przy przesyłaniu!");
  }
  console.error(error);
};
</script>

<template>
  <div class="container mt-5">
    <h3>Wybierz pliki do wysłania</h3>
    <form @submit.prevent="send">
      <div class="mb-3">
        <label for="fileInput" class="form-label">Wybierz pliki</label>
        <input
          type="file"
          id="fileInput"
          class="form-control"
          @change="e => files = e.target.files"
          multiple
          required
        />
      </div>
      <button type="submit" class="btn btn-primary" :disabled="progress > 0 && progress < 100 || isComputing">
        {{ isComputing ? "Przetwarzanie..." : progress > 0 && progress < 100 ? "Przesyłanie..." : "Wyślij plik" }}
      </button>
    </form>

    <div v-if="progress > 0" class="mt-3">
      <h5>Postęp przesyłania</h5>
      <div class="progress">
        <div
          class="progress-bar progress-bar-striped progress-bar-animated"
          role="progressbar"
          :style="{ width: progress + '%' }"
          :aria-valuenow="progress"
          aria-valuemin="0"
          aria-valuemax="100"
        >
          {{ progress }}%
        </div>
      </div>
    </div>
    <div v-if="isComputing && progress === 0" class="mt-3">
      <p class="text-danger">Trwa przetwarzanie. Proszę czekać...</p>
    </div>
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

.progress {
  height: 25px;
}
</style>
