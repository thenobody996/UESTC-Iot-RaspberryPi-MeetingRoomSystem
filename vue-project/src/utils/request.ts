// src/utils/request.ts - 最保守的方案
import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosRequestConfig } from 'axios'
import type { BaseResponse } from '@/types/api'

const service: AxiosInstance = axios.create({
  baseURL: '/api', // 添加基础URL
  timeout: 15000,
  withCredentials: false
})

// 请求拦截器 - 完全不修改 headers，在请求时单独设置
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    try {
      const fullUrl = `${service.defaults.baseURL ?? ''}${config.url ?? ''}`
      console.log('🚀 发送请求:', {
        method: config.method?.toUpperCase(),
        url: fullUrl,
        params: config.params,
        data: config.data
      })
    } catch (e) {
      // swallow logging errors
      // eslint-disable-next-line no-console
      console.warn('request interceptor logging failed', e)
    }

    // 不在这里设置 headers，避免类型问题
    return config
  },
  (error: unknown) => Promise.reject(error)
)

// 响应拦截器 - 打印错误详情，便于排查 5xx/4xx
service.interceptors.response.use(
  (response) => response,
  (error) => {
    try {
      const e = error as unknown as { response?: { status?: number; statusText?: string; data?: unknown }; config?: { url?: string } }
      if (e?.response) {
        console.error('🚨 HTTP Error:', {
          url: `${service.defaults.baseURL ?? ''}${(e.config?.url) ?? ''}`,
          status: e.response.status,
          statusText: e.response.statusText,
          data: e.response.data
        })
      } else {
        console.error('🚨 Network/Error (no response):', e)
      }
    } catch (logErr) {
      console.warn('response interceptor logging failed', logErr)
    }
    return Promise.reject(error)
  }
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

  post: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<BaseResponse<T>> => {
    const headers = (config?.headers as Record<string, string> | undefined) ?? {}

    // 如果没有显式指定 Content-Type，且 data 不是 FormData，则默认 application/json
    if (!headers['Content-Type']) {
      if (data instanceof FormData) {
        // 让 axios 自动设置 multipart/form-data boundary
      } else {
        headers['Content-Type'] = 'application/json'
      }
    }

    return service.post(url, data, {
      ...config,
      headers
    }).then(res => res.data as BaseResponse<T>)
  },

  put: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<BaseResponse<T>> => {
    const headers = (config?.headers as Record<string, string> | undefined) ?? {}

    if (!headers['Content-Type']) {
      if (data instanceof FormData) {
        // allow axios to set boundary
      } else {
        headers['Content-Type'] = 'application/json'
      }
    }

    return service.put(url, data, {
      ...config,
      headers
    }).then(res => res.data as BaseResponse<T>)
  },

  delete: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<BaseResponse<T>> =>
    service.delete(url, {
      ...config,
      headers: {
        'Content-Type': 'application/json',
        ...config?.headers
      }
    }).then(res => res.data as BaseResponse<T>),

  patch: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<BaseResponse<T>> => {
    const headers = (config?.headers as Record<string, string> | undefined) ?? {}

    if (!headers['Content-Type']) {
      if (data instanceof FormData) {
        // allow axios to set boundary
      } else {
        headers['Content-Type'] = 'application/json'
      }
    }

    return service.patch(url, data, {
      ...config,
      headers
    }).then(res => res.data as BaseResponse<T>)
  }
}

export default service
