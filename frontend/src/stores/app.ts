import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 状态
  const sidebarCollapsed = ref(false)
  const pageLoading = ref(false)
  const device = ref<'desktop' | 'mobile'>('desktop')

  // 方法
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
  }

  function setPageLoading(loading: boolean) {
    pageLoading.value = loading
  }

  function setDevice(dev: 'desktop' | 'mobile') {
    device.value = dev
  }

  // 检测设备类型
  function detectDevice() {
    const width = window.innerWidth
    if (width < 768) {
      device.value = 'mobile'
      sidebarCollapsed.value = true
    } else {
      device.value = 'desktop'
    }
  }

  return {
    sidebarCollapsed,
    pageLoading,
    device,
    toggleSidebar,
    setSidebarCollapsed,
    setPageLoading,
    setDevice,
    detectDevice
  }
})
