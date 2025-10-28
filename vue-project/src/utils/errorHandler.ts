// src/utils/errorHandler.ts
import { ElMessage } from 'element-plus'
import type { BaseResponse } from '@/types/api'

// 完整的错误对象类型
interface ApiError {
  message?: string
  code?: string | number
  config?: {
    url?: string
    method?: string
    baseURL?: string
  }
  response?: {
    status?: number
    statusText?: string
    data?: {
      message?: string
      code?: number
      errors?: Record<string, string[]>
    }
  }
  request?: unknown
}

export const handleApiError = (error: unknown, defaultMessage = '操作失败'): void => {
  let message = defaultMessage

  if (error && typeof error === 'object') {
    const apiError = error as ApiError

    // 优先使用后端返回的错误消息
    if (apiError.response?.data?.message) {
      message = apiError.response.data.message
    }
    // 其次使用 HTTP 状态码对应的消息
    else if (apiError.response?.status) {
      message = getHttpStatusMessage(apiError.response.status)
    }
    // 最后使用错误对象的 message
    else if (apiError.message) {
      message = apiError.message
    }

    // 记录错误详情（开发环境）
    if (import.meta.env.DEV) {
      console.error('API 错误详情:', {
        message: apiError.message,
        status: apiError.response?.status,
        url: apiError.config?.url,
        method: apiError.config?.method,
        responseData: apiError.response?.data
      })
    }
  }

  ElMessage.error(message)
}

// HTTP 状态码对应的错误消息
const getHttpStatusMessage = (status: number): string => {
  const messages: Record<number, string> = {
    400: '请求参数错误',
    401: '未授权，请重新登录',
    403: '访问被拒绝',
    404: '请求的资源不存在',
    405: '请求方法不被允许',
    408: '请求超时',
    409: '资源冲突',
    422: '请求参数验证失败',
    429: '请求过于频繁',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务不可用',
    504: '网关超时'
  }

  return messages[status] || `请求失败 (${status})`
}

export const isSuccess = (response: BaseResponse): boolean => {
  return response.code === 200
}

// 额外的工具函数
export const getErrorMessage = (error: unknown): string => {
  if (error && typeof error === 'object') {
    const apiError = error as ApiError
    return apiError.response?.data?.message || apiError.message || '未知错误'
  }
  return '未知错误'
}

export const isNetworkError = (error: unknown): boolean => {
  if (error && typeof error === 'object') {
    const apiError = error as ApiError
    return !apiError.response && !!apiError.request
  }
  return false
}
