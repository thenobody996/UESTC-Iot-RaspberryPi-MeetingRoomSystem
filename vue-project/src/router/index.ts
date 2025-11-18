import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

import LoginRegister from "@/views/Login/LoginRegister.vue";
import Meeting from '@/views/Meeting/MeetingPage.vue'
import User from '@/views/User/User.vue'
import MeetingroomManager from '@/views/Admin/MeetingroomManager.vue'
import UserManager from '@/views/Admin/UserManager.vue'

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
    },
    // 管理员路由（占位）
    {
      path: '/admin/meetingroom',
      name: 'admin-meetingroom',
      component: MeetingroomManager,
      meta: { hideSidebar: false, requiresAdmin: true }
    },
    {
      path: '/admin/user',
      name: 'admin-user',
      component: UserManager,
      meta: { hideSidebar: false, requiresAdmin: true }
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
    return
  }

  // 如果目标路由要求管理员权限，则检查 userInfo.role
  if (to.meta && (to.meta as any).requiresAdmin) {
    try {
      const raw = sessionStorage.getItem('userInfo')
      if (!raw) {
        ElMessage.error('需要管理员权限，请先登录')
        next({ path: '/login' })
        return
      }
      const parsed = JSON.parse(raw)
      const role = parsed.role || parsed.authority || ''
      if (role !== 'admin') {
        ElMessage.error('没有权限访问此页面')
        next(false)
        return
      }
    } catch (e) {
      console.warn('解析 userInfo 失败', e)
      ElMessage.error('权限校验失败')
      next(false)
      return
    }
  }

  next()
})

export default router
