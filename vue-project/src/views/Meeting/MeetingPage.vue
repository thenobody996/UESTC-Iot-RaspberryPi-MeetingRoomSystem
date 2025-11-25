<template>
  <div class="meeting-page">
    <div class="meeting-container">
      <!-- 左侧功能区域 -->
      <div class="left-panel">
        <div class="grid-container">
          <div class="grid-item" @click="handleJoinMeeting">
            <div class="icon-wrapper">
              <el-icon size="32"><VideoCamera /></el-icon>
            </div>
            <span class="label">加入会议</span>
          </div>

          <div class="grid-item" @click="handleQuickMeeting">
            <div class="icon-wrapper">
              <el-icon size="32"><VideoPlay /></el-icon>
            </div>
            <span class="label">快速会议</span>
          </div>

          <div class="grid-item" @click="handleScheduleMeeting">
            <div class="icon-wrapper">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <span class="label">预约会议</span>
          </div>

          <div class="grid-item" @click="handleMeetingRecords">
            <div class="icon-wrapper">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <span class="label">会议记录</span>
          </div>
        </div>
      </div>

      <!-- 右侧日程区域 -->
      <div class="right-panel">
        <div class="date-section">
          <div class="current-date">
            <div class="date-number">{{ currentDate.day }}</div>
            <div class="date-info">
              <div class="weekday">{{ currentDate.weekday }}</div>
              <div class="full-date">{{ currentDate.fullDate }}</div>
            </div>
          </div>
        </div>

        <div class="schedule-section">
          <div class="schedule-header">
            <h3>今日会议日程</h3>
          </div>

          <div class="schedule-list">
            <div
              v-for="(meeting, index) in displayedMeetings"
              :key="meeting.meetingId ?? meeting.id ?? index"
              class="schedule-item"
              :class="{ active: meeting.isActive }"
            >
              <div class="meeting-time">
                <span class="time">{{ meeting.time }}</span>
                <span class="duration">{{ meeting.duration }}</span>
              </div>
              <div class="meeting-info">
                <div class="meeting-title">{{ meeting.title }}</div>
                <div class="meeting-details">
                  <span class="meeting-id">ID: {{ meeting.meetingId }}</span>
                  <span class="meeting-room">{{ meeting.room }}</span>
                  <span class="meeting-participants">{{ meeting.participants }} 人参加</span>
                  <span v-if="meeting.hasPassword" class="password-tag">有密码</span>
                </div>
              </div>
              <div class="meeting-actions">
                <el-button
                  v-if="meeting.isActive"
                  type="primary"
                  size="small"
                  @click.stop="joinScheduledMeeting(meeting)"
                >
                  加入
                </el-button>
                <el-button
                  v-else
                  type="info"
                  size="small"
                  disabled
                >
                  未开始
                </el-button>
              </div>
            </div>

            <div v-if="displayedMeetings.length === 0" class="empty-schedule">
              <el-icon><Calendar /></el-icon>
              <p>今日暂无会议安排</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加会议日程对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditMode ? '编辑会议日程' : '添加会议日程'"
      width="600px"
      center
    >
      <el-form
        :model="newMeetingForm"
        :rules="meetingRules"
        ref="meetingFormRef"
        label-width="100px"
      >
        <el-form-item label="会议标题" prop="title">
          <el-input
            v-model="newMeetingForm.title"
            placeholder="请输入会议主题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="newMeetingForm.startTime"
            type="datetime"
            placeholder="选择会议开始时间"
            :disabled-date="disabledDate"
            :disabled-time="disabledTime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>

        <el-form-item label="会议时长" prop="durationType">
          <el-radio-group v-model="newMeetingForm.durationType">
            <el-radio label="fixed">固定时长</el-radio>
            <el-radio label="custom">选择结束时间</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="newMeetingForm.durationType === 'fixed'"
          label="选择时长"
          prop="fixedDuration"
        >
          <el-select v-model="newMeetingForm.fixedDuration" placeholder="请选择会议时长">
            <el-option label="15分钟" value="15分钟" />
            <el-option label="30分钟" value="30分钟" />
            <el-option label="45分钟" value="45分钟" />
            <el-option label="1小时" value="1小时" />
            <el-option label="2小时" value="2小时" />
            <el-option label="3小时" value="3小时" />
          </el-select>
        </el-form-item>

        <el-form-item
          v-else
          label="结束时间"
          prop="endTime"
        >
          <el-date-picker
            v-model="newMeetingForm.endTime"
            type="datetime"
            placeholder="选择会议结束时间"
            :disabled-date="disabledEndDate"
            :disabled-time="disabledEndTime"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>

        <el-form-item label="会议室" prop="room">
          <el-select v-model="newMeetingForm.room" placeholder="请选择会议室">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="room.name"
              :value="room.name"
              :disabled="!room.available"
            >
              <span>{{ room.name }}</span>
              <span v-if="!room.available" style="color: #ccc; margin-left: 8px;">(已占用)</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="参与人数" prop="participants">
          <el-input-number
            v-model="newMeetingForm.participants"
            :min="1"
            :max="50"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="会议密码">
          <el-checkbox v-model="newMeetingForm.usePassword">使用会议密码</el-checkbox>
        </el-form-item>

        <el-form-item
          v-if="newMeetingForm.usePassword"
          label="密码"
          prop="password"
        >
          <el-input
            v-model="newMeetingForm.password"
            type="password"
            placeholder="请输入会议密码"
            maxlength="20"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAddSchedule">
            {{ isEditMode ? '保存修改' : '确认添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 加入会议对话框 -->
    <el-dialog
      v-model="showJoinDialog"
      title="加入会议"
      width="500px"
      center
    >
      <el-form
        :model="joinMeetingForm"
        :rules="joinMeetingRules"
        ref="joinMeetingFormRef"
        label-width="100px"
      >
        <el-form-item label="会议号" prop="meetingId">
          <el-input
            v-model="joinMeetingForm.meetingId"
            placeholder="请输入会议号"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="joinMeetingForm.nickname"
            placeholder="请输入您的昵称"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="会议密码" prop="password" v-if="joinMeetingForm.hasPassword">
          <el-input
            v-model="joinMeetingForm.password"
            type="password"
            placeholder="请输入会议密码"
            maxlength="20"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showJoinDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmJoinMeeting">加入会议</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 会议记录对话框 -->
    <el-dialog
      v-model="showMeetingRecordsDialog"
      title="会议记录"
      width="800px"
      center
    >
      <div class="meeting-records-container">
        <!-- 今天 -->
        <div v-if="todayRecords.length > 0" class="record-section">
          <h3 class="section-title">今天</h3>
          <div class="record-list">
            <div
              v-for="(record, index) in todayRecords"
              :key="index"
              class="record-item"
            >
              <div class="record-time">
                <div class="date">{{ formatDate(record.startTime, 'MM-DD') }}</div>
                <div class="time-duration">
                  {{ formatDate(record.startTime, 'HH:mm') }} / {{ record.duration }}
                </div>
              </div>
              <div class="record-info">
                <div class="record-title">{{ record.title }}</div>
                <div class="record-details">
                  <span class="record-id">ID: {{ record.meetingId }}</span>
                  <span class="record-room">{{ record.room }}</span>
                  <span class="record-participants">{{ record.participants }} 人参加</span>
                </div>
              </div>
              <div class="record-actions">
                <el-button type="primary" size="small" @click="viewRecordDetails(record)">
                  查看
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 昨天 -->
        <div v-if="yesterdayRecords.length > 0" class="record-section">
          <h3 class="section-title">昨天</h3>
          <div class="record-list">
            <div
              v-for="(record, index) in yesterdayRecords"
              :key="index"
              class="record-item"
            >
              <div class="record-time">
                <div class="date">{{ formatDate(record.startTime, 'MM-DD') }}</div>
                <div class="time-duration">
                  {{ formatDate(record.startTime, 'HH:mm') }} / {{ record.duration }}
                </div>
              </div>
              <div class="record-info">
                <div class="record-title">{{ record.title }}</div>
                <div class="record-details">
                  <span class="record-id">ID: {{ record.meetingId }}</span>
                  <span class="record-room">{{ record.room }}</span>
                  <span class="record-participants">{{ record.participants }} 人参加</span>
                </div>
              </div>
              <div class="record-actions">
                <el-button type="primary" size="small" @click="viewRecordDetails(record)">
                  查看
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 七天内 -->
        <div v-if="weekRecords.length > 0" class="record-section">
          <h3 class="section-title">七天内</h3>
          <div class="record-list">
            <div
              v-for="(record, index) in weekRecords"
              :key="index"
              class="record-item"
            >
              <div class="record-time">
                <div class="date">{{ formatDate(record.startTime, 'MM-DD') }}</div>
                <div class="time-duration">
                  {{ formatDate(record.startTime, 'HH:mm') }} / {{ record.duration }}
                </div>
              </div>
              <div class="record-info">
                <div class="record-title">{{ record.title }}</div>
                <div class="record-details">
                  <span class="record-id">ID: {{ record.meetingId }}</span>
                  <span class="record-room">{{ record.room }}</span>
                  <span class="record-participants">{{ record.participants }} 人参加</span>
                </div>
              </div>
              <div class="record-actions">
                <el-button type="primary" size="small" @click="viewRecordDetails(record)">
                  查看
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 更早 -->
        <div v-if="earlierRecords.length > 0" class="record-section">
          <h3 class="section-title">更早</h3>
          <div class="record-list">
            <div
              v-for="(record, index) in earlierRecords"
              :key="index"
              class="record-item"
            >
              <div class="record-time">
                <div class="date">{{ formatDate(record.startTime, 'YYYY-MM-DD') }}</div>
                <div class="time-duration">
                  {{ formatDate(record.startTime, 'HH:mm') }} / {{ record.duration }}
                </div>
              </div>
              <div class="record-info">
                <div class="record-title">{{ record.title }}</div>
                <div class="record-details">
                  <span class="record-id">ID: {{ record.meetingId }}</span>
                  <span class="record-room">{{ record.room }}</span>
                  <span class="record-participants">{{ record.participants }} 人参加</span>
                </div>
              </div>
              <div class="record-actions">
                <el-button type="primary" size="small" @click="viewRecordDetails(record)">
                  查看
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="allRecords.length === 0" class="empty-records">
          <el-icon><Document /></el-icon>
          <p>暂无会议记录</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  VideoCamera,
  VideoPlay,
  Calendar,
  Document
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import isBetween from 'dayjs/plugin/isBetween'

// types
import type { MeetingRequest } from '@/types/api'

// 新增：引入后端 meeting API
import { meetingAPI } from '@/api/meeting'

// 扩展 dayjs 插件
dayjs.extend(isBetween)

// 定义会议接口（与 types/api.ts 保持兼容）
interface Meeting {
  id?: number
  title?: string
  startTime?: string
  endTime?: string
  duration?: string
  room?: string
  participants?: number
  isActive?: boolean
  time?: string
  hasPassword?: boolean
  password?: string
  meetingId?: string
}

// 定义新会议表单接口
interface NewMeetingForm {
  title: string
  startTime: string
  endTime: string
  durationType: string
  fixedDuration: string
  room: string
  participants: number
  usePassword: boolean
  password: string
}

// 定义加入会议表单接口
interface JoinMeetingForm {
  meetingId: string
  nickname: string
  password: string
  hasPassword: boolean
}

// 定义会议室接口
interface Room {
  id: number
  name: string
  available: boolean
}

// 当前日期信息
const currentDate = reactive({ day: '', weekday: '', fullDate: '' })

// 今日会议日程 - 初始为空（前端示例）
const todayMeetings = ref<Meeting[]>([])

// 所有会议记录（包括过去的会议） - 本地示例作为降级显示
const allRecords = ref<Meeting[]>([
  {
    id: 1,
    title: '产品需求评审会议',
    startTime: dayjs().format('YYYY-MM-DD 09:30'),
    endTime: dayjs().format('YYYY-MM-DD 10:30'),
    duration: '1小时',
    room: '会议室1',
    participants: 8,
    isActive: false,
    time: '09:30',
    hasPassword: false,
    meetingId: 'M12345678'
  }
])

// 会议室列表
const rooms = ref<Room[]>([ { id: 1, name: '会议室1', available: true }, { id: 2, name: '会议室2', available: true } ])

// 对话与表单
const showAddDialog = ref(false)
const isEditMode = ref(false)
const meetingFormRef = ref<FormInstance>()
const newMeetingForm = reactive<NewMeetingForm>({ title: '', startTime: '', endTime: '', durationType: 'fixed', fixedDuration: '1小时', room: '', participants: 1, usePassword: false, password: '' })

const showJoinDialog = ref(false)
const joinMeetingFormRef = ref<FormInstance>()
const joinMeetingForm = reactive<JoinMeetingForm>({ meetingId: '', nickname: '', password: '', hasPassword: false })

const showMeetingRecordsDialog = ref(false)

// 表单规则
const meetingRules: FormRules = {
  title: [{ required: true, message: '请输入会议标题', trigger: 'blur' }, { min: 2, max: 50, message: '会议标题长度在 2 到 50 个字符', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择会议开始时间', trigger: 'change' }],
  fixedDuration: [{ required: true, message: '请选择会议时长', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择会议结束时间', trigger: 'change' }],
  room: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  participants: [{ required: true, message: '请填写参与人数', trigger: 'blur' }],
  password: [{ required: true, message: '请输��会议密码', trigger: 'blur' }]
}

const joinMeetingRules: FormRules = {
  meetingId: [{ required: true, message: '请输入会议号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入您的昵称', trigger: 'blur' }, { min: 1, max: 20, message: '昵称长度在 1 到 20 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入会议密码', trigger: 'blur' }]
}

// 后端会议相关状态
const backendMeetings = ref<Meeting[]>([])
const meetingsLoading = ref(false)
const meetingsPage = ref(0)
const meetingsSize = ref(10)
const meetingsTotal = ref(0)

// Helper: unwrap BaseResponse.data safely
function unwrap<T>(resp: unknown): T | undefined { return ((resp as unknown) as { data?: unknown })?.data as T | undefined }

// fetch meetings from backend
async function fetchMeetings(page = 0, size = 10) {
  try {
    meetingsLoading.value = true
    const resp = await meetingAPI.getAllMeetings(page, size)
    const data = unwrap<Record<string, unknown>>(resp) ?? resp

    if (data && typeof data === 'object') {
      const d = data as Record<string, unknown>
      if (Array.isArray(d['content'])) {
        backendMeetings.value = (d['content'] as unknown) as Meeting[]
        meetingsTotal.value = typeof d['totalElements'] === 'number' ? (d['totalElements'] as number) : backendMeetings.value.length
        meetingsPage.value = (typeof d['number'] === 'number') ? ((d['number'] as number) + 1) : 1
        meetingsSize.value = typeof d['size'] === 'number' ? (d['size'] as number) : size
      } else if (Array.isArray(d['list'])) {
        backendMeetings.value = (d['list'] as unknown) as Meeting[]
        meetingsTotal.value = typeof d['total'] === 'number' ? (d['total'] as number) : backendMeetings.value.length
        meetingsPage.value = (typeof d['page'] === 'number') ? ((d['page'] as number) + 1) : 1
        meetingsSize.value = typeof d['pageSize'] === 'number' ? (d['pageSize'] as number) : size
      } else if (Array.isArray(data)) {
        backendMeetings.value = data as Meeting[]
        meetingsTotal.value = backendMeetings.value.length
        meetingsPage.value = 1
        meetingsSize.value = backendMeetings.value.length
      } else {
        backendMeetings.value = []
        meetingsTotal.value = 0
      }
    } else {
      backendMeetings.value = []
      meetingsTotal.value = 0
    }
  } catch (err) {
    console.error('fetchMeetings error', err)
    ElMessage.error('加载会议列表失败')
  } finally { meetingsLoading.value = false }
}

// 优先显示后端会议，否则显示本地 todayMeetings
const displayedMeetings = computed(() => backendMeetings.value.length > 0 ? backendMeetings.value : todayMeetings.value)

// 计算会议记录分组
const todayRecords = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return allRecords.value.filter(record => dayjs(record.startTime).format('YYYY-MM-DD') === today)
})
const yesterdayRecords = computed(() => {
  const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
  return allRecords.value.filter(record => dayjs(record.startTime).format('YYYY-MM-DD') === yesterday)
})
const weekRecords = computed(() => {
  const weekAgo = dayjs().subtract(7, 'day')
  const twoDaysAgo = dayjs().subtract(2, 'day')
  return allRecords.value.filter(record => {
    const recordDate = dayjs(record.startTime)
    return recordDate.isAfter(weekAgo) && recordDate.isBefore(twoDaysAgo, 'day')
  })
})
const earlierRecords = computed(() => {
  const weekAgo = dayjs().subtract(7, 'day')
  return allRecords.value.filter(record => dayjs(record.startTime).isBefore(weekAgo))
})

// 计算可用的会议室
const availableRooms = computed(() => {
  return rooms.value.map(room => {
    const isOccupied = (displayedMeetings.value ?? todayMeetings.value).some(meeting => {
      if (meeting.room === room.name && meeting.startTime && meeting.endTime) {
        const now = dayjs()
        const meetingStart = dayjs(meeting.startTime)
        const meetingEnd = dayjs(meeting.endTime)
        return now.isBetween(meetingStart, meetingEnd, null, '[]')
      }
      return false
    })
    return { ...room, available: !isOccupied }
  })
})

// 表单/按钮处理
const handleJoinMeeting = () => { showJoinDialog.value = true; if (joinMeetingFormRef.value) joinMeetingFormRef.value.resetFields(); Object.assign(joinMeetingForm, { meetingId: '', nickname: '', password: '', hasPassword: false }) }
const handleQuickMeeting = () => {
  const currentUser = getCurrentUser()
  const currentTime = dayjs()
  const endTime = currentTime.add(1, 'hour')
  const availableRoom = availableRooms.value.find(r => r.available)
  if (!availableRoom) { ElMessage.warning('当前没有可用的会议室'); return }
  const quickMeeting: Meeting = { title: `${currentUser}的会议`, startTime: currentTime.format('YYYY-MM-DD HH:mm'), endTime: endTime.format('YYYY-MM-DD HH:mm'), duration: '1小时', room: availableRoom.name, participants: 1, isActive: true, time: currentTime.format('HH:mm'), hasPassword: false, meetingId: `QM${Date.now().toString().slice(-6)}` }
  todayMeetings.value.push(quickMeeting)
  todayMeetings.value.sort((a,b) => (a.startTime ?? '').localeCompare(b.startTime ?? ''))
  ElMessage.success(`快速会议已创建，会议号: ${quickMeeting.meetingId}`)
}
const handleScheduleMeeting = () => { showAddDialog.value = true; isEditMode.value = false; resetForm() }
const handleMeetingRecords = () => { showMeetingRecordsDialog.value = true }

const joinScheduledMeeting = (meeting: Meeting) => {
  if (meeting.hasPassword) { showJoinDialog.value = true; joinMeetingForm.meetingId = meeting.meetingId || ''; joinMeetingForm.hasPassword = true } else { ElMessage.success(`加入会议: ${meeting.title}`) }
}

// 重置表单等
const resetForm = () => { if (meetingFormRef.value) meetingFormRef.value.clearValidate(); Object.assign(newMeetingForm, { title: '', startTime: dayjs().format('YYYY-MM-DD HH:mm'), endTime: dayjs().add(1, 'hour').format('YYYY-MM-DD HH:mm'), durationType: 'fixed', fixedDuration: '1小时', room: '', participants: 1, usePassword: false, password: '' }) }

const computedEndTime = computed(() => {
  if (!newMeetingForm.startTime || newMeetingForm.durationType !== 'fixed') return ''
  const start = dayjs(newMeetingForm.startTime)
  const durationMap: Record<string, number> = { '15分钟':15,'30分钟':30,'45分钟':45,'1小时':60,'2小时':120,'3小时':180 }
  const minutes = durationMap[newMeetingForm.fixedDuration] || 60
  return start.add(minutes, 'minute').format('YYYY-MM-DD HH:mm')
})

// disabled helpers
const disabledDate = (time: Date) => time.getTime() < Date.now() - 24 * 60 * 60 * 1000
const disabledTime = (time: Date) => {
  if (dayjs(time).isSame(dayjs(), 'day')) {
    const now = dayjs(); const selected = dayjs(time)
    return { hours: () => selected.hour() < now.hour(), minutes: () => (selected.hour() === now.hour() ? selected.minute() < now.minute() : false) }
  }
  return {}
}
const disabledEndDate = (time: Date) => {
  if (!newMeetingForm.startTime) return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
  const startDate = dayjs(newMeetingForm.startTime).startOf('day')
  const endLimit = startDate.add(1,'day')
  return time.getTime() < startDate.valueOf() || time.getTime() > endLimit.valueOf()
}
const disabledEndTime = (time: Date) => {
  if (!newMeetingForm.startTime) return {}
  const start = dayjs(newMeetingForm.startTime); const selected = dayjs(time); const maxEndTime = start.add(24,'hour')
  if (selected.isAfter(start,'day')) { return { hours: () => selected.hour() > maxEndTime.hour(), minutes: (h:number) => h===maxEndTime.hour() ? selected.minute() > maxEndTime.minute() : false } }
  if (selected.isSame(start,'day')) { return { hours: () => selected.hour() < start.hour() || selected.hour() > maxEndTime.hour(), minutes: (h:number) => { if (h===start.hour()) return selected.minute() <= start.minute(); if (h===maxEndTime.hour()) return selected.minute() > maxEndTime.minute(); return false } } }
  return {}
}

// 创建会议���调用后端）
const confirmAddSchedule = async () => {
  if (!meetingFormRef.value) return
  try {
    const valid = await meetingFormRef.value.validate()
    if (!valid) return
    let endTime = newMeetingForm.endTime
    if (newMeetingForm.durationType === 'fixed') endTime = computedEndTime.value
    if (!endTime) { ElMessage.warning('请确保结束时间有效'); return }
    if (dayjs(endTime).isBefore(dayjs(newMeetingForm.startTime))) { ElMessage.warning('结束时间不能早于开始时间'); return }
    const start = dayjs(newMeetingForm.startTime); const end = dayjs(endTime)
    if (end.diff(start,'hour',true) > 24) { ElMessage.warning('会议时长不能超过24小时'); return }

    const payload: MeetingRequest = { host_id: undefined, place_id: undefined, title: newMeetingForm.title, description: '', start_time: newMeetingForm.startTime, end_time: endTime, members_id: [] }

    try {
      await meetingAPI.createMeeting(payload)
      ElMessage.success('会议创建成功')
      showAddDialog.value = false
      resetForm()
      setTimeout(() => fetchMeetings(0, meetingsSize.value), 500)
    } catch (e) {
      console.error('create meeting error', e)
      ElMessage.error('创建会议失败')
    }
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('添加会议失败，请重试')
  }
}

// 加入会议对话（模拟）
const confirmJoinMeeting = async () => {
  if (!joinMeetingFormRef.value) return
  try {
    const valid = await joinMeetingFormRef.value.validate()
    if (!valid) return
    ElMessage.success(`成功加入会议，会议号: ${joinMeetingForm.meetingId}`)
    showJoinDialog.value = false
    if (joinMeetingFormRef.value) joinMeetingFormRef.value.resetFields()
  } catch (e) { console.error('join validate error', e); ElMessage.error('加入会议失败') }
}

// 工具函数
const getCurrentUser = (): string => {
  try {
    const userInfo = sessionStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.account || '用户'
    }
  } catch (e) {
    console.warn('无法读取用户信息:', e)
  }
  return '用户'
}

const formatDate = (dateString: string | undefined, format = 'YYYY-MM-DD') => {
  if (!dateString) return ''
  return dayjs(dateString).format(format)
}

// 新增：查看会议记录详情（绑定到模板按钮）
const viewRecordDetails = (record: Meeting) => {
  if (!record) return
  ElMessage.info(`会议：${record.title ?? ''}\n时间：${record.startTime ?? ''} - ${record.endTime ?? ''}`)
}

// 初始化辅助函数
function initDateInfo() { const now = new Date(); const days = ['星期日','星期一','星期二','星期三','星期四','星期五','星期六']; currentDate.day = now.getDate().toString(); currentDate.weekday = days[now.getDay()]; currentDate.fullDate = `${now.getFullYear()}年${now.getMonth()+1}月${now.getDate()}日` }
function setDefaultStartTime() { newMeetingForm.startTime = dayjs().format('YYYY-MM-DD HH:mm') }
function setDefaultEndTime() { newMeetingForm.endTime = dayjs(newMeetingForm.startTime).add(1,'hour').format('YYYY-MM-DD HH:mm') }

onMounted(() => { initDateInfo(); setDefaultStartTime(); setDefaultEndTime(); fetchMeetings(meetingsPage.value - 1, meetingsSize.value) })
</script>

<style scoped>
:root{
  --accent-color: #409EFF; /* 主强调色，Element 默认蓝 */
  --success-color: #67C23A;
  --muted-color: #6c757d;
  --panel-bg: #f7f9fc;
}

.meeting-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(180deg, #f5f7fb 0%, #eef4fb 100%);
  padding: 18px;
  box-sizing: border-box;
  overflow: hidden;
}

.meeting-container {
  display: flex;
  width: 100%;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(10, 28, 70, 0.06);
  overflow: hidden;
}

/* 左侧面板样式 - 收窄 */
.left-panel {
  flex: 0 0 34%;
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--panel-bg);
  border-right: 1px solid rgba(16, 24, 40, 0.04);
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 18px;
  width: 100%;
  max-width: 360px;
}

.grid-item {
  background: #fff;
  border-radius: 10px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.18s ease;
  box-shadow: 0 4px 14px rgba(12, 18, 40, 0.04);
  border: 1px solid rgba(16, 24, 40, 0.03);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 110px;
}

.grid-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(16, 24, 40, 0.08);
  border-color: rgba(64,158,255,0.18);
}

.grid-item .icon-wrapper {
  color: var(--accent-color);
  margin-bottom: 10px;
}

.grid-item .label {
  font-size: 14px;
  font-weight: 600;
  color: #172b4d;
}

/* 右侧面板样式 - 扩大 */
.right-panel {
  flex: 1;
  padding: 28px 30px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.date-section {
  margin-bottom: 18px;
  flex-shrink: 0;
}

.current-date {
  display: flex;
  align-items: center;
  gap: 14px;
}

.date-number {
  font-size: 40px;
  font-weight: 700;
  color: var(--accent-color);
  line-height: 1;
}

.weekday {
  font-size: 16px;
  font-weight: 700;
  color: #172b4d;
  margin-bottom: 6px;
}

.full-date {
  font-size: 13px;
  color: var(--muted-color);
}

.schedule-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.schedule-header {
  flex-shrink: 0;
}

.schedule-header h3 {
  margin: 0 0 12px 0;
  color: #172b4d;
  font-size: 16px;
  font-weight: 700;
}

.schedule-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 6px;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  margin-bottom: 10px;
  background: #fbfdff;
  border-radius: 8px;
  border-left: 4px solid rgba(64,158,255,0.12);
  transition: all 0.18s ease;
}

.schedule-item:hover {
  background: #f6fbff;
}

.schedule-item.active {
  border-left-color: var(--success-color);
  background: #f6fff3;
}

.meeting-time {
  min-width: 84px;
  margin-right: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time {
  font-size: 13px;
  font-weight: 700;
  color: var(--accent-color);
  margin-bottom: 4px;
}

.duration {
  font-size: 12px;
  color: var(--muted-color);
}

.meeting-info {
  flex: 1;
}

.meeting-title {
  font-size: 15px;
  font-weight: 700;
  color: #10233f;
  margin-bottom: 4px;
}

.meeting-details {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--muted-color);
}

.meeting-id {
  font-weight: 600;
  color: var(--accent-color);
}

.meeting-room {
  padding: 2px 6px;
  background: #f0f6ff;
  border-radius: 4px;
}

.password-tag {
  padding: 2px 6px;
  background: #fff7ed;
  color: #e6a23c;
  border-radius: 4px;
}

.meeting-actions {
  min-width: 92px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.empty-schedule {
  text-align: center;
  padding: 28px 20px;
  color: var(--muted-color);
}

.empty-schedule .el-icon {
  font-size: 38px;
  margin-bottom: 12px;
  color: #c7dfff;
}

/* 会议记录样式（对话框里） */
.meeting-records-container {
  max-height: 62vh;
  overflow-y: auto;
  padding-right: 10px;
}

.record-section {
  margin-bottom: 18px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  color: #10233f;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(16,24,40,0.04);
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fbfdff;
  border-radius: 8px;
  border-left: 4px solid rgba(64,158,255,0.08);
}

.record-time {
  min-width: 92px;
  margin-right: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.record-time .date {
  font-size: 13px;
  font-weight: 700;
  color: var(--accent-color);
  margin-bottom: 4px;
}

.record-time .time-duration {
  font-size: 12px;
  color: var(--muted-color);
}

.record-info {
  flex: 1;
}

.record-title {
  font-size: 14px;
  font-weight: 700;
  color: #10233f;
  margin-bottom: 4px;
}

.record-details {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--muted-color);
}

.empty-records .el-icon {
  font-size: 38px;
  margin-bottom: 12px;
  color: #c7dfff;
}

/* 响应式 */
@media (max-width: 900px) {
  .meeting-container { flex-direction: column; height: auto; min-height: 100vh }
  .left-panel { flex: none; width: 100%; border-right: none; border-bottom: 1px solid rgba(16, 24, 40, 0.04); padding: 18px }
  .right-panel { padding: 18px }
  .grid-container { max-width: 100%; gap: 12px }
}

/* 对话框样式调整 */
:deep(.el-dialog) { border-radius: 10px }
:deep(.el-form-item) { margin-bottom: 16px }
:deep(.el-radio-group) { width: 100% }
:deep(.el-select) { width: 100% }
</style>
