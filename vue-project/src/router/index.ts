import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

import LoginRegister from "@/views/Login/LoginRegister.vue";
import Meeting from '@/views/Meeting/MeetingPage.vue'
import User from '@/views/User/User.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:'/',
      redirect:'/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginRegister,
      meta: { hideSidebar: true } // 登录页不显示侧栏
    },
    {
      path: '/meeting',
      name: 'meeting',
      component: Meeting,
      meta: { hideSidebar: false }
    },
    {
      path: '/user',
      name: 'user',
      component: User,
      meta: { hideSidebar: false }
    }
  ],
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!sessionStorage.getItem('userInfo')

  // 如果路由要跳转到除了登录和注册的界面的话就判断是否已经登录
  if (to.path !== '/login' && to.path !== '/register' && !isAuthenticated) {
    ElMessage({
      message: '请先登录！',
      type: 'warning'
    })
    next({ path: '/login' })
  } else {
    next()
  }
})

export default router
