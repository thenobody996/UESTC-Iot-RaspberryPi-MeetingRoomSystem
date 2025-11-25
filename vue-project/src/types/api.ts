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

// 新增会议室相关类型（与后端 OpenAPI 对齐）
export interface MeetingRoom {
  id: number
  name: string
  manager?: User
  volume?: number
  description?: string
  locateURL?: string
  createAt?: string
  updateAt?: string
  isDeleted?: boolean
}

export interface MeetingRoomRequest {
  name?: string
  manager_id?: number
  volume?: number
  description?: string
  locateURL?: string
}

// 会议相关类型（根据后端 OpenAPI）
export interface Meeting {
  id?: number
  hoster?: User
  place?: MeetingRoom
  title?: string
  description?: string
  startTime?: string
  endTime?: string
  create_At?: string
  update_At?: string
  members?: User[]
  deleted?: boolean
}

export interface MeetingRequest {
  host_id?: number
  place_id?: number
  title?: string
  description?: string
  start_time?: string
  end_time?: string
  members_id?: number[]
}
