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
              v-for="(meeting, index) in todayMeetings"
              :key="index"
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

            <div v-if="todayMeetings.length === 0" class="empty-schedule">
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  VideoCamera,
  VideoPlay,
  Calendar,
  Document,
  Plus
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import isBetween from 'dayjs/plugin/isBetween'

// 扩展 dayjs 插件
dayjs.extend(isBetween)

// 定义会议接口
interface Meeting {
  id?: number
  title: string
  startTime: string
  endTime: string
  duration: string
  room: string
  participants: number
  isActive: boolean
  time: string
  hasPassword: boolean
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
const currentDate = reactive({
  day: '',
  weekday: '',
  fullDate: ''
})

// 今日会议日程 - 初始为空
const todayMeetings = ref<Meeting[]>([])

// 所有会议记录（包括过去的会议）
const allRecords = ref<Meeting[]>([
  // 今天
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
  },
  // 昨天
  {
    id: 2,
    title: '技术方案讨论',
    startTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD 14:00'),
    endTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD 15:00'),
    duration: '1小时',
    room: '会议室2',
    participants: 5,
    isActive: false,
    time: '14:00',
    hasPassword: true,
    meetingId: 'M23456789'
  },
  // 3天前
  {
    id: 3,
    title: '项目进度同步会',
    startTime: dayjs().subtract(3, 'day').format('YYYY-MM-DD 16:30'),
    endTime: dayjs().subtract(3, 'day').format('YYYY-MM-DD 17:00'),
    duration: '30分钟',
    room: '会议室1',
    participants: 12,
    isActive: false,
    time: '16:30',
    hasPassword: false,
    meetingId: 'M34567890'
  },
  // 10天前
  {
    id: 4,
    title: '月度总结会议',
    startTime: dayjs().subtract(10, 'day').format('YYYY-MM-DD 10:00'),
    endTime: dayjs().subtract(10, 'day').format('YYYY-MM-DD 11:30'),
    duration: '1.5小时',
    room: '会议室2',
    participants: 15,
    isActive: false,
    time: '10:00',
    hasPassword: false,
    meetingId: 'M45678901'
  }
])

// 会议室列表
const rooms = ref<Room[]>([
  { id: 1, name: '会议室1', available: true },
  { id: 2, name: '会议室2', available: true }
])

// 添加/编辑会议对话框
const showAddDialog = ref(false)
const isEditMode = ref(false)
const meetingFormRef = ref<FormInstance>()
const newMeetingForm = reactive<NewMeetingForm>({
  title: '',
  startTime: '',
  endTime: '',
  durationType: 'fixed',
  fixedDuration: '1小时',
  room: '',
  participants: 1,
  usePassword: false,
  password: ''
})

// 加入会议对话框
const showJoinDialog = ref(false)
const joinMeetingFormRef = ref<FormInstance>()
const joinMeetingForm = reactive<JoinMeetingForm>({
  meetingId: '',
  nickname: '',
  password: '',
  hasPassword: false
})

// 会议记录对话框
const showMeetingRecordsDialog = ref(false)

