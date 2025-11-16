// src/api/profile.ts
import { request } from '@/utils/request'
import type { Profile, BaseResponse } from '@/types/api'

export const profileAPI = {
  // 更新个人资料
  updateProfile: (id: number, data: Profile): Promise<BaseResponse<Profile>> => {
    return request.put<Profile>(`/profile/${id}`, data)
  },

  // 上传头像图片，返回图片访问 URL
  // 使用 multipart/form-data 上传头像，并可选传入进度回调 onProgress(percentage: number)
  // 后端返回 BaseResponse<string>，string 为图片 URL
  uploadAvatar: (file: File, onProgress?: (percentage: number) => void): Promise<BaseResponse<string>> => {
    const form = new FormData()
    form.append('avatar', file)

    return request.post<string>('/profile/uploadAvatar', form, {
      // axios 的 onUploadProgress 可以收到进度事件
      onUploadProgress: (ev: ProgressEvent) => {
        try {
          if (ev.total && onProgress) {
            const percent = Math.round((ev.loaded / ev.total) * 100)
            onProgress(percent)
          }
        } catch (e) {
          // ignore progress errors
        }
      }
    })
  }
}
