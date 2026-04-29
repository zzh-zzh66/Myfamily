<template>
  <div id="app" class="myfamily-app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.token) {
    userStore.fetchUserInfo()
    connectWebSocket()
  }
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style lang="scss">
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.myfamily-app {
  width: 100%;
  height: 100%;
  min-height: 100vh;
  background-color: $color-bg-page;
  font-family: $font-family-serif;
  color: $color-text-primary;
}
</style>
