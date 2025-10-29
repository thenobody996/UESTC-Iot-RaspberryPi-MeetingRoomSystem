// src/utils/request.ts - 最保守的方案
import axios, {
  type AxiosInstance,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  type AxiosResponse,
  type InternalAxiosRequestConfig,
  type AxiosRequestConfig
} from 'axios'
import type { BaseResponse } from '@/types/api'

const service: AxiosInstance = axios.create({
  baseURL: '/api', // 添加基础URL
  timeout: 15000,
  withCredentials: false
})

// 请求拦截器 - 完全不修改 headers，在请求时单独设置
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    console.log('🚀 发送请求:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      data: config.data
    })

    // 不在这里设置 headers，避免类型问题
    return config
  },
  (error: unknown) => Promise.reject(error)
)

// 响应拦截器保持不变...

// 封装的请求函数 - 在每次请求时单独设置 headers
export const request = {
  get: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<BaseResponse<T>> =>
    service.get(url, {
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config?.headers
      }
    }).then(res => res.data as BaseResponse<T>),

  post: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<BaseResponse<T>> =>
    service.post(url, data, {
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config?.headers
      }
    }).then(res => res.data as BaseResponse<T>),

  put: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<BaseResponse<T>> =>
    service.put(url, data, {
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config?.headers
      }
    }).then(res => res.data as BaseResponse<T>),

  delete: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<BaseResponse<T>> =>
    service.delete(url, {
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config?.headers
      }
    }).then(res => res.data as BaseResponse<T>),

  patch: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<BaseResponse<T>> =>
    service.patch(url, data, {
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config?.headers
      }
    }).then(res => res.data as BaseResponse<T>)
}

export default service
