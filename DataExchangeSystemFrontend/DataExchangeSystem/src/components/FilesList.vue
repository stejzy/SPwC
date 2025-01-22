<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.js'

const authStore = useAuthStore();

const files = ref([]);
const isLoading = ref(true);

const fetchFiles = async () => {
  try {
    const response = await axios.get('http://localhost:8080/files', {
      params: { "username": authStore.user.username }
    });
    files.value = response.data;
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
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
        <th>Akcja</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(file, index) in files" :key="file.name">
        <td>{{ index + 1 }}</td>
        <td>{{ file.blobName }}</td>
        <td>
          <a :href="file.blobUrl" class="btn btn-success btn-sm" download :title="'Pobierz ' + file.blobName">
            Pobierz
          </a>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>

</style>
