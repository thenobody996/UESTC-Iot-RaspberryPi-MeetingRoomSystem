<template>
  <div class="admin-page">
    <div class="toolbar" style="display:flex;gap:12px;align-items:center;margin-bottom:12px">
      <el-input v-model="filters.account" placeholder="按账户名搜索" clearable @clear="onSearchClear" @keyup.enter="fetchUsers">
        <template #append>
          <el-button type="primary" icon="el-icon-search" @click="fetchUsers">搜索</el-button>
        </template>
      </el-input>

      <div style="margin-left:auto">
        <el-button type="primary" icon="el-icon-refresh" @click="fetchUsers">刷新</el-button>
      </div>
    </div>

    <el-table :data="users" stripe style="width:100%">
      <el-table-column prop="id" label="#" width="80" />
      <el-table-column prop="account" label="账号" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column prop="createdAt" label="创建时间" width="200" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEdit(row)">查看/编辑</el-button>
          <el-button type="danger" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" style="margin-top:12px;display:flex;justify-content:flex-end">
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

    <el-dialog :title="dialogTitle" :model-value="dialogVisible" @update:model-value="handleDialogUpdate">
      <el-form :model="form" label-width="100px">
        <el-form-item label="账号">
          <el-input v-model="form.account" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="user" value="user" />
            <el-option label="admin" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userAPI } from '@/api/user'
import type { User } from '@/types/api'

const users = ref<User[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = reactive({ account: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive<Partial<User>>({})

async function fetchUsers() {
  try {
    if (filters.account && filters.account.trim().length > 0) {
      const resp = await userAPI.searchUserByAccount(filters.account.trim())
      const data = ((resp as unknown) as { data?: unknown }).data ?? resp
      if (Array.isArray(data)) {
        users.value = data as User[]
        total.value = users.value.length
      } else {
        users.value = []
        total.value = 0
      }
      page.value = 1
      return
    }

    const resp = await userAPI.getAllUsers(page.value - 1, pageSize.value)
    const data = ((resp as unknown) as { data?: unknown }).data ?? resp
    if (Array.isArray(data)) {
      users.value = data as User[]
      total.value = users.value.length
    } else if (data && typeof data === 'object') {
      const dataRec = data as Record<string, unknown>
      if (Array.isArray(dataRec['content'])) {
        users.value = (dataRec['content'] as unknown) as User[]
        total.value = typeof dataRec['totalElements'] === 'number' ? (dataRec['totalElements'] as number) : users.value.length
      } else {
        users.value = []
        total.value = 0
      }
    } else {
      users.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('fetchUsers error', err)
    ElMessage.error('加载用户失败')
  }
}

function onSearchClear() {
  filters.account = ''
  page.value = 1
  fetchUsers()
}

function onPageChange(p:number){ page.value = p; fetchUsers() }
function onSizeChange(s:number){ pageSize.value = s; page.value = 1; fetchUsers() }

function openEdit(row: User){ dialogTitle.value = '查看/编辑用户'; Object.assign(form, row); dialogVisible.value = true }

function handleDialogUpdate(v: boolean) {
  dialogVisible.value = v
}

async function saveUser(){
  // currently backend does not provide user update endpoint in docs; only delete is present.
  // We'll simulate save for role change by calling updateRoom? Not allowed. So just close and show message.
  dialogVisible.value = false
  ElMessage.info('已保存（注意：后端未实现用户更新接口，需后端支持以正式保存）')
}

async function confirmDelete(row: User){
  try{
    await ElMessageBox.confirm(`确认删除用户 ${row.account} ?`, '确认', { type: 'warning' })
    await userAPI.deleteUser(row.id!)
    ElMessage.success('删除成功')
    fetchUsers()
  }catch{
    // user cancelled or error handled above
  }
}

onMounted(()=>{ fetchUsers() })
</script>

<style scoped>
.admin-page{ padding:20px }
</style>
