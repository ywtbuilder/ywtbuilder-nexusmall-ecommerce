<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <p class="page-desc">管理后台用户账户、状态及角色分配</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="用户名">
          <el-input v-model="query.keyword" placeholder="请输入用户名" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickName" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAllocRole(row)">分配角色</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        background layout="total, prev, pager, next, sizes"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @change="fetchList"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="editForm.username" :disabled="!!editForm.id" /></el-form-item>
        <el-form-item v-if="!editForm.id" label="密码"><el-input v-model="editForm.password" type="password" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="editForm.nickName" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.note" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.name" :value="r.id" />
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminRequest } from '@mall/api-sdk'

interface Admin { id: number; username?: string; password?: string; nickName?: string; email?: string; status?: number; note?: string; createTime?: string }
interface Role { id: number; name?: string }

const loading = ref(false)
const saving = ref(false)
const list = ref<Admin[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const editForm = ref<Partial<Admin>>({})
const roleDialogVisible = ref(false)
const allRoles = ref<Role[]>([])
const selectedRoleIds = ref<number[]>([])
let currentAdminId = 0

async function fetchList() {
  loading.value = true
  try {
    const { data } = await adminRequest.get('/admin/list', { params: query })
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() { editForm.value = { status: 1 }; dialogVisible.value = true }
function handleEdit(row: Admin) { editForm.value = { ...row }; dialogVisible.value = true }

async function handleSave() {
  saving.value = true
  try {
    if (editForm.value.id) {
      await adminRequest.post(`/admin/update/${editForm.value.id}`, editForm.value)
    } else {
      await adminRequest.post('/admin/register', editForm.value)
    }
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchList()
  } finally { saving.value = false }
}

async function handleDelete(row: Admin) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await adminRequest.post(`/admin/delete/${row.id}`)
  ElMessage.success('删除成功'); fetchList()
}

async function handleAllocRole(row: Admin) {
  currentAdminId = row.id
  const [rolesRes, allocatedRes] = await Promise.all([
    adminRequest.get('/role/listAll'),
    adminRequest.get(`/admin/role/${row.id}`),
  ])
  allRoles.value = rolesRes.data.data
  selectedRoleIds.value = (allocatedRes.data.data as Role[]).map((r: Role) => r.id)
  roleDialogVisible.value = true
}

async function saveRole() {
  saving.value = true
  try {
    await adminRequest.post('/admin/role/update', null, { params: { adminId: currentAdminId, roleIds: selectedRoleIds.value.join(',') } })
    ElMessage.success('分配成功'); roleDialogVisible.value = false
  } finally { saving.value = false }
}

onMounted(fetchList)
</script>
