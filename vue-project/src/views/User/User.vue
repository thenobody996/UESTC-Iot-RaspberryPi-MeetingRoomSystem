<template>
  <div class="user-page">
    <div class="user-card">
      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          accept="image/*"
          :before-upload="beforeAvatarUpload"
        >
          <div class="avatar-preview" role="img" :aria-label="`用户头像`">
            <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" />
            <div v-else class="avatar-placeholder">上传头像</div>
          </div>
        </el-upload>
        <div class="avatar-hint">建议：jpg/png，大小不超过 2MB</div>
      </div>

      <div class="profile-section">
        <el-form class="user-form" :model="formData" label-width="100px">
          <el-form-item class="user-form-item" label="用户ID">
            <el-input class="user-form-input" v-model="formData.id" disabled />
          </el-form-item>

          <el-form-item class="user-form-item" label="账号">
            <el-input class="user-form-input" v-model="formData.account" disabled />
          </el-form-item>

          <el-form-item class="user-form-item" label="用户名">
            <el-input class="user-form-input" v-model="formData.userName" maxlength="30" show-word-limit />
          </el-form-item>

          <el-form-item class="user-form-item" label="邮箱">
            <el-input class="user-form-input" v-model="formData.email" />
          </el-form-item>

          <el-form-item class="user-form-item" label="个人描述">
            <el-input
              class="user-form-input"
              type="textarea"
              v-model="formData.description"
              :rows="4"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item class="user-form-item">
            <div class="actions-row">
              <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
              <el-button @click="resetProfile">重置</el-button>
              <el-button type="danger" @click="logout">退出登录</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { profileAPI } from '@/api/profile'
import type { Profile } from '@/types/api'
import { handleApiError } from '@/utils/errorHandler'

// 前端表单数据类型
interface FormData {
  id: number
  account: string
  userName: string
  description: string
  email: string
  avatar: string
}

