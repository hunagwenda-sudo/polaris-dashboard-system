<template>
  <!-- Login page: no sidebar/header -->
  <router-view v-if="isLoginPage" />

  <!-- Main layout -->
  <div v-else class="flex h-screen bg-trust-900 overflow-hidden antialiased">
    <Sidebar />
    <div class="flex-1 flex flex-col overflow-hidden">
      <Header />
      <main class="flex-1 overflow-y-auto">
        <div class="px-6 py-5">
          <router-view />
        </div>
      </main>
    </div>
  </div>

  <ConfirmDialog />
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const isLoginPage = computed(() => route.name === 'login' || route.name === 'changePassword')

const auth = useAuthStore()
onMounted(() => {
  if (auth.isLoggedIn) auth.fetchEstimatedLevel()
})
</script>