// 表单验证规则
const meetingRules: FormRules = {
  title: [
    { required: true, message: '请输入会议标题', trigger: 'blur' },
    { min: 2, max: 50, message: '会议标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择会议开始时间', trigger: 'change' }
  ],
  fixedDuration: [
    { required: true, message: '请选择会议时长', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择会议结束时间', trigger: 'change' }
  ],
  room: [
    { required: true, message: '请选择会议室', trigger: 'change' }
  ],
  participants: [
    { required: true, message: '请填写参与人数', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入会议密码', trigger: 'blur' }
  ]
}

// 加入会议表单验证规则
const joinMeetingRules: FormRules = {
  meetingId: [
    { required: true, message: '请输入会议号', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入您的昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入会议密码', trigger: 'blur' }
  ]
}

// 计算可用的会议室
const availableRooms = computed(() => {
  return rooms.value.map(room => {
    // 检查会议室是否被占用
    const isOccupied = todayMeetings.value.some(meeting => {
      if (meeting.room === room.name) {
        const now = dayjs()
        const meetingStart = dayjs(meeting.startTime)
        const meetingEnd = dayjs(meeting.endTime)

        // 使用 isBetween 检查当前时间是否在会议时间范围内
        return now.isBetween(meetingStart, meetingEnd, null, '[]')
      }
      return false
    })

    return {
      ...room,
      available: !isOccupied
    }
  })
})

// 计算会议记录分组
const todayRecords = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return allRecords.value.filter(record =>
    dayjs(record.startTime).format('YYYY-MM-DD') === today
  )
})

const yesterdayRecords = computed(() => {
  const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
  return allRecords.value.filter(record =>
    dayjs(record.startTime).format('YYYY-MM-DD') === yesterday
  )
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
  return allRecords.value.filter(record =>
    dayjs(record.startTime).isBefore(weekAgo)
  )
})

// 计算结束时间（当选择固定时长时）
const computedEndTime = computed(() => {
  if (!newMeetingForm.startTime || newMeetingForm.durationType !== 'fixed') {
    return ''
  }

  const start = dayjs(newMeetingForm.startTime)
  const durationMap: Record<string, number> = {
    '15分钟': 15,
    '30分钟': 30,
    '45分钟': 45,
    '1小时': 60,
    '2小时': 120,
    '3小时': 180
  }

  const minutes = durationMap[newMeetingForm.fixedDuration] || 60
  return start.add(minutes, 'minute').format('YYYY-MM-DD HH:mm')
})

// 设置默认开始时间为当前时间
const setDefaultStartTime = () => {
  newMeetingForm.startTime = dayjs().format('YYYY-MM-DD HH:mm')
}

// 设置默认结束时间为开始时间后1小时
const setDefaultEndTime = () => {
  newMeetingForm.endTime = dayjs(newMeetingForm.startTime).add(1, 'hour').format('YYYY-MM-DD HH:mm')
}

// 监听开始时间变化，更新结束时间
watch(() => newMeetingForm.startTime, (newVal) => {
  if (newVal && newMeetingForm.durationType === 'fixed') {
    newMeetingForm.endTime = computedEndTime.value
  } else if (newVal && !newMeetingForm.endTime) {
    setDefaultEndTime()
  }
})

// 监听时长类型变化
watch(() => newMeetingForm.durationType, (newVal) => {
  if (newVal === 'fixed' && newMeetingForm.startTime) {
    newMeetingForm.endTime = computedEndTime.value
  }
})

// 监听固定时长变化
watch(() => newMeetingForm.fixedDuration, (newVal) => {
  if (newMeetingForm.durationType === 'fixed' && newMeetingForm.startTime) {
    newMeetingForm.endTime = computedEndTime.value
  }
})

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 禁用过去的时间（如果是今天）
const disabledTime = (time: Date) => {
  if (dayjs(time).isSame(dayjs(), 'day')) {
    const now = dayjs()
    const selected = dayjs(time)

    return {
      hours: () => selected.hour() < now.hour(),
      minutes: (selectedHour: number) =>
        selectedHour === now.hour() ? selected.minute() < now.minute() : false
    }
  }
  return {}
}

// 禁用结束日期（只能选择开始时间的当天和第二天）
const disabledEndDate = (time: Date) => {
  if (!newMeetingForm.startTime) return time.getTime() < Date.now() - 24 * 60 * 60 * 1000

  const startDate = dayjs(newMeetingForm.startTime).startOf('day')
  const endLimit = startDate.add(1, 'day') // 最多只能选择到第二天

  return time.getTime() < startDate.valueOf() || time.getTime() > endLimit.valueOf()
}

// 禁用结束时间（不能早于开始时间，且不能超过开始时间24小时）
const disabledEndTime = (time: Date) => {
  if (!newMeetingForm.startTime) return {}

  const start = dayjs(newMeetingForm.startTime)
  const selected = dayjs(time)
  const maxEndTime = start.add(24, 'hour') // 最多24小时

  // 如果选择的日期是开始时间的第二天，检查时间是否超过最大限制
  if (selected.isAfter(start, 'day')) {
    return {
      hours: () => selected.hour() > maxEndTime.hour(),
      minutes: (selectedHour: number) =>
        selectedHour === maxEndTime.hour() ? selected.minute() > maxEndTime.minute() : false
    }
  }

  // 如果是同一天，检查时间是否在开始时间和最大结束时间之间
  if (selected.isSame(start, 'day')) {
    return {
      hours: () => selected.hour() < start.hour() || selected.hour() > maxEndTime.hour(),
      minutes: (selectedHour: number) => {
        if (selectedHour === start.hour()) {
          return selected.minute() <= start.minute()
        } else if (selectedHour === maxEndTime.hour()) {
          return selected.minute() > maxEndTime.minute()
        }
        return false
      }
    }
  }

  return {}
}

// 初始化日期信息
const initDateInfo = () => {
  const now = new Date()
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

  currentDate.day = now.getDate().toString()
  currentDate.weekday = days[now.getDay()]
  currentDate.fullDate = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
}

// 获取当前用户信息
const getCurrentUser = () => {
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

// 格式化日期
const formatDate = (dateString: string, format: string) => {
  return dayjs(dateString).format(format)
}

// 左侧功能按钮处理
const handleJoinMeeting = () => {
  showJoinDialog.value = true
  // 重置表单
  if (joinMeetingFormRef.value) {
    joinMeetingFormRef.value.resetFields()
  }
  Object.assign(joinMeetingForm, {
    meetingId: '',
    nickname: '',
    password: '',
    hasPassword: false
  })
}

const handleQuickMeeting = () => {
  try {
    const currentUser = getCurrentUser()
    const currentTime = dayjs()
    const endTime = currentTime.add(1, 'hour')

    // 查找空闲会议室
    const availableRoom = availableRooms.value.find(room => room.available)
    if (!availableRoom) {
      ElMessage.warning('当前没有可用的会议室')
      return
    }

    const quickMeeting: Meeting = {
      title: `${currentUser}的会议`,
      startTime: currentTime.format('YYYY-MM-DD HH:mm'),
      endTime: endTime.format('YYYY-MM-DD HH:mm'),
      duration: '1小时',
      room: availableRoom.name,
      participants: 1,
      isActive: true,
      time: currentTime.format('HH:mm'),
      hasPassword: false,
      meetingId: `QM${Date.now().toString().slice(-6)}`
    }

    todayMeetings.value.push(quickMeeting)

    // 按开始时间排序
    todayMeetings.value.sort((a, b) => a.startTime.localeCompare(b.startTime))

    ElMessage.success(`快速会议已创建，会议号: ${quickMeeting.meetingId}`)
  } catch (error) {
    console.error('创建快速会议时出错:', error)
    ElMessage.error('创建快速会议失败，请重试')
  }
}

const handleScheduleMeeting = () => {
  showAddDialog.value = true
  isEditMode.value = false
  resetForm()
}

const handleMeetingRecords = () => {
  showMeetingRecordsDialog.value = true
}

// 加入已安排的会议
const joinScheduledMeeting = (meeting: Meeting) => {
  if (meeting.hasPassword) {
    // 如果会议有密码，需要先输入密码
    showJoinDialog.value = true
    joinMeetingForm.meetingId = meeting.meetingId || ''
    joinMeetingForm.hasPassword = true
  } else {
    ElMessage.success(`加入会议: ${meeting.title}`)
    // 实际开发中这里可以加入具体的会议
  }
}

// 添加日程
const handleAddSchedule = () => {
  showAddDialog.value = true
  isEditMode.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  if (meetingFormRef.value) {
    meetingFormRef.value.clearValidate()
  }
  Object.assign(newMeetingForm, {
    title: '',
    startTime: dayjs().format('YYYY-MM-DD HH:mm'),
    endTime: dayjs().add(1, 'hour').format('YYYY-MM-DD HH:mm'),
    durationType: 'fixed',
    fixedDuration: '1小时',
    room: '',
    participants: 1,
    usePassword: false,
    password: ''
  })
}

// 计算会议时长显示
const calculateDurationDisplay = (startTime: string, endTime: string): string => {
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  const diffMinutes = end.diff(start, 'minute')

  if (diffMinutes < 60) {
    return `${diffMinutes}分钟`
  } else if (diffMinutes === 60) {
    return '1小时'
  } else {
    const hours = Math.floor(diffMinutes / 60)
    const minutes = diffMinutes % 60
    return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
  }
}

// 生成会议ID
const generateMeetingId = (): string => {
  return `M${Date.now().toString().slice(-8)}`
}

// 确认添加日程
const confirmAddSchedule = async () => {
  if (!meetingFormRef.value) return

  try {
    const valid = await meetingFormRef.value.validate()
    if (!valid) return

    // 如果是固定时长模式，计算结束时间
    let endTime = newMeetingForm.endTime
    if (newMeetingForm.durationType === 'fixed') {
      endTime = computedEndTime.value
    }

    if (!endTime) {
      ElMessage.warning('请确保结束时间有效')
      return
    }

    // 确保结束时间晚于开始时间
    if (dayjs(endTime).isBefore(dayjs(newMeetingForm.startTime))) {
      ElMessage.warning('结束时间不能早于开始时间')
      return
    }

    // 确保会议时长不超过24小时
    const start = dayjs(newMeetingForm.startTime)
    const end = dayjs(endTime)
    const diffHours = end.diff(start, 'hour', true)
    if (diffHours > 24) {
      ElMessage.warning('会议时长不能超过24小时')
      return
    }

    const newMeeting: Meeting = {
      title: newMeetingForm.title,
      startTime: newMeetingForm.startTime,
      endTime: endTime,
      duration: calculateDurationDisplay(newMeetingForm.startTime, endTime),
      room: newMeetingForm.room,
      participants: newMeetingForm.participants,
      isActive: false,
      time: dayjs(newMeetingForm.startTime).format('HH:mm'),
      hasPassword: newMeetingForm.usePassword,
      password: newMeetingForm.usePassword ? newMeetingForm.password : undefined,
      meetingId: generateMeetingId()
    }

    todayMeetings.value.push(newMeeting)

    // 按开始时间排序
    todayMeetings.value.sort((a, b) => a.startTime.localeCompare(b.startTime))

    ElMessage.success(isEditMode.value ? '会议日程修改成功' : '会议日程添加成功')
    showAddDialog.value = false
    resetForm()
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('添加会议失败，请重试')
  }
}

// 确认加入会议
const confirmJoinMeeting = async () => {
  if (!joinMeetingFormRef.value) return

  try {
    const valid = await joinMeetingFormRef.value.validate()
    if (!valid) return

    // 在实际应用中，这里会验证会议号和密码
    // 这里只是模拟成功加入
    ElMessage.success(`成功加入会议，会议号: ${joinMeetingForm.meetingId}`)
    showJoinDialog.value = false

    // 重置表单
    if (joinMeetingFormRef.value) {
      joinMeetingFormRef.value.resetFields()
    }
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('加入会议失败，请重试')
  }
}

// 查看会议记录详情
const viewRecordDetails = (record: Meeting) => {
  ElMessage.info(`查看会议记录: ${record.title}`)
  // 在实际应用中，这里可以跳转到会议详情页面或显示更多信息
}

onMounted(() => {
  initDateInfo()
  setDefaultStartTime()
  setDefaultEndTime()
})
</script>

<style scoped>
.meeting-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

.meeting-container {
  display: flex;
  width: 100%;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧面板样式 */
.left-panel {
  flex: 1;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-right: 1px solid #e9ecef;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 30px;
  width: 100%;
  max-width: 400px;
}

.grid-item {
  background: white;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 140px;
}

.grid-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #5b7cfa;
}

.grid-item .icon-wrapper {
  color: #5b7cfa;
  margin-bottom: 12px;
}

.grid-item .label {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

/* 右侧面板样式 */
.right-panel {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.date-section {
  margin-bottom: 30px;
  flex-shrink: 0;
}

.current-date {
  display: flex;
  align-items: center;
  gap: 20px;
}

.date-number {
  font-size: 48px;
  font-weight: bold;
  color: #5b7cfa;
  line-height: 1;
}

.date-info {
  display: flex;
  flex-direction: column;
}

.weekday {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.full-date {
  font-size: 14px;
  color: #6c757d;
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
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.schedule-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #5b7cfa;
  transition: all 0.3s ease;
}

.schedule-item:hover {
  background: #e9ecef;
}

.schedule-item.active {
  border-left-color: #67c23a;
  background: #f0f9ff;
}

.meeting-time {
  min-width: 80px;
  margin-right: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time {
  font-size: 14px;
  font-weight: 600;
  color: #5b7cfa;
  margin-bottom: 4px;
}

.duration {
  font-size: 12px;
  color: #6c757d;
}

.schedule-item.active .time {
  color: #67c23a;
}

.meeting-info {
  flex: 1;
}

.meeting-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.meeting-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6c757d;
}

.meeting-id {
  font-weight: 600;
  color: #5b7cfa;
}

.meeting-room {
  padding: 2px 6px;
  background: #e9ecef;
  border-radius: 4px;
}

.password-tag {
  padding: 2px 6px;
  background: #fff2e8;
  color: #e6a23c;
  border-radius: 4px;
}

.meeting-actions {
  min-width: 80px;
}

.empty-schedule {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
}

.empty-schedule .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #adb5bd;
}

.empty-schedule p {
  margin: 0;
  font-size: 14px;
}

/* 会议记录样式 */
.meeting-records-container {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.record-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e9ecef;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #5b7cfa;
  transition: all 0.3s ease;
}

.record-item:hover {
  background: #e9ecef;
}

.record-time {
  min-width: 100px;
  margin-right: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.record-time .date {
  font-size: 14px;
  font-weight: 600;
  color: #5b7cfa;
  margin-bottom: 4px;
}

.record-time .time-duration {
  font-size: 12px;
  color: #6c757d;
}

.record-info {
  flex: 1;
}

.record-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.record-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6c757d;
}

.record-id {
  font-weight: 600;
  color: #5b7cfa;
}

.record-room {
  padding: 2px 6px;
  background: #e9ecef;
  border-radius: 4px;
}

.record-actions {
  min-width: 80px;
}

.empty-records {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
}

.empty-records .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #adb5bd;
}

.empty-records p {
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .meeting-container {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
  }

  .left-panel {
    border-right: none;
    border-bottom: 1px solid #e9ecef;
    padding: 20px;
  }

  .right-panel {
    padding: 20px;
  }

  .grid-container {
    gap: 15px;
    max-width: 300px;
  }

  .grid-item {
    padding: 20px 15px;
    min-height: 120px;
  }

  .meeting-details,
  .record-details {
    flex-direction: column;
    gap: 4px;
  }

  .record-time {
    min-width: 80px;
  }
}

/* 确保在超小屏幕上也能正常显示 */
@media (max-height: 600px) {
  .meeting-page {
    padding: 10px;
  }

  .left-panel,
  .right-panel {
    padding: 20px;
  }

  .grid-container {
    gap: 15px;
  }

  .grid-item {
    padding: 15px 10px;
    min-height: 100px;
  }

  .date-number {
    font-size: 36px;
  }

  .weekday {
    font-size: 16px;
  }

  .schedule-item,
  .record-item {
    padding: 12px;
  }
}

/* 滚动条样式 */
.schedule-list::-webkit-scrollbar,
.meeting-records-container::-webkit-scrollbar {
  width: 4px;
}

.schedule-list::-webkit-scrollbar-track,
.meeting-records-container::-webkit-scrollbar-track {
  background: transparent;
}

.schedule-list::-webkit-scrollbar-thumb,
.meeting-records-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.schedule-list::-webkit-scrollbar-thumb:hover,
.meeting-records-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 对话框样式调整 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-radio-group) {
  width: 100%;
}

:deep(.el-radio) {
  margin-right: 20px;
}

:deep(.el-select) {
  width: 100%;
}
</style>
