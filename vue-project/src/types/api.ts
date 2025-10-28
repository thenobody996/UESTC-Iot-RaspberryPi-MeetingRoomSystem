// src/types/api.ts
// 基础响应类型
export interface BaseResponse<T = unknown> {
  code: number
  message: string
  data: T
}

// 用户相关类型
export interface User {
  id: number
  account: string
  password: string
  role: string
  createdAt: string
  updatedAt: string
  deleted: boolean
}

export interface LoginRequest {
  account: string
  password: string
}

// 个人资料相关类型
export interface Profile {
  id?: number
  userName: string
  description?: string
  email: string
  avatar?: string
  user?: User
  isDeleted: boolean
}

// 分页参数
export interface PageParams {
  page?: number
  pageSize?: number
  [key: string]: unknown
}

// 分页响应
export interface PageResponse<T = unknown> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
