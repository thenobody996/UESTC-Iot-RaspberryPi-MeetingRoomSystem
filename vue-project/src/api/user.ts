// src/api/user.ts
import { request } from '@/utils/request'
import type {
  User,
  LoginRequest,
  BaseResponse
} from '@/types/api'

export const userAPI = {
  // 用户注册
  register: (data: LoginRequest): Promise<BaseResponse<User>> => {
    return request.post<User>('/user/register', data)
  },

  // 用户登录
  login: (data: LoginRequest): Promise<BaseResponse<User>> => {
    return request.post<User>('/user/login', data)
  },

  // 根据ID获取用户
  getUserById: (id: number): Promise<BaseResponse<User>> => {
    return request.get<User>(`/user/${id}`)
  },

  // 根据账户名搜索用户
  searchUserByAccount: (account: string): Promise<BaseResponse<User[]>> => {
    return request.get<User[]>(`/user/search/${account}`)
  },

  // 获取所有用户
  getAllUsers: (page?: number, size?: number): Promise<BaseResponse<User[]>> => {
    const params: Record<string, number> = {}
    if (typeof page === 'number') params.page = page
    if (typeof size === 'number') params.size = size
    return request.get<User[]>('/user/', { params: Object.keys(params).length ? params : undefined })
  },

  // 删除用户
  deleteUser: (id: number): Promise<BaseResponse<User>> => {
    return request.delete<User>(`/user/delete/${id}`)
  },

  // 获取用户加入的会议
  getJoinedMeetings: (userId: number) => request.get(`/user/joinedmeetings/${userId}`)
}
