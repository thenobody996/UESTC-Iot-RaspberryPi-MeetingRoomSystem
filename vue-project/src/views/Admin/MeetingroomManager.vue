<template>
  <div class="meetingroom-page">
    <div class="toolbar">
      <el-input v-model="filters.name" placeholder="按会议室名称搜索" clearable @clear="onSearchClear" @keyup.enter="fetchRooms">
        <template #append>
          <el-button type="primary" icon="el-icon-search" @click="fetchRooms">搜索</el-button>
        </template>
      </el-input>

      <div class="actions">
        <el-button type="primary" icon="el-icon-plus" @click="openCreate">新建会议室</el-button>
      </div>
    </div>

    <!-- DEBUG: 显示 dialogVisible 状态（临时） -->
    <div v-if="dialogVisible" style="margin:6px 0;padding:6px;background:#eef;">DEBUG: dialogVisible is true</div>

    <div v-if="lastError" class="debug-error" style="margin:12px 0;padding:12px;border-radius:6px;background:#fff3f3;border:1px solid #ffcccc;color:#800">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div><strong>请求错误：</strong>{{ lastError.message }} <span v-if="lastError.status">({{ lastError.status }} {{ lastError.statusText }})</span></div>
        <el-button size="small" @click="lastError = null">关闭</el-button>
      </div>
      <pre style="margin-top:8px;white-space:pre-wrap;word-break:break-word;">{{ JSON.stringify(lastError.data, null, 2) }}</pre>
      <div v-if="lastError && lastError.url" style="margin-top:6px;font-size:12px;color:#666">请求: {{ lastError.url }}</div>
    </div>

    <el-table :data="rooms" stripe style="width: 100%">
      <el-table-column prop="id" label="#" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="管理员" width="160">
        <template #default="{ row }">
          <span>{{ row.manager?.account ?? (row.manager ? String(row.manager) : '-') }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="volume" label="容纳人数" width="120" />
      <el-table-column prop="locateURL" label="位置链接" width="220">
        <template #default="{ row }">
          <a v-if="row.locateURL" :href="row.locateURL" target="_blank">链接</a>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[5,10,20,50]"
        :total="total"
        @size-change="onSizeChange"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog
      :title="dialogTitle"
      :model-value="dialogVisible"
      @update:model-value="handleModelValueUpdate"
      :visible="dialogVisible"
      @update:visible="handleVisibleUpdate"
      width="520px"
    >
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="管理员ID">
          <el-input-number v-model="form.manager_id" :min="1" />
          <div class="hint">如果已有管理员用户，请填写其 user id，否则留空</div>
        </el-form-item>

        <el-form-item label="容纳人数">
          <el-input-number v-model="form.volume" :min="0" />
        </el-form-item>

        <el-form-item label="位置链接">
          <el-input v-model="form.locateURL" placeholder="可选，一般为地图或会议室详情页" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { meetingRoomAPI } from '@/api/meetingroom'
import type { MeetingRoomRequest, MeetingRoom } from '@/types/api'

// --- state ---
const rooms = ref<MeetingRoom[]>([])
const page = ref<number>(1)
const pageSize = ref<number>(10)
const total = ref<number>(0)

// debug: last request error to show on page for easier diagnosis
const lastError = ref<{ message: string; status?: number; statusText?: string; data?: unknown; url?: string } | null>(null)

const filters = reactive({ name: '' })

// dialog / form state
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const form = reactive<MeetingRoomRequest>({ name: '', manager_id: undefined, volume: undefined, description: undefined, locateURL: undefined })

// Helper to parse ambiguous page response shapes
function parsePageResult(data: unknown): { list: MeetingRoom[]; total: number; page: number; pageSize: number } {
  try {
    const d = data as Record<string, unknown> | unknown[]
    // Common shapes: { list, total, page, pageSize }
    if (typeof d === 'object' && d !== null && Array.isArray((d as Record<string, unknown>)['list'])) {
      const dd = d as Record<string, unknown>
      const list = dd['list'] as unknown
      const totalVal = dd['total'] ?? dd['totalCount'] ?? dd['count']
      const pageVal = dd['page']
      const pageSizeVal = dd['pageSize']
      return { list: (list as MeetingRoom[]), total: Number(totalVal ?? (Array.isArray(list) ? (list as unknown[]).length : 0)), page: Number((pageVal as number | undefined) ?? 0) + 1, pageSize: Number((pageSizeVal as number | undefined) ?? pageSize.value) }
    }
    // Spring pageable: { content: [...], totalElements, number, size }
    if (typeof d === 'object' && d !== null && Array.isArray((d as Record<string, unknown>)['content'])) {
      const dd = d as Record<string, unknown>
      const content = dd['content'] as unknown
      const totalVal = dd['totalElements'] ?? dd['total']
      const numVal = dd['number']
      const sizeVal = dd['size']
      return { list: (content as MeetingRoom[]), total: Number(totalVal ?? (Array.isArray(content) ? (content as unknown[]).length : 0)), page: Number((numVal as number | undefined) ?? 0) + 1, pageSize: Number((sizeVal as number | undefined) ?? pageSize.value) }
    }
    // simple array
    if (Array.isArray(d)) {
      return { list: d as MeetingRoom[], total: d.length, page: 1, pageSize: d.length }
    }
  } catch {
    // fallthrough
  }
  return { list: [], total: 0, page: 1, pageSize: pageSize.value }
}

// fetch rooms (either all or by search)
async function fetchRooms() {
  lastError.value = null
  try {
    const resp = (filters.name && filters.name.trim().length > 0)
      ? await meetingRoomAPI.searchRooms(filters.name.trim(), page.value - 1, pageSize.value)
      : await meetingRoomAPI.getAllRooms(page.value - 1, pageSize.value)

    const data = resp.data
    const parsed = parsePageResult(data)
    rooms.value = parsed.list as MeetingRoom[]
    total.value = parsed.total
    // sync current page/pageSize with parsed (in case backend uses 0-based page)
    page.value = parsed.page
    pageSize.value = parsed.pageSize
  } catch (err: unknown) {
    console.error('fetchRooms error', err)
    // better logging for axios errors
    try {
      const e = err as unknown as { isAxiosError?: boolean; config?: Record<string, unknown>; response?: { status?: number; statusText?: string; data?: unknown }; message?: string }
      if (e?.isAxiosError) {
        console.error('Axios error config:', e.config)
        console.error('Axios response:', e.response)
        if (e.response) {
          const status = e.response.status
          const data = e.response.data
          ElMessage.error(`加载会议室失败: ${status} ${e.response.statusText || ''}`)
          console.error('Server response data:', data)
          // populate on-page debug info (ensure types are compatible)
          const url = typeof (e.config as any)?.url === 'string' ? ((e.config as any).url as string) : undefined
          lastError.value = { message: String(e.message ?? 'Axios error'), status, statusText: e.response.statusText, data, url }
           return
         }
       }
     } catch (logErr) {
      console.error('error while logging fetchRooms error', logErr)
    }

    const msg = err instanceof Error ? err.message : String(err)
    lastError.value = { message: msg }
    ElMessage.error(msg || '加载会议室失败')
  }
}

function onSearchClear() {
  filters.name = ''
  page.value = 1
  fetchRooms()
}

function onPageChange(p: number) {
  page.value = p
  fetchRooms()
}

function onSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  fetchRooms()
}

