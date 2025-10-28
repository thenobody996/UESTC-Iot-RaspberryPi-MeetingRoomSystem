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
  getAllUsers: (): Promise<BaseResponse<User[]>> => {
    return request.get<User[]>('/user/')
  },

  // 删除用户
  deleteUser: (id: number): Promise<BaseResponse<User>> => {
    return request.delete<User>(`/user/delete/${id}`)
  }
}
