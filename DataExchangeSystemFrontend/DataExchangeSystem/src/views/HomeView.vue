<script>
import axios from 'axios';

export default {
  data() {
    return {
      files: [],
    };
  },
  methods: {
    async fetchFiles() {
      const token = localStorage.getItem('token');
      try {
        const response = await axios.get("http://localhost:8080/files", {
          headers: {
            Authorization: `Bearer ${token}`,
          }
        });
        this.files = response.data;
      } catch (error) {
        console.log(error);
      }
    },
    loadFiles() {
      this.fetchFiles();
    }
  },
  mounted() {
    this.fetchFiles();
  }
}
</script>

<template>
  <main>
    <div>
      <h2>pliki</h2>
      <ul v-if="files.length > 0">
        <li v-for="blob in files" :key="blob.blobName">
          <a :href="blob.blobUrl" target="_blank">{{ blob.blobName }}</a>
        </li>
      </ul>
      <p v-else>no pliki</p>
    </div>
  </main>
</template>