export default defineComponent({
  name: 'UserView',
  setup() {
    const router = useRouter()

    const defaultFormData: FormData = {
      id: 0,
      account: '',
      userName: '',
      description: '',
      email: '',
      avatar: ''
    }

    const formData = reactive<FormData>({ ...defaultFormData })
    const avatarUrl = ref<string>('')
    const saving = ref<boolean>(false)
    const currentProfileId = ref<number>(0)

    // 加载用户资料
    const loadProfile = async () => {
      try {
        // 从 sessionStorage 获取用户信息
        const userInfoRaw = sessionStorage.getItem('userInfo')
        if (!userInfoRaw) {
          ElMessage.warning('请先登录')
          router.push('/login')
          return
        }

        const userInfo = JSON.parse(userInfoRaw)

        // 设置基本信息
        formData.id = userInfo.id || 0
        formData.account = userInfo.account || ''

        // 如果有本地存储的资料，先加载
        const localProfile = sessionStorage.getItem('userProfile')
        if (localProfile) {
          const parsed = JSON.parse(localProfile)
          formData.userName = parsed.userName || userInfo.account || ''
          formData.email = parsed.email || ''
          formData.description = parsed.description || ''
          formData.avatar = parsed.avatar || ''
          avatarUrl.value = formData.avatar || ''
          currentProfileId.value = parsed.id || 0
        } else {
          // 使用用户信息初始化
          formData.userName = userInfo.userName || userInfo.account || ''
          formData.email = userInfo.email || ''
          formData.description = userInfo.description || ''
          formData.avatar = userInfo.avatar || ''
          avatarUrl.value = formData.avatar || ''
          currentProfileId.value = userInfo.id || 0
        }
      } catch (e) {
        console.warn('加载用户信息失败', e)
        ElMessage.error('加载用户信息失败')
      }
    }

    onMounted(() => {
      loadProfile()
    })

    const fileToBase64 = (file: File): Promise<string> => {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => resolve(reader.result as string)
        reader.onerror = (err) => reject(err)
        reader.readAsDataURL(file)
      })
    }

    const compressImage = async (file: File, maxSide = 800, quality = 0.8): Promise<string> => {
      const originalDataUrl = await fileToBase64(file)
      const img = new Image()
      img.src = originalDataUrl
      await new Promise<void>((resolve, reject) => {
        img.onload = () => resolve()
        img.onerror = (e) => reject(e)
      })

      const w = img.width
      const h = img.height
      let targetW = w
      let targetH = h
      const maxDim = Math.max(w, h)
      if (maxDim > maxSide) {
        const ratio = maxSide / maxDim
        targetW = Math.round(w * ratio)
        targetH = Math.round(h * ratio)
      }

      const canvas = document.createElement('canvas')
      canvas.width = targetW
      canvas.height = targetH
      const ctx = canvas.getContext('2d')
      if (!ctx) throw new Error('无法获取 canvas 上下文')
      ctx.drawImage(img, 0, 0, targetW, targetH)

      const preservePng = file.type === 'image/png'
      const outputType = preservePng ? 'image/png' : 'image/jpeg'
      const compressed = canvas.toDataURL(outputType, quality)

      const sizeOfBase64 = (b64: string) => Math.ceil((b64.length - b64.indexOf(',') - 1) * 3 / 4)
      if (sizeOfBase64(compressed) >= sizeOfBase64(originalDataUrl)) {
        return originalDataUrl
      }
      return compressed
    }

    const beforeAvatarUpload = async (file: File) => {
      const isImage = file.type && file.type.startsWith('image/')
      if (!isImage) {
        ElMessage.error('只能上传图片文件')
        return false
      }
      const limit = 2 * 1024 * 1024 // 2MB
      if (file.size > limit) {
        try {
          const compressed = await compressImage(file, 1024, 0.75)
          const sizeAfter = Math.ceil((compressed.length - compressed.indexOf(',') - 1) * 3 / 4)
          if (sizeAfter > limit) {
            ElMessage.error('图片大小不能超过 2MB，且压缩后仍超出限制')
            return false
          }
          avatarUrl.value = compressed
          formData.avatar = compressed
          ElMessage.success('头像已压缩并选择（预览）。请点击保存以最终保存配置。')
          return false
        } catch (e) {
          console.error(e)
          ElMessage.error('图片处理失败')
          return false
        }
      }

      try {
        const dataUrl = await fileToBase64(file)
        avatarUrl.value = dataUrl
        formData.avatar = dataUrl
        ElMessage.success('头像已选择（并预览）。请点击保存以最终保存配置。')
      } catch (e) {
        console.error(e)
        ElMessage.error('读取图片失败')
      }
      return false
    }

    const saveProfile = async () => {
      if (!formData.userName || !formData.email) {
        ElMessage.warning('请填写用户名和邮箱')
        return
      }

      if (!currentProfileId.value) {
        ElMessage.error('用户ID不存在，请重新登录')
        return
      }

      saving.value = true
      try {
        // 构建符合后端 Profile 接口的数据
        const profileData: Profile = {
          userName: formData.userName,
          description: formData.description,
          email: formData.email,
          avatar: formData.avatar,
          isDeleted: false
        }

        const response = await profileAPI.updateProfile(currentProfileId.value, profileData)

        if (response.code === 200) {
          // 更新本地存储
          const toSave = {
            id: currentProfileId.value,
            account: formData.account,
            userName: formData.userName,
            description: formData.description,
            email: formData.email,
            avatar: formData.avatar
          }
          sessionStorage.setItem('userProfile', JSON.stringify(toSave))

          // 同时更新 userInfo
          const userInfo = {
            id: currentProfileId.value,
            account: formData.account,
            userName: formData.userName,
            email: formData.email,
            avatar: formData.avatar
          }
          sessionStorage.setItem('userInfo', JSON.stringify(userInfo))

          window.dispatchEvent(new CustomEvent('userProfileUpdated', { detail: toSave }))
          ElMessage.success('保存成功')
        } else {
          ElMessage.error(response.message || '保存失败')
        }
      } catch (error) {
        handleApiError(error, '保存失败，请稍后重试')
      } finally {
        saving.value = false
      }
    }

    const resetProfile = () => {
      loadProfile()
      ElMessage.info('已恢复为上次保存的配置')
    }

    const logout = () => {
      try {
        sessionStorage.removeItem('userInfo')
        sessionStorage.removeItem('userProfile')
        sessionStorage.removeItem('token')
      } catch (e) {
        console.warn('清理 sessionStorage 失败', e)
      }
      window.dispatchEvent(new CustomEvent('userProfileUpdated', { detail: null }))
      ElMessage.success('已退出登录')
      router.push('/login')
    }

    return {
      formData,
      avatarUrl,
      saving,
      beforeAvatarUpload,
      saveProfile,
      resetProfile,
      logout
    }
  }
})
</script>

