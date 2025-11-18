<template>
  <div class="auth-wrapper">
    <div class="auth-container">
      <div class="auth-header">
        <h1>{{ isLogin ? '欢迎登录系统' : '注册新账号' }}</h1>
      </div>

      <div class="auth-content" v-loading="loading">
        <!-- 登录表单 -->
        <div class="form-container" :class="{ active: isLogin }">
          <el-form
            :model="loginForm"
            :rules="loginRules"
            ref="loginFormRef"
            @submit.prevent="handleLogin"
          >
            <div class="form-group">
              <el-form-item prop="account">
                <el-input
                  v-model="loginForm.account"
                  placeholder="请输入账号"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-group">
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-group">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a href="#" style="float: right; color: #5b7cfa;">忘记密码?</a>
            </div>

            <div class="form-actions">
              <el-button type="primary" size="large" @click="handleLogin" style="width: 100%;">
                立即登录
              </el-button>
            </div>
          </el-form>

          <a class="switch-link" @click="switchToRegister">
            还没有账号？立即注册
          </a>
        </div>

        <!-- 注册表单 -->
        <div class="form-container" :class="{ active: !isLogin }">
          <a class="back-link" @click="switchToLogin">
            <el-icon><ArrowLeft /></el-icon> 返回登录
          </a>

          <el-form
            :model="registerForm"
            :rules="registerRules"
            ref="registerFormRef"
            @submit.prevent="handleRegister"
          >
            <div class="form-group">
              <el-form-item prop="account">
                <el-input
                  v-model="registerForm.account"
                  placeholder="请输入账号"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-group">
              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-group">
              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  size="large"
                  show-password
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-group">
              <el-checkbox v-model="agreeTerms">
                我已阅读并同意<a href="#" style="color: #5b7cfa;">服务条款</a>和<a href="#" style="color: #5b7cfa;">隐私政策</a>
              </el-checkbox>
            </div>

            <div class="form-actions">
              <el-button type="primary" size="large" @click="handleRegister" style="width: 100%;">
                立即注册
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { ref, reactive, defineComponent, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { userAPI } from '@/api/user'
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import type { BaseResponse, User as UserType } from '@/types/api'
import { handleApiError } from '@/utils/errorHandler'

export default defineComponent({
  name: 'LoginRegister',
  components: {
    User,
    Lock,
    ArrowLeft
  },
  setup() {
    const router = useRouter()
    const isLogin = ref(true)
    const loading = ref(false)
    const rememberMe = ref(false)
    const agreeTerms = ref(false)
    const loginFormRef = ref()
    const registerFormRef = ref()

    const loginForm = reactive({
      account: '',
      password: ''
    })

    const registerForm = reactive({
      account: '',
      password: '',
      confirmPassword: ''
    })

    // 虚拟账号列表
    const demoAccounts = [
      { account: 'demo', password: '123456', name: '演示用户', id: 1, role: 'user' },
      { account: 'test', password: '123456', name: '测试用户', id: 2, role: 'user' },
      { account: 'admin', password: 'admin123', name: '管理员', id: 3, role: 'admin' }
    ]

    // 登录表单验证规则
    const loginRules = {
      account: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 16, message: '账号长度在 3 到 16 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    }

    // 注册表单验证规则
    const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const registerRules = {
      account: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { min: 3, max: 16, message: '账号长度在 3 到 16 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }

    // 模拟登录成功
    const simulateLoginSuccess = (account: string) => {
      const user = demoAccounts.find(u => u.account === account) || {
        account: account,
        name: account + '用户',
        id: Date.now()
      }

      return {
        code: 200,
        message: '登录成功',
        data: user
      }
    }

    // 模拟注册成功
    const simulateRegisterSuccess = (account: string) => {
      return {
        code: 200,
        message: '注册成功',
        data: {
          account: account,
          name: account + '用户',
          id: Date.now()
        }
      }
    }

    // 检查是否为虚拟账号
    const isDemoAccount = (account: string, password: string): boolean => {
      return demoAccounts.some(user =>
        user.account === account && user.password === password
      )
    }

    // 切换到注册界面
    const switchToRegister = () => {
      // 如果登录表单中已经输入了账号，将其复制到注册表单
      if (loginForm.account) {
        registerForm.account = loginForm.account
      }
      isLogin.value = false
    }

    // 切换到登录界面
    const switchToLogin = () => {
      isLogin.value = true
      // 清空注册表单
      if (registerFormRef.value) {
        registerFormRef.value.resetFields()
      }
    }

    // 处理登录
    const handleLogin = async () => {
      if (!loginFormRef.value) return

      try {
        const valid = await loginFormRef.value.validate()
        if (!valid) return

        loading.value = true

        // 检查是否为虚拟账号
        if (isDemoAccount(loginForm.account, loginForm.password)) {
          // 使用虚拟账号登录
          console.log('🚀 使用虚拟账号登录:', loginForm.account)
          await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟网络延迟

          const response = simulateLoginSuccess(loginForm.account)

          ElMessage.success('登录成功！')
          // 保存用户信息到 sessionStorage
          try {
            // ensure role is present for permission checks
            const out = Object.assign({ role: 'user' }, response.data)
            sessionStorage.setItem('userInfo', JSON.stringify(out))
            sessionStorage.setItem('token', 'demo-token-' + response.data.id)
            sessionStorage.setItem('isDemo', 'true') // 标记为演示模式
          } catch (e) {
            console.warn('无法写入 sessionStorage:', e)
          }
          // 跳转到会议页
          router.push('/meeting')
        } else {
          // 正常调用登录API
          try {
            const response = await userAPI.login({
              account: loginForm.account,
              password: loginForm.password
            })

            if (response.code === 20000) {
              ElMessage.success('登录成功！')
              // 保存用户信息到 sessionStorage
              try {
                // backend response should include role; if not, default to 'user'
                const out = Object.assign({ role: 'user' }, response.data)
                sessionStorage.setItem('userInfo', JSON.stringify(out))
                sessionStorage.setItem('token', 'logged-in')
                sessionStorage.setItem('isDemo', 'false')
              } catch (e) {
                console.warn('无法写入 sessionStorage:', e)
              }
              // 跳转到会议页
              router.push('/meeting')
            } else {
              ElMessage.error(response.message || '登录失败，请检查账号和密码')
            }
          } catch (error) {
            handleApiError(error, '登录失败，请稍后重试')
          }
        }
      } catch (error) {
        handleApiError(error, '登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 处理注册
    const handleRegister = async () => {
      if (!registerFormRef.value) return

      try {
        const valid = await registerFormRef.value.validate()
        if (!valid) return

        if (!agreeTerms.value) {
          ElMessage.warning('请同意服务条款和隐私政策')
          return
        }

        loading.value = true

        // 模拟注册过程
        console.log('🚀 模拟注册账号:', registerForm.account)
        await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟网络延迟

        const response = simulateRegisterSuccess(registerForm.account)

        ElMessage.success('注册成功！')
        // 自动填充登录表单并切换回登录界面
        loginForm.account = registerForm.account
        loginForm.password = '' // 清空密码，让用户重新输入
        switchToLogin()
      } catch (error) {
        handleApiError(error, '注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 全屏适配
    const updateContainerHeight = () => {
      const container = document.querySelector('.auth-container') as HTMLElement
      if (container) {
        const gap = 60
        const viewportHeight = window.innerHeight
        const fixedHeight = Math.max(300, viewportHeight - gap * 2)
        container.style.height = `${fixedHeight}px`
        container.style.maxHeight = `${fixedHeight}px`
        container.style.minHeight = 'auto'
        container.style.overflow = 'hidden'
      }
    }

    onMounted(() => {
      updateContainerHeight()
      window.addEventListener('resize', updateContainerHeight)
    })

    onUnmounted(() => {
      window.removeEventListener('resize', updateContainerHeight)
    })

    return {
      isLogin,
      loading,
      rememberMe,
      agreeTerms,
      loginForm,
      registerForm,
      loginRules,
      registerRules,
      loginFormRef,
      registerFormRef,
      switchToRegister,
      switchToLogin,
      handleLogin,
      handleRegister
    }
  }
})
</script>

<style scoped>
/* 原有样式保持不变 */
.auth-wrapper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

.auth-container {
  width: 100%;
  max-width: 600px;
  background: white;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  margin: 60px 0;
  height: calc(100vh - 120px);
  max-height: calc(100vh - 120px);
  border-radius: 8px;
}

.auth-header {
  background: #5b7cfa;
  color: white;
  padding: 24px 20px;
  text-align: center;
  flex-shrink: 0;
}

.auth-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.auth-content {
  padding: 30px;
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  box-sizing: border-box;
}

.auth-content::-webkit-scrollbar {
  display: none;
}

.auth-content {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.form-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  width: auto;
  padding: 30px;
  opacity: 0;
  transform: translateX(20px);
  transition: all 0.4s ease;
  visibility: hidden;
}

.form-container.active {
  opacity: 1;
  transform: translateX(0);
  visibility: visible;
}

.form-group {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.switch-link {
  color: #5b7cfa;
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
  text-align: center;
  display: block;
  margin-top: 20px;
}

.switch-link:hover {
  text-decoration: underline;
}

.back-link {
  color: #666;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 15px;
  display: inline-flex;
  align-items: center;
}

.back-link:hover {
  color: #5b7cfa;
}

/* 响应式设计保持不变 */
@media (max-width: 768px) {
  .auth-container {
    max-width: 100%;
    border-radius: 0;
  }

  .auth-content {
    padding: 20px;
  }

  .form-container {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .auth-header {
    padding: 20px 15px;
  }

  .auth-header h1 {
    font-size: 20px;
  }

  .auth-content {
    padding: 15px;
  }

  .form-container {
    padding: 15px;
  }
}

@media (max-height: 600px) {
  .auth-content {
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .form-group {
    margin-bottom: 15px;
  }

  .form-actions {
    margin-top: 15px;
  }
}

@media (max-height: 500px) and (orientation: landscape) {
  .auth-container {
    flex-direction: row;
    height: 100vh;
    max-width: 100%;
  }

  .auth-header {
    width: 40%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
  }

  .auth-content {
    width: 60%;
    overflow: hidden;
  }
}
</style>
