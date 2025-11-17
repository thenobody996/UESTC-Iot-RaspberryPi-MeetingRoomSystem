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

        <!-- 新增：上传进度条 -->
        <el-progress
          v-if="uploading"
          class="avatar-progress"
          :percentage="uploadProgress"
          :stroke-width="8"
          text-color="#fff"
          :status="uploadProgress === 100 ? 'success' : ''"
        />
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

    // 新增：存储用户选择的文件（原始或压缩后的 File）
    const selectedFile = ref<File | null>(null)

    // 新增：上传进度相关状态
    const uploadProgress = ref<number>(0)
    const uploading = ref<boolean>(false)
    // store server-side avatar path
    const serverAvatarRef = ref<string>('')

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

        // 尽量从多个位置获取 id（后端返回字段可能命名不同）
        currentProfileId.value = userInfo.id || userInfo.userId || 0
        console.debug('loadProfile: userInfo', userInfo, '=> profileId', currentProfileId.value)

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

          // prefer client-facing avatar if present (may be proxied /api/profile/... or data: URL)
          let clientAvatar = parsed.avatar || ''
          // normalize '/api/profile/...' -> '/profile/...' to avoid hitting API controller for GET
          if (clientAvatar && clientAvatar.startsWith('/api/profile/')) {
            clientAvatar = clientAvatar.replace(/^\/api/, '')
          }
           // Keep backend path as-is (backend serves /profile/**); do NOT prefix with /api
           if (clientAvatar && clientAvatar.startsWith('/profile/')) {
             // keep as /profile/xxx
           }
           formData.avatar = clientAvatar
          avatarUrl.value = clientAvatar || ''

          // prefer id from parsed profile if available
          currentProfileId.value = parsed.id || currentProfileId.value || 0
        } else {
          // 使用用户信息初始化
          formData.userName = userInfo.userName || userInfo.username || userInfo.account || ''
          formData.email = userInfo.email || ''
          formData.description = userInfo.description || ''
          // normalize userInfo.avatar as well
          let clientAvatar = userInfo.avatar || ''
          if (clientAvatar && clientAvatar.startsWith('/api/profile/')) {
            clientAvatar = clientAvatar.replace(/^\/api/, '')
          }
           // keep as /profile/xxx if backend path
           if (clientAvatar && clientAvatar.startsWith('/profile/')) {
             // keep as-is
           }
           formData.avatar = clientAvatar
           avatarUrl.value = clientAvatar || ''
           currentProfileId.value = currentProfileId.value || userInfo.id || 0
        }

        console.debug('Initialized formData, avatarUrl=', avatarUrl.value, 'currentProfileId=', currentProfileId.value)
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

    // preload image helper for verifying server-saved avatar (client-facing URL)
    const preloadImage = (url: string): Promise<void> => {
      return new Promise((resolve, reject) => {
        try {
          const img = new Image()
          img.onload = () => resolve()
          img.onerror = () => reject(new Error('image load error'))
          // Normalize URL: if someone passed '/api/profile/...' convert to '/profile/...' so
          // the browser GET goes to the static file handler instead of the API controller
          let normalizedUrl = url
          try {
            if (normalizedUrl.startsWith('/api/profile/')) {
              normalizedUrl = normalizedUrl.replace(/^\/api/, '')
            }
          } catch (e) {
            // ignore
          }
          img.src = normalizedUrl
        } catch (e) {
          reject(e)
        }
      })
    }

    // helper: dataURL -> File
    const dataURLtoFile = (dataurl: string, filename = 'avatar.jpg') => {
      const arr = dataurl.split(',')
      const mimeMatch = arr[0].match(/:(.*?);/)
      const mime = mimeMatch ? mimeMatch[1] : 'image/jpeg'
      const bstr = atob(arr[1])
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      return new File([u8arr], filename, { type: mime })
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
          // 预览使用压缩后的 base64
          // Reduce preview size by using the compressed dataUrl
          avatarUrl.value = compressed
          formData.avatar = compressed
          // 将压缩后的 dataURL 转为 File，存储以便后续上传
          try {
            selectedFile.value = dataURLtoFile(compressed, file.name)
          } catch {
            // 如果转换失败，回退为原始文件（仍可上传)
            selectedFile.value = file
          }

          // 立即派发事件，侧边栏可以实时显示预览头像
          window.dispatchEvent(new CustomEvent('userProfileUpdated', {
            detail: {
              avatar: avatarUrl.value,
              userName: formData.userName,
              account: formData.account
            }
          }))

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
        // reduce preview size by showing a scaled-down thumbnail (data URL) if desired
        avatarUrl.value = dataUrl
        formData.avatar = dataUrl
        // 保存原始文件以便上传
        selectedFile.value = file
        // 立即派发事件更新侧边栏预览
        window.dispatchEvent(new CustomEvent('userProfileUpdated', {
          detail: {
            avatar: avatarUrl.value,
            userName: formData.userName,
            account: formData.account
          }
        }))
        ElMessage.success('头像已选择（并预览）。请点击保存以最��保存配置。')
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
        // 如果用户选中了文件（原始或压缩），先上传文件获取 URL
        if (selectedFile.value) {
          try {
            uploading.value = true
            uploadProgress.value = 0
            console.debug('Uploading avatar file...', selectedFile.value)
            const uploadRes = await profileAPI.uploadAvatar(selectedFile.value, (p) => {
              uploadProgress.value = p
            })
            console.debug('uploadAvatar response:', uploadRes)
            if (uploadRes && uploadRes.code === 20000) {
              const serverAvatar = uploadRes.data || ''
              serverAvatarRef.value = serverAvatar

              // client-facing avatar: use backend static path '/profile/...' (dev proxy maps '/profile')
              let clientAvatar = serverAvatar
              // normalize '/api/profile/...' -> '/profile/...' to avoid hitting API controller for GET
              if (clientAvatar && clientAvatar.startsWith('/api/profile/')) {
                clientAvatar = clientAvatar.replace(/^\/api/, '')
              }
              // append cache-busting timestamp so refresh will load the new image
              clientAvatar = `${clientAvatar}?t=${Date.now()}`

              try {
                // ensure preloadImage uses normalized URL internally
                await preloadImage(clientAvatar)
              } catch (e) {
                // server did not save or cannot serve the image yet
                console.error('Avatar not available at', clientAvatar, e)
                ElMessage.error('头像上传成功，但服务器未能保存或无法访问图片，请稍后重试')
                uploading.value = false
                uploadProgress.value = 0
                saving.value = false
                return
              }

              formData.avatar = clientAvatar
              avatarUrl.value = clientAvatar
              selectedFile.value = null
            } else {
              ElMessage.error(uploadRes.message || '头像上传失败')
              uploading.value = false
              uploadProgress.value = 0
              saving.value = false
              return
            }
          } catch (e) {
            uploading.value = false
            uploadProgress.value = 0
            handleApiError(e, '头像上传失败')
            saving.value = false
            return
          } finally {
            uploading.value = false
          }
         }

         // Determine server-side avatar to send in payload.
         let serverAvatarToSend = serverAvatarRef.value || ''
         if (!serverAvatarToSend) {
           if (formData.avatar && (formData.avatar.startsWith('/api/profile/') || formData.avatar.startsWith('/profile/'))) {
             serverAvatarToSend = formData.avatar.replace(/^\/api/, '').split('?')[0]
           } else if (formData.avatar) {
             serverAvatarToSend = formData.avatar.split('?')[0]
           }
         }

         const profileData: Profile = {
           userName: formData.userName,
           description: formData.description,
           email: formData.email,
           avatar: serverAvatarToSend,
           isDeleted: false
         }

         console.debug('Sending updateProfile payload:', currentProfileId.value, profileData)

         const response = await profileAPI.updateProfile(currentProfileId.value, profileData)
         console.debug('updateProfile response:', response)

         if (response && response.code === 20000) {
           uploadProgress.value = 0

           // Use server-returned profile if available to ensure we store the authoritative id/path
           const returnedProfile = (response as any).data || null
           let savedId = currentProfileId.value
           if (returnedProfile && (returnedProfile.id || returnedProfile.avatar)) {
             savedId = returnedProfile.id || savedId
           }

           // Ensure we persist a client-facing avatar (proxied) for UI use
           let clientFacing = formData.avatar || ''
           if (!clientFacing && returnedProfile && returnedProfile.avatar) {
             // keep returnedProfile.avatar as backend path (/profile/...), don't force /api prefix
             clientFacing = returnedProfile.avatar
           }
           // normalize '/api/profile/...' -> '/profile/...' to avoid hitting API controller for GET
           if (clientFacing && clientFacing.startsWith('/api/profile/')) {
             clientFacing = clientFacing.replace(/^\/api/, '')
           }
           if (clientFacing && !clientFacing.includes('?')) clientFacing = `${clientFacing}?t=${Date.now()}`

           const toSave = {
             id: savedId,
             account: formData.account,
             userName: formData.userName,
             description: formData.description,
             email: formData.email,
             avatar: clientFacing
           }

           try {
             sessionStorage.setItem('userProfile', JSON.stringify(toSave))

             const userInfo = {
               id: savedId,
               account: formData.account,
               userName: formData.userName,
               email: formData.email,
               avatar: clientFacing
             }
             sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
           } catch (e) {
             console.warn('写入 sessionStorage 失败', e)
           }

           window.dispatchEvent(new CustomEvent('userProfileUpdated', { detail: toSave }))
           window.dispatchEvent(new Event('userInfoUpdated'))
           ElMessage.success('个人资料更新成功')
         } else {
           ElMessage.error(response.message || '更新失败')
         }
      } catch (e) {
        handleApiError(e, '保存失败')
      } finally {
        saving.value = false
      }
    }

    const resetProfile = () => {
      Object.assign(formData, defaultFormData)
      avatarUrl.value = ''
      selectedFile.value = null
    }

    const logout = () => {
      try {
        sessionStorage.removeItem('userInfo')
        sessionStorage.removeItem('userProfile')
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch {
        ElMessage.error('退出登录失败')
      }
    }

    return {
      formData,
      avatarUrl,
      saving,
      uploading,
      uploadProgress,
      beforeAvatarUpload,
      saveProfile,
      resetProfile,
      logout
    }
  }
})
</script>

<style scoped>
.user-page {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.user-card {
  width: 100%;
  max-width: 800px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

/* uploader container: center the preview and allow fixed preview size */
.avatar-uploader {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}

.avatar-preview {
  /* fixed preview box: 200x200, rounded corners */
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background-color: #f5f5f5;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #999;
  font-size: 14px;
}

.avatar-hint {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.avatar-progress {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
}

.profile-section {
  padding: 20px;
}

.user-form-item {
  margin-bottom: 16px;
}

.user-form-input {
  width: 100%;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
