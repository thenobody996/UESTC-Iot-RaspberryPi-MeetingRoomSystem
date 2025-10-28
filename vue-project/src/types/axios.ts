// src/types/axios.ts
import type { AxiosError } from 'axios'

export interface CustomAxiosError extends Omit<AxiosError, 'response'> {
  response?: {
    status?: number
    statusText?: string
    data?: {
      message?: string
      code?: number
    }
  }
}

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}
