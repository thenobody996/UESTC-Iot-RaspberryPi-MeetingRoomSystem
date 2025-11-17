<script setup lang="ts">
defineOptions({ name: 'AppNavbar' })
import { useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'

const router = useRouter()
// 默认头像（可替换为项目中的图片），使用相对 public 路径
const defaultAvatar = '/favicon.ico'
const avatarSrc = ref<string>(defaultAvatar)
const displayName = ref<string>('')

const backendPort = 8088 // 如果后端端口不是 8088，请修改

const go = (path: string) => {
  router.push(path)
}

const isDataOrRemote = (avatar?: string | null) => {
  if (!avatar) return false
  return avatar.startsWith('data:') || avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('blob:')
}

const variantsFor = (avatar?: string | null) => {
  // produce candidate URLs to try loading, in order of preference
  if (!avatar) return [defaultAvatar]
  if (isDataOrRemote(avatar)) return [avatar]

  const candidates: string[] = []
  // strip query string for base
  const base = avatar.split('?')[0]

  // if backend path like /profile/xxx
  if (base.startsWith('/profile/')) {
    // try raw backend path first. Do NOT attempt /api prefix for static profile files (avoids 405)
    candidates.push(base)
  } else if (base.startsWith('/api/profile/')) {
    // input is proxied form '/api/profile/...'. Prefer the raw backend path '/profile/...' (keep query)
    const raw = avatar.replace(/^\/api/, '')
    candidates.push(raw)
    candidates.push(raw.split('?')[0])
    // as last resort include the original proxied avatar (may be used in prod setups)
    candidates.push(avatar)
  } else if (base.startsWith('/api/')) {
    // generic api path: try as-is then raw
    candidates.push(avatar)
    candidates.push(base)
    candidates.push(base.replace(/^\/api/, ''))
  } else if (base.startsWith('/')) {
    // other absolute path
    candidates.push(avatar)
    candidates.push(base)
    candidates.push(`/api${base}`)
  } else {
    // relative or unknown - try as given and prepend /api/
    candidates.push(avatar)
    candidates.push(`/api/${avatar}`)
    candidates.push(`/${avatar}`)
  }

  // also include original avatar with query if provided (cache-busting)
  if (avatar.includes('?') && !candidates.includes(avatar)) candidates.unshift(avatar)

  // dedupe while preserving order
  return Array.from(new Set(candidates))
}

// preload 图像，成功后 resolve，否则 reject
const preloadImage = (url: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    try {
      const img = new Image()
      // 不设置 crossOrigin，避免引起 CORS 要求；仅用于显示
      img.onload = () => resolve()
      img.onerror = () => reject(new Error('image load error'))
      img.src = url
    } catch (e) {
      reject(e)
    }
  })
}

const safeSetAvatar = async (avatar?: string | null) => {
  if (!avatar) {
    avatarSrc.value = defaultAvatar
    return
  }

  if (isDataOrRemote(avatar)) {
    try {
      await preloadImage(avatar)
      avatarSrc.value = avatar
      return
    } catch {
      console.warn('Navbar: data/remote avatar failed to load, falling back')
      avatarSrc.value = defaultAvatar
      return
    }
  }

  const candidates = variantsFor(avatar)
  for (const c of candidates) {
    try {
      // try to load candidate
      const tryUrl = c
      // eslint-disable-next-line no-await-in-loop
      await preloadImage(tryUrl)
      avatarSrc.value = tryUrl
      return
    } catch {
      // continue to next candidate
    }
  }

  // all attempts failed, fallback
  avatarSrc.value = defaultAvatar
}

const loadFromSession = () => {
  try {
    const raw = sessionStorage.getItem('userProfile') || sessionStorage.getItem('userInfo')
    if (raw) {
      const parsed = JSON.parse(raw)
      void safeSetAvatar(parsed.avatar)
      // prefer userName, then nickname/username/account
      displayName.value = parsed.userName || parsed.nickname || parsed.username || parsed.account || ''
    } else {
      avatarSrc.value = defaultAvatar
      displayName.value = ''
    }
  } catch (e) {
    console.warn('Navbar: 读取 sessionStorage 失败', e)
    avatarSrc.value = defaultAvatar
    displayName.value = ''
  }
}

const onUserProfileUpdated = (event: Event) => {
  // CustomEvent with detail may be used
  try {
    const ce = event as CustomEvent
    const detail = ce?.detail
    if (!detail) {
      avatarSrc.value = defaultAvatar
      displayName.value = ''
      return
    }
    void safeSetAvatar(detail.avatar)
    displayName.value = detail.userName || detail.nickname || detail.username || detail.account || ''
  } catch (err) {
    console.warn('Navbar: userProfileUpdated 事件处理失败', err)
  }
}

const onStorage = (e: StorageEvent) => {
  if (e.key === 'userProfile' || e.key === 'userInfo') {
    if (e.newValue) {
      try {
        const parsed = JSON.parse(e.newValue)
        void safeSetAvatar(parsed.avatar)
        displayName.value = parsed.userName || parsed.nickname || parsed.username || parsed.account || ''
      } catch (err) {
        console.warn('Navbar: storage 事件解析失败', err)
      }
    } else {
      avatarSrc.value = defaultAvatar
      displayName.value = ''
    }
  }
}

const onAvatarError = (ev?: Event) => {
  // fallback if somehow <img> error triggers
  console.warn('Navbar: avatar load error', ev)
  avatarSrc.value = defaultAvatar
}

onMounted(() => {
  loadFromSession()
  window.addEventListener('userProfileUpdated', onUserProfileUpdated as EventListener)
  window.addEventListener('storage', onStorage)
})

onUnmounted(() => {
  window.removeEventListener('userProfileUpdated', onUserProfileUpdated as EventListener)
  window.removeEventListener('storage', onStorage)
})
</script>

<template>
  <nav class="sidebar" aria-label="主侧边导航">
    <div class="avatar-wrapper" @click.prevent="go('/user')" role="button" tabindex="0" aria-label="个人信息">
      <img class="avatar" :src="avatarSrc" alt="avatar" @error="onAvatarError" />
      <div class="avatar-name" v-if="displayName">{{ displayName }}</div>
    </div>

    <ul class="nav-list">
      <li class="nav-item" @click.prevent="go('/meeting')" role="button" tabindex="0" aria-label="会议">会议</li>
      <!-- 未来导航项占位 -->
      <li class="nav-item placeholder">更多</li>
    </ul>
  </nav>
</template>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  width: 220px; /* 可调整宽度 */
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-sizing: border-box;
  z-index: 1000;
  overflow: hidden; /* 不显示横向滚动条 */
}

.avatar-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: 8px;
  margin-bottom: 18px;
  cursor: pointer;
}

.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255,255,255,0.15);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.avatar-name {
  margin-top: 8px;
  font-size: 14px;
  color: rgba(255,255,255,0.95);
  text-align: center;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  text-align: center;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.18s ease, transform 0.12s ease;
}

.nav-item:hover {
  background: rgba(255,255,255,0.08);
  transform: translateY(-1px);
}

.nav-item.placeholder {
  opacity: 0.6;
  cursor: default;
}

/* 小屏幕时收窄侧栏但保持功能 */
@media (max-width: 768px) {
  .sidebar {
    width: 72px;
    padding: 12px 8px;
  }

  .avatar {
    width: 56px;
    height: 56px;
  }

  .avatar-name {
    display: none;
  }

  .nav-item {
    font-size: 13px;
    padding: 8px 6px;
  }
}
</style>
