<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useToast } from 'vue-toastification'
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js'
import router from '@/router/index.js'

const authStore = useAuthStore();
const toast = useToast();
const route = useRoute();

const fileName = route.params.fileName;
const isLoading = ref(true);
const versions = ref([]);

const fetchVersions = async () => {
  try {
    const response = await axios.get('/api/allFileVersions', {
      params: { username: authStore.user.username, fileName: fileName }
    });
    versions.value = response.data;
  } catch (error) {
    toast.error("Błąd w trakcie ładowania wersji pliku.");
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const formatFileSize = (size) => {
  if (size < 1024) {
    return `${size} B`;
  } else if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(2)} KB`;
  } else if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(2)} MB`;
  } else if (size < 1024 * 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`;
  }
}

const downloadVersion = async (file) => {
  const url = file.blobUrl.replace('https://descontainer.blob.core.windows.net', '/blob')
  let response;
  try {
    response = await axios.get(url, { responseType: 'arraybuffer' });
  } catch (error) {
    console.error(error);
    toast.error("Błąd w trakcie pobierania pliku.");
    return;
  }

  const blob = new Blob([response.data], { type: 'application/octet-stream' });

  const blobUrl = URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = file.blobName;

  link.click();

  URL.revokeObjectURL(blobUrl);
}

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchVersions();
});
</script>

<template>
  <div class="container mt-5">
    <button
      @click="goBack"
      class="btn btn-secondary mb-3">
      <i class="pi pi-arrow-left" style="font-size: 1.2em;"></i> Wróć
    </button>

    <h3>Wersje pliku: {{ fileName }}</h3>
    <div v-if="isLoading" class="text-center">
      <i class="pi pi-spinner pi-spin" style="font-size: 2em;"></i>
    </div>

    <div v-else-if="versions.length === 0" class="alert alert-info text-center">
      Brak dostępnych wersji tego pliku
    </div>

    <table v-else class="table table-striped table-hover">
      <thead class="thead-dark">
      <tr>
        <th>Wersja</th>
        <th>Rozmiar Pliku</th>
        <th>Data modyfikacji</th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(version, index) in versions" :key="version.version">
        <td>{{ version.version }}</td>
        <td>{{(formatFileSize(version.fileSize))}}</td>
        <td>{{ new Date(version.lastModification).toLocaleString('pl-PL', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }) }}</td>
        <td>
          <button
            @click="downloadVersion(version)"
            class="btn btn-primary btn-sm"
            title="Pobierz wersję">
            <i class="pi pi-download" style="font-size: 1.2em;"></i> Pobierz
          </button>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
