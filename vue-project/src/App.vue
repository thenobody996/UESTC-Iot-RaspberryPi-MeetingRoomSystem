<script setup lang="ts">
import { useRoute } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { onMounted, onUnmounted, ref } from 'vue'

const route = useRoute()
const windowHeight = ref('100vh')

// 处理移动端高度适配
const updateHeight = () => {
  windowHeight.value = `${window.innerHeight}px`
}

onMounted(() => {
  updateHeight()
  window.addEventListener('resize', updateHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateHeight)
})
</script>

<template>
  <div id="app" :style="{ height: windowHeight }">
    <!-- 只有当路由没有设置 meta.hideSidebar 时显示侧栏 -->
    <Navbar v-if="!route.meta?.hideSidebar" />

    <!-- 主内容，若侧栏存在则应用左侧间距 -->
    <div :class="['main-container', { 'with-sidebar': !route.meta?.hideSidebar }]">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
#app {
  display: flex;
  width: 100vw;
  min-height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 移动端适配 */
@media (max-width: 768px) {
  #app {
    flex-direction: column;
  }

  .main-container {
    margin-left: 0 !important;
    padding: 16px;
    width: 100%;
  }
}

.main-container {
  box-sizing: border-box;
  flex: 1;
  width: 100%;
  min-height: 100%;
  transition: all 0.3s ease;
  padding: 24px;
  overflow: auto; /* 允许内容区域单独滚动 */
  background-color: #f5f5f5; /* 可选：添加背景色更好地可视化区域 */
}

/* 当侧栏存在时，给主内容左侧留出与侧栏相同的宽度 */
.with-sidebar {
  margin-left: 220px;
  width: calc(100% - 220px);
}

/* 大屏幕优化 */
@media (min-width: 1200px) {
  .main-container {
    padding: 32px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .main-container {
    padding: 12px;
  }
}

/* 确保所有子元素都能正确继承高度 */
#app > * {
  box-sizing: border-box;
}
</style>