<!-- 样式保持不变 -->
<style scoped>
.user-page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  box-sizing: border-box;
  padding: 2rem 1rem;
  background: #f5f7fa;
}

.user-card {
  width: min(1200px, 100%);
  min-height: 500px;
  display: flex;
  gap: 2rem;
  background: #fff;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 12px 48px rgba(0,0,0,0.12);
  box-sizing: border-box;
  align-items: flex-start;
  overflow: visible;
  margin: 0 auto;
  margin-bottom: 2rem;
}

.avatar-section {
  flex: 0 0 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding-right: 0;
}

.avatar-uploader {
  width: 100%;
  display: flex;
  justify-content: center;
}

.avatar-preview {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border: 2px dashed #e1e6f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-preview:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.avatar-hint {
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.4;
  max-width: 200px;
}

.profile-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: visible;
  padding-top: 0.5rem;
}

.user-form {
  width: 100%;
  max-width: 100%;
}

.user-form .user-form-item {
  width: 100%;
  margin-bottom: 24px;
}

.user-form :deep(.el-form-item__label) {
  color: #333 !important;
  font-weight: 500;
  font-size: 14px;
  text-align: left;
}

.user-form .user-form-input :deep(.el-input__inner),
.user-form .user-form-input :deep(.el-textarea__inner) {
  color: #333 !important;
  background: #fff !important;
  border: 1px solid #e1e6f0;
  border-radius: 6px;
  font-size: 14px;
  width: 100%;
}

.user-form .user-form-input :deep(.el-input__inner):focus,
.user-form .user-form-input :deep(.el-textarea__inner):focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.user-form .user-form-input :deep(.is-disabled .el-input__inner) {
  background-color: #f5f7fa !important;
  color: #666 !important;
  border-color: #e4e7ed;
}

.actions-row {
  display: flex;
  gap: 12px;
  width: 100%;
  justify-content: flex-start;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.actions-row .el-button {
  min-width: 100px;
}

@media (max-width: 1200px) {
  .user-card {
    width: 95%;
    margin: 0 auto 2rem;
  }
}

@media (max-width: 980px) {
  .avatar-section {
    flex: 0 0 240px;
  }
  .avatar-preview {
    width: 160px;
    height: 160px;
  }
  .avatar-hint {
    max-width: 160px;
  }
}

@media (max-width: 768px) {
  .user-page {
    padding: 1rem 0.5rem;
  }

  .user-card {
    flex-direction: column;
    padding: 2rem 1.5rem;
    width: 100%;
    min-height: auto;
    gap: 1.5rem;
    margin-bottom: 1rem;
  }

  .avatar-section {
    width: 100%;
    align-items: center;
    flex: none;
  }

  .avatar-preview {
    width: 120px;
    height: 120px;
  }

  .avatar-hint {
    max-width: 120px;
  }

  .profile-section {
    width: 100%;
  }

  .actions-row {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .user-page {
    padding: 0.5rem;
  }

  .user-card {
    padding: 1.5rem 1rem;
    border-radius: 8px;
  }

  .actions-row {
    flex-direction: column;
    align-items: stretch;
  }

  .actions-row .el-button {
    width: 100%;
  }
}

@media (min-width: 1400px) {
  .user-card {
    margin-top: 2rem;
  }
}

.user-form .el-form-item__content {
  flex: 1;
  min-width: 0;
}

.user-form .user-form-input :deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 80px;
}
</style>
