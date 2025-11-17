// src/api/profile.ts
import axios from 'axios'
import { request } from '@/utils/request'
import type { Profile, BaseResponse } from '@/types/api'
import type { AxiosProgressEvent } from 'axios'

const BACKEND_ORIGIN = 'http://localhost:8088'

export const profileAPI = {
  // 更新个人资料
  updateProfile: (id: number, data: Profile): Promise<BaseResponse<Profile>> => {
    return request.put<Profile>(`/profile/${id}`, data)
  },

  // 上传头像图片，返回图片访问 URL
  uploadAvatar: async (file: File, onProgress?: (percentage: number) => void): Promise<BaseResponse<string>> => {
    const form = new FormData()
    form.append('avatar', file)

    // helper to normalize response object
    const normalize = (res: unknown): BaseResponse<string> => {
      try {
        const respRecord = res as Record<string, unknown>
        const maybeMsg = typeof respRecord?.message === 'string' ? (respRecord.message as string) : undefined
        const maybeData = typeof respRecord?.data === 'string' ? (respRecord.data as string) : undefined
        if (maybeData || maybeMsg) {
          const raw = maybeData || maybeMsg || ''
          let normalized = raw
          try {
            const parsed = new URL(raw, window.location.href)
            if (parsed.origin === window.location.origin) {
              normalized = parsed.pathname + parsed.search + parsed.hash
            }
          } catch {
            // ignore URL parse errors
          }

          // If backend returned an API-prefixed path like '/api/profile/..', convert to '/profile/..'
          if (normalized.startsWith('/api/profile/')) {
            normalized = normalized.replace(/^\/api/, '')
          }

          if (!normalized.startsWith('/')) normalized = `/${normalized}`
          ;(res as Record<string, unknown>).data = normalized
        }
      } catch {
        // ignore normalization errors
      }
      return (res as unknown) as BaseResponse<string>
    }

    // Try using app request (goes through dev proxy). If 404, fall back to direct backend request.
    try {
      const res = await request.post<string>('/profile/uploadAvatar', form, {
        onUploadProgress: (ev?: AxiosProgressEvent) => {
          try {
            if (ev && ev.total && onProgress) {
              const percent = Math.round((ev.loaded as number / ev.total) * 100)
              onProgress(percent)
            }
          } catch {
            // ignore
          }
        }
      })
      return normalize(res)
    } catch (err: unknown) {
      // If proxy returned 404, try direct backend upload (bypassing dev proxy)
      const status = (() => {
        try {
          const e = err as { response?: { status?: number } }
          return e?.response?.status
        } catch {
          return undefined
        }
      })()

      if (status === 404 || status === undefined) {
        try {
          const res = await axios.post<BaseResponse<string>>(`${BACKEND_ORIGIN}/api/profile/uploadAvatar`, form, {
            headers: {
              // let axios set multipart boundary
            },
            onUploadProgress: (ev?: AxiosProgressEvent) => {
              try {
                if (ev && ev.total && onProgress) {
                  const percent = Math.round((ev.loaded as number / ev.total) * 100)
                  onProgress(percent)
                }
              } catch {
                // ignore
              }
            }
          })
          return normalize(res.data)
        } catch (innerErr: unknown) {
          return Promise.reject(innerErr)
        }
      }
      return Promise.reject(err)
    }
  }
}
