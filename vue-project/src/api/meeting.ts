import { request } from '@/utils/request'
import type { Meeting, MeetingRequest } from '@/types/api'

export const meetingAPI = {
  // 创建新会议
  createMeeting: (data: MeetingRequest) => request.post<Meeting>('/meeting/', data),

  // 根据id获取会议
  getMeetingById: (id: number) => request.get<Meeting>(`/meeting/${id}`),

  // 更新会议信息（不含成员）
  updateMeeting: (id: number, data: MeetingRequest) => request.put<Meeting>(`/meeting/${id}`, data),

  // 删除会议
  deleteMeeting: (id: number) => request.delete<Meeting>(`/meeting/delete/${id}`),

  // 获取所有会议（分页）
  getAllMeetings: (page: number, size: number) => request.get<object>(`/meeting/allmeetings`, { params: { page, size } }),

  // 添加/删除成员
  addMember: (meetingId: number, memberId: number) => request.post<Meeting>(`/meeting/addmember/${meetingId}`, null, { params: { member: memberId } }),
  removeMember: (meetingId: number, memberId: number) => request.post<Meeting>(`/meeting/removemember/${meetingId}`, null, { params: { member: memberId } }),
  updateMembers: (meetingId: number, members: number[]) => request.patch<Meeting>(`/meeting/updatemembers/${meetingId}`, members)
}

