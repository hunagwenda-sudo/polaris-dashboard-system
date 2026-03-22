<template>
  <!-- Login page: no sidebar/header -->
  <router-view v-if="isLoginPage" />

  <!-- Main layout -->
  <div v-else class="flex h-screen bg-trust-900 overflow-hidden antialiased">
    <!-- Mobile overlay -->
    <div v-if="sidebarOpen" class="fixed inset-0 bg-black/50 z-40 lg:hidden" @click="sidebarOpen = false" />
    <!-- Sidebar: hidden on mobile, slide-in when toggled -->
    <div :class="['fixed inset-y-0 left-0 z-50 lg:static lg:z-auto lg:h-auto transition-transform duration-200 h-full',
      sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0']">
      <Sidebar class="h-full" @navigate="sidebarOpen = false" />
    </div>
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <Header @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      <main class="flex-1 overflow-y-auto">
        <div class="px-4 py-4 sm:px-6 sm:py-5">
          <router-view />
        </div>
      </main>
    </div>
  </div>

  <ConfirmDialog />
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const isLoginPage = computed(() => route.name === 'login' || route.name === 'changePassword')
const sidebarOpen = ref(false)

// 路由切换时关闭侧边栏
watch(() => route.path, () => { sidebarOpen.value = false })

const auth = useAuthStore()
onMounted(() => {
  if (auth.isLoggedIn) auth.fetchEstimatedLevel()
})
</script>
