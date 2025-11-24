<script setup lang="ts">
defineOptions({ name: 'AppNavbar' })
import { useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'

const router = useRouter()
// 默认头像（可替换为项目中的图片），使用相对 public 路径
const defaultAvatar = '/favicon.ico'
const avatarSrc = ref<string>(defaultAvatar)
const displayName = ref<string>('')
const isAdmin = ref<boolean>(false)

const go = (path: string) => {
  router.push(path)
}

const isDataOrRemote = (avatar?: string | null) => {
  if (!avatar) return false
  return avatar.startsWith('data:') || avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('blob:')
}

// Normalize avatar URLs produced by backend or proxies.
// - If the backend returns a full URL that contains a "/api/profile/..." path, prefer the raw "/profile/..." path (no /api) which the backend serves as static files.
// - If the avatar starts with /api/, drop the /api prefix.
// - If it's an absolute URL without /api, keep it as-is.
const sanitizeAvatar = (avatar?: string | null): string | null => {
  if (!avatar) return null
  try {
    // If it's a full URL, use URL to extract pathname
    if (/^https?:\/\//i.test(avatar)) {
      const u = new URL(avatar)
      // if path starts with /api/, strip only the leading "/api"
      if (u.pathname.startsWith('/api/')) {
        return u.pathname + (u.search || '')
      }
      // otherwise return the full URL (remote images)
      return avatar
    }

    // If it's an absolute path starting with /api/, remove the /api prefix
    if (avatar.startsWith('/api/')) return avatar.replace(/^\/api/, '')

    // Otherwise return as-is (relative path or already /profile/...)
    return avatar
  } catch {
    // if URL parsing fails, just return original
    return avatar
  }
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
    // generic api path: try raw then as-is
    candidates.push(base.replace(/^\/api/, ''))
    candidates.push(avatar)
    candidates.push(base)
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

  // sanitize avatar: remove /api prefix when pointing to profile static files
  const candidate = sanitizeAvatar(avatar)

  // if candidate is falsy after sanitize, fallback
  if (!candidate) {
    avatarSrc.value = defaultAvatar
    return
  }

  // if candidate is remote (full http URL) but points to /api/profile, sanitizeAvatar returned pathname
  // if candidate is still a full url to a remote host, try to load it directly
  if (isDataOrRemote(candidate)) {
    try {
      await preloadImage(candidate)
      avatarSrc.value = candidate
      return
    } catch {
      console.warn('Navbar: data/remote avatar failed to load, falling back')
      avatarSrc.value = defaultAvatar
      return
    }
  }

  const candidates = variantsFor(candidate)
  for (const c of candidates) {
    try {
      // try to load candidate
      const tryUrl = c
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
      // normalize avatar and set
      void safeSetAvatar(sanitizeAvatar(parsed.avatar ?? parsed.avatarUrl ?? parsed.avatar_url ?? parsed.avatarPath ?? null))
      // prefer userName, then nickname/username/account
      displayName.value = parsed.userName || parsed.nickname || parsed.username || parsed.account || ''
      // determine admin role from either profile or userInfo; support several common shapes
      const roleIndicator = parsed.role || parsed.authority || parsed.authorities || parsed.roles || parsed.user?.role || parsed.user?.roles || parsed.user?.authorities || ''
      // roleIndicator may be a string or an array
      isAdmin.value = isRoleAdmin(roleIndicator)
    } else {
      avatarSrc.value = defaultAvatar
      displayName.value = ''
      isAdmin.value = false
    }
  } catch (e) {
    console.warn('Navbar: 读取 sessionStorage 失败', e)
    avatarSrc.value = defaultAvatar
    displayName.value = ''
    isAdmin.value = false
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
      isAdmin.value = false
      return
    }
    // sanitize avatar from event detail as well
    void safeSetAvatar(sanitizeAvatar(detail.avatar ?? detail.avatarUrl ?? null))
    displayName.value = detail.userName || detail.nickname || detail.username || detail.account || ''
    // detail may include role in several shapes
    const roleIndicator = detail.role || detail.authority || detail.authorities || detail.roles || detail.user?.role || detail.user?.roles || ''
    isAdmin.value = isRoleAdmin(roleIndicator)
  } catch (err) {
    console.warn('Navbar: userProfileUpdated 事件处理失败', err)
  }
}

const onStorage = (e: StorageEvent) => {
  if (e.key === 'userProfile' || e.key === 'userInfo') {
    if (e.newValue) {
      try {
        const parsed = JSON.parse(e.newValue)
        void safeSetAvatar(sanitizeAvatar(parsed.avatar ?? parsed.avatarUrl ?? null))
        displayName.value = parsed.userName || parsed.nickname || parsed.username || parsed.account || ''
        const roleIndicator = parsed.role || parsed.authority || parsed.authorities || parsed.roles || parsed.user?.role || parsed.user?.roles || ''
        isAdmin.value = isRoleAdmin(roleIndicator)
      } catch (err) {
        console.warn('Navbar: storage 事件解析失败', err)
      }
    } else {
      avatarSrc.value = defaultAvatar
      displayName.value = ''
      isAdmin.value = false
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

// click helpers to avoid template attribute parsing warnings
const goMeeting = () => go('/meeting')
const goAdminMeetingroom = () => go('/admin/meetingroom')
const goAdminUser = () => go('/admin/user')
const goUser = () => go('/user')

// helper to detect admin role in various shapes
const isRoleAdmin = (roleIndicator: unknown): boolean => {
  if (!roleIndicator) return false
  const regex = /(^|\W)(admin|ROLE_ADMIN)(\W|$)/i
  if (typeof roleIndicator === 'string') return regex.test(roleIndicator)
  if (Array.isArray(roleIndicator)) {
    for (const item of roleIndicator as unknown[]) {
      if (!item) continue
      if (typeof item === 'string' && regex.test(item)) return true
      if (typeof item === 'object') {
        const obj = item as Record<string, unknown>
        const v = String(obj.authority ?? obj.role ?? obj.name ?? '')
        if (regex.test(v)) return true
      }
    }
    return false
  }
  if (typeof roleIndicator === 'object') {
    const obj = roleIndicator as Record<string, unknown>
    const v = String(obj.authority ?? obj.role ?? obj.name ?? '')
    return regex.test(v)
  }
  return false
}
</script>

<template>
  <nav class="sidebar" aria-label="主侧边导航">
    <div class="avatar-wrapper" @click.prevent="goUser" role="button" aria-label="个人信息">
      <img class="avatar" :src="avatarSrc" alt="avatar" @error="onAvatarError" />
      <div class="avatar-name" v-if="displayName">{{ displayName }}</div>
    </div>

    <ul class="nav-list">
      <li class="nav-item">
        <button type="button" class="nav-btn" @click.prevent="goMeeting" aria-label="会议">会议</button>
      </li>
      <!-- 管理入口，仅管理员可见 -->
      <li v-if="isAdmin" class="nav-item">
        <button type="button" class="nav-btn" @click.prevent="goAdminMeetingroom">会议室管理</button>
      </li>
      <li v-if="isAdmin" class="nav-item">
        <button type="button" class="nav-btn" @click.prevent="goAdminUser">用户管理</button>
      </li>
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
  padding: 0; /* reset since button will provide padding */
}

.nav-btn {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  text-align: center;
  cursor: pointer;
  user-select: none;
  background: transparent;
  border: none;
  transition: background-color 0.18s ease, transform 0.12s ease;
}

.nav-btn:hover {
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
