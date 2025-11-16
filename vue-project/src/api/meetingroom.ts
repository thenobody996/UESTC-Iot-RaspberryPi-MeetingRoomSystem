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
    return request.get<object>(`/meetingroom/search/${encodeURIComponent(name)}`, {
      params: { page, size }
    })
  },

  // 获取所有会议室（分页） -> 文档为 object（分页结果），包装在 BaseResponse.data
  getAllRooms: (page?: number, size?: number): Promise<BaseResponse<object>> => {
    return request.get<object>('/meetingroom/allrooms', {
      params: { page, size }
    })
  }
}
