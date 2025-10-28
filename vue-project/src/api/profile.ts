// src/api/profile.ts
import { request } from '@/utils/request'
import type { Profile, BaseResponse } from '@/types/api'

export const profileAPI = {
  // 更新个人资料
  updateProfile: (id: number, data: Profile): Promise<BaseResponse<Profile>> => {
    return request.put<Profile>(`/profile/${id}`, data)
  }
}
