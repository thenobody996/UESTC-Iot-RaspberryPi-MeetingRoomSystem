// src/api/meetingroom.ts
import { request } from '@/utils/request'
import type { MeetingRoom, MeetingRoomRequest, BaseResponse } from '../types/api'

export const meetingRoomAPI = {
  // 创建新会议室 -> 后端返回 ResultMeetingRoom { code,message,data:MeetingRoom }
  createRoom: (data: MeetingRoomRequest): Promise<BaseResponse<MeetingRoom>> => {
    return request.post<MeetingRoom>('/meetingroom/', data)
  },

  // 根据ID查询会议室 -> 返回 MeetingRoom 包装在 BaseResponse.data
  getRoomById: (id: number): Promise<BaseResponse<MeetingRoom>> => {
    return request.get<MeetingRoom>(`/meetingroom/${id}`)
  },

  // 更新会议室信息 -> 返回 MeetingRoom 包装在 BaseResponse.data
  updateRoom: (roomId: number, data: MeetingRoomRequest): Promise<BaseResponse<MeetingRoom>> => {
    return request.put<MeetingRoom>(`/meetingroom/update/${roomId}`, data)
  },

  // 删除会议室 -> 文档返回 object 包装在 BaseResponse.data
  deleteRoom: (roomId: number): Promise<BaseResponse<object>> => {
    return request.delete<object>(`/meetingroom/delete/${roomId}`)
  },

  // 按名称模糊查询（可选分页） -> 文档为 object（分页结果），包装在 BaseResponse.data
  searchRooms: (name: string, page?: number, size?: number): Promise<BaseResponse<object>> => {
    const params: Record<string, number> = {}
    if (typeof page === 'number') params.page = page
    if (typeof size === 'number') params.size = size
    return request.get<object>(`/meetingroom/search/${encodeURIComponent(name)}`, {
      params: Object.keys(params).length ? params : undefined
    })
  },

  // 获取所有会议室（分页） -> 文档为 object（分页结果），包装在 BaseResponse.data
  getAllRooms: async (page?: number, size?: number): Promise<BaseResponse<object>> => {
    const params: Record<string, number> = {}
    if (typeof page === 'number') params.page = page
    if (typeof size === 'number') params.size = size

    // Some backends return list only via the search endpoint when called with a space (%20).
    // Try that first to avoid hitting endpoints that may return 500/405/404.
    const endpoints = [
      '/meetingroom/search/%20',
      '/meetingroom/allrooms',
      '/meetingroom',
      '/meetingroom/'
    ]

    let lastErrorObj: unknown = null
    for (const ep of endpoints) {
      try {
        // use request.get which unwraps res.data
        // if this request succeeds, return its response directly
        return await request.get<object>(ep, { params: Object.keys(params).length ? params : undefined })
      } catch (err) {
        // record and continue to next fallback
        console.warn(`meetingRoomAPI.getAllRooms: endpoint ${ep} failed, trying next.`, err)
        lastErrorObj = err
        // small delay between attempts to avoid hammering server
        await new Promise(resolve => setTimeout(resolve, 80))
      }
    }

    // Log the last error to make debugging easier for developers
    // eslint-disable-next-line no-console
    console.error('meetingRoomAPI.getAllRooms: all endpoints failed, last error:', lastErrorObj)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return Promise.resolve({ code: -1, message: 'all endpoints failed', data: { content: [], totalElements: 0, number: 0, size: params.size ?? 10 } } as any)
  }
}
