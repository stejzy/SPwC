<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.js'
import { useToast } from 'vue-toastification'
import {useRouter} from 'vue-router'

const router = useRouter();

const toast = useToast();

const authStore = useAuthStore();

const files = ref([]);
const isLoading = ref(true);

const fetchFiles = async () => {
  try {
    const response = await axios.get('/api/newestFiles', {
      params: { "username": authStore.user.username }
    });
    files.value = response.data;
    console.log(files.value);
  } catch (error) {
    toast.error("Błąd w trakcie ładowania plików.");
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const deleteFile = async (blobName) => {
  if (confirm(`Czy na pewno chcesz usunąć plik "${blobName}"?`)) {
    try {
      await axios.delete(`/api/deleteFile`, {
        params: { username: authStore.user.username, fileName: blobName }
      });
      files.value = files.value.filter(file => file.blobName !== blobName);
    } catch (error) {
      toast.error('Błąd podczas usuwania pliku.');
      console.error(error);
    }
  }
};

const downloadFile = async (file) => {
  const url = file.blobUrl.replace('https://descontainer.blob.core.windows.net', '/blob')

  const originalAuthHeader = axios.defaults.headers.common['Authorization'];

  delete axios.defaults.headers.common['Authorization'];

  let response;
  try {
    response = await axios.get(url, { responseType: 'arraybuffer' });
  } catch (error) {
    console.error(error);
    toast.error("Błąd w trakcie pobierania pliku.");
    return;
  } finally {
    if (originalAuthHeader) {
      axios.defaults.headers.common['Authorization'] = originalAuthHeader;
    } else {
      delete axios.defaults.headers.common['Authorization'];
    }
  }

  const blob = new Blob([response.data], { type: 'application/octet-stream' });

  const blobUrl = URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = file.blobName;

  link.click();

  URL.revokeObjectURL(blobUrl);
}

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

const viewVersions = async (file) => {
  try {
    const response = await axios.get('/api/allFileVersions', {
      params: { username: authStore.user.username, fileName: file.blobName }
    });
    const versions = response.data;
    console.log('Wersje pliku:', versions);

    await router.push({ name: 'filesVersions', params: { fileName: file.blobName } });
  } catch (error) {
    toast.error("Błąd w trakcie ładowania wersji pliku.");
    console.error(error);
  }
};

onMounted(() => {
  fetchFiles();
});
</script>

<template>
  <div class="container mt-5">
    <div v-if="isLoading" class="text-center">
      <i class="pi pi-spinner pi-spin" style="font-size: 2em;"></i>
    </div>

    <div v-else-if="files.length === 0" class="alert alert-info text-center">
      Nie przesłałeś jeszcze żadnych plików
    </div>

    <table v-else class="table table-striped table-hover">
      <thead class="thead-dark">
      <tr>
        <th>#</th>
        <th>Nazwa Pliku</th>
        <th>Rozmiar pliku</th>
        <th>Ostatnia modyfikacja</th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(file, index) in files" :key="file.name">
        <td>{{ index + 1 }}</td>
        <td>{{ file.blobName }}</td>
        <td>{{(formatFileSize(file.fileSize))}}</td>
        <td>{{ new Date(file.lastModification).toLocaleString('pl-PL', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }) }}</td>
        <td>
          <div class="dropdown">
            <button
              class="btn btn-link p-0"
              type="button"
              id="dropdownMenuButton"
              data-bs-toggle="dropdown"
              aria-expanded="false"
              title="Akcje">
              <i class="pi pi-ellipsis-v" style="font-size: 1.5em; color: black;"></i>
            </button>
            <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton">
              <li>
                <button
                  @click="downloadFile(file)"
                  class="dropdown-item"
                  :title="'Pobierz ' + file.blobName">
                  Pobierz
                </button>
              </li>
              <li>
                <button
                  class="dropdown-item"
                  @click="viewVersions(file)"
                  :title="'Wersje ' + file.blobName">
                  Wersje
                </button>
              </li>
              <li>
                <button
                  class="dropdown-item text-danger"
                  @click="deleteFile(file.blobName)">
                  Usuń
                </button>
              </li>
            </ul>
          </div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.dropdown-menu {
  min-width: auto;
}

.dropdown-item.text-danger {
  color: #dc3545;
}

.dropdown-item.text-danger:hover {
  background-color: #f8d7da;
}

button.btn-link i {
  border-radius: 50%;
  padding: 5px;
  transition: background-color 0.3s ease;
}

button.btn-link i:hover {
  background-color: #e0e0e0;
}
</style>
