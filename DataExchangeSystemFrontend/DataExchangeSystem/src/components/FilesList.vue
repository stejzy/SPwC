<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth.js'

const authStore = useAuthStore();

const files = ref([]);

const fetchFiles = async () => {
  await axios.get('http://localhost:8080/files', {
    params: { "username": authStore.user.username }
  })
    .then(res => files.value = res.data)
    .catch(error => console.error(error));
  console.log(files.value);
};

onMounted(() => {
  fetchFiles();
});
</script>

<template>
  <div class="container mt-5">
    <div v-if="files.length === 0" class="alert alert-info text-center">
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
