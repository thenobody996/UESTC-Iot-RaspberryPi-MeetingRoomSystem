// src/types/axios.d.ts
import 'axios'

declare module 'axios' {
  export interface AxiosResponse<T = unknown> {
    code: number
    message: string
    data: T
    success?: boolean
  }
}
