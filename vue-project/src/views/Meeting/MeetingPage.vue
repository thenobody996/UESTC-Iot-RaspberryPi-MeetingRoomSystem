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
              </div>
              <div class="meeting-info">
                <div class="meeting-title">{{ meeting.title }}</div>
                <div class="meeting-participants">{{ meeting.participants }} 人参加</div>
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

        <!-- 圆形添加按钮 -->
        <div class="add-button-wrapper">
          <el-button
            class="add-schedule-btn"
            type="primary"
            circle
            @click="handleAddSchedule"
          >
            <el-icon size="24"><Plus /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 添加日程对话框 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加会议日程"
      width="500px"
      center
    >
      <el-form :model="newMeetingForm" label-width="80px">
        <el-form-item label="会议主题">
          <el-input v-model="newMeetingForm.title" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="会议时间">
          <el-time-picker
            v-model="newMeetingForm.time"
            placeholder="选择时间"
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="参与人数">
          <el-input-number
            v-model="newMeetingForm.participants"
            :min="1"
            :max="100"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAddSchedule">确认添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  VideoCamera,
  VideoPlay,
  Calendar,
  Document,
  Plus
} from '@element-plus/icons-vue'

// 定义会议接口
interface Meeting {
  time: string
  title: string
  participants: number
  isActive: boolean
}

// 定义新会议表单接口
interface NewMeetingForm {
  title: string
  time: string
  participants: number
}

// 当前日期信息
const currentDate = reactive({
  day: '',
  weekday: '',
  fullDate: ''
})

// 今日会议日程
const todayMeetings = ref<Meeting[]>([
  {
    time: '09:30',
    title: '产品需求评审会议',
    participants: 8,
    isActive: true
  },
  {
    time: '14:00',
    title: '技术方案讨论',
    participants: 5,
    isActive: false
  },
  {
    time: '16:30',
    title: '项目进度同步会',
    participants: 12,
    isActive: false
  }
])

// 添加日程对话框
const showAddDialog = ref(false)
const newMeetingForm = reactive<NewMeetingForm>({
  title: '',
  time: '',
  participants: 1
})

// 初始化日期信息
const initDateInfo = () => {
  const now = new Date()
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

  currentDate.day = now.getDate().toString()
  currentDate.weekday = days[now.getDay()]
  currentDate.fullDate = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
}

// 左侧功能按钮处理
const handleJoinMeeting = () => {
  ElMessage.info('跳转到加入会议页面')
  // 实际开发中这里可以跳转到对应页面或打开对话框
}

const handleQuickMeeting = () => {
  ElMessage.success('快速会议已创建')
  // 实际开发中这里可以创建即时会议
}

const handleScheduleMeeting = () => {
  showAddDialog.value = true
  // 可以在这里预填充表单
}

const handleMeetingRecords = () => {
  ElMessage.info('跳转到会议记录页面')
  // 实际开发中这里可以跳转到会议记录页面
}

// 加入已安排的会议
const joinScheduledMeeting = (meeting: Meeting) => {
  ElMessage.success(`加入会议: ${meeting.title}`)
  // 实际开发中这里可以加入具体的会议
}

// 添加日程
const handleAddSchedule = () => {
  showAddDialog.value = true
}

// 确认添加日程
const confirmAddSchedule = () => {
  if (!newMeetingForm.title || !newMeetingForm.time) {
    ElMessage.warning('请填写完整的会议信息')
    return
  }

  todayMeetings.value.push({
    time: newMeetingForm.time,
    title: newMeetingForm.title,
    participants: newMeetingForm.participants,
    isActive: false
  })

  // 按时间排序
  todayMeetings.value.sort((a, b) => a.time.localeCompare(b.time))

  ElMessage.success('会议日程添加成功')
  showAddDialog.value = false

  // 重置表单
  newMeetingForm.title = ''
  newMeetingForm.time = ''
  newMeetingForm.participants = 1
}

onMounted(() => {
  initDateInfo()
})
</script>

<style scoped>
.meeting-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden; /* 防止页面出现滚动条 */
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
  overflow: hidden; /* 防止右侧面板出现滚动条 */
}

.date-section {
  margin-bottom: 30px;
  flex-shrink: 0; /* 防止日期区域被压缩 */
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
  overflow: hidden; /* 防止日程区域溢出 */
}

.schedule-header {
  flex-shrink: 0; /* 防止标题被压缩 */
}

.schedule-header h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.schedule-list {
  flex: 1;
  overflow-y: auto; /* 只在日程列表内部允许滚动 */
  padding-right: 4px; /* 为隐藏的滚动条留出空间 */
}

/* 隐藏滚动条但保留滚动功能 */
.schedule-list::-webkit-scrollbar {
  width: 4px;
}

.schedule-list::-webkit-scrollbar-track {
  background: transparent;
}

.schedule-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.schedule-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
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
  min-width: 60px;
  margin-right: 16px;
}

.time {
  font-size: 14px;
  font-weight: 600;
  color: #5b7cfa;
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

.meeting-participants {
  font-size: 12px;
  color: #6c757d;
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

/* 圆形添加按钮 */
.add-button-wrapper {
  position: absolute;
  bottom: 40px;
  right: 40px;
}

.add-schedule-btn {
  width: 60px;
  height: 60px;
  box-shadow: 0 4px 16px rgba(91, 124, 250, 0.3);
}

.add-schedule-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(91, 124, 250, 0.4);
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

  .add-button-wrapper {
    position: fixed;
    bottom: 20px;
    right: 20px;
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

  .schedule-item {
    padding: 12px;
  }
}
</style>
