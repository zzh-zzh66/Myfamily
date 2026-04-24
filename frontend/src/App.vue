<template>
  <div id="app" class="myfamily-app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 初始化用户状态
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
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