function openCreate() {
  console.debug('openCreate called')
  dialogTitle.value = '新建会议室'
  isEditing.value = false
  editingId.value = null
  Object.assign(form, { name: '', manager_id: undefined, volume: undefined, description: undefined, locateURL: undefined })
  dialogVisible.value = true
  console.debug('dialogVisible set to', dialogVisible.value)
}

function openEdit(row: MeetingRoom) {
  dialogTitle.value = '编辑会议室'
  isEditing.value = true
  editingId.value = row.id
  // try to extract manager id safely from possibly different backend shapes
  let managerId: number | undefined
  const rowRec = row as unknown as Record<string, unknown>
  if (rowRec.manager && typeof rowRec.manager === 'object') {
    const m = rowRec.manager as Record<string, unknown>
    if (typeof m.id === 'number') managerId = m.id
  }
  if (managerId === undefined && typeof rowRec['manager_id'] === 'number') {
    managerId = rowRec['manager_id'] as number
  }
  Object.assign(form, { name: row.name, manager_id: managerId, volume: row.volume, description: row.description, locateURL: row.locateURL })
  dialogVisible.value = true
}

async function save() {
  try {
    // basic validation
    if (!form.name || !form.name.trim()) {
      ElMessage.warning('请输入会议室名称')
      return
    }

    // build payload and omit fields that are not set to avoid sending nulls (server may unbox nulls)
    const payload: Record<string, unknown> = { name: form.name }
    // backend unboxes Integer to int; avoid sending null which causes NullPointerException server-side
    payload.manager_id = typeof form.manager_id === 'number' ? form.manager_id : 0
    payload.volume = typeof form.volume === 'number' ? form.volume : 0
    if (form.description) payload.description = form.description
    if (form.locateURL) payload.locateURL = form.locateURL

    console.debug('creating/updating meetingroom payload:', payload)

    if (isEditing.value && editingId.value != null) {
      await meetingRoomAPI.updateRoom(editingId.value, payload as any)
      ElMessage.success('更新成功')
    } else {
      await meetingRoomAPI.createRoom(payload as any)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchRooms()
  } catch (err: unknown) {
    console.error('save error', err)
    // if axios error, extract response details
    const e = err as unknown as { isAxiosError?: boolean; response?: { status?: number; statusText?: string; data?: unknown }; message?: string }
    if (e?.isAxiosError && e.response) {
      console.error('Server responded with:', e.response)
      lastError.value = { message: String(e.message ?? 'Axios error'), status: e.response.status, statusText: e.response.statusText, data: e.response.data, url: '/api/meetingroom/' }
      ElMessage.error(`保存失败: ${e.response.status} ${e.response.statusText || ''}`)
      return
    }
    const msg = err instanceof Error ? err.message : String(err)
    lastError.value = { message: msg }
    ElMessage.error(msg || '保存失败')
  }
}

async function confirmDelete(row: MeetingRoom) {
  try {
    await ElMessageBox.confirm(`确认删除会议室 「${row.name}」？`, '确认', { type: 'warning' })
    if (row.id != null) {
      await meetingRoomAPI.deleteRoom(row.id)
      ElMessage.success('删除成功')
      fetchRooms()
    }
  } catch (err: unknown) {
    // cancel or error
    if (err === 'cancel' || err === 'close') return
    console.error('delete error', err)
    const msg = err instanceof Error ? err.message : String(err)
    ElMessage.error(msg || '删除失败')
  }
}

function handleModelValueUpdate(v: boolean) {
  dialogVisible.value = v
}

function handleVisibleUpdate(v: boolean) {
  dialogVisible.value = v
}

onMounted(() => {
  fetchRooms()
})
</script>

<style scoped>
.meetingroom-page {
  padding: 16px;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
.toolbar .actions {
  margin-left: auto;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.hint {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}
.debug-error {
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
</style>
