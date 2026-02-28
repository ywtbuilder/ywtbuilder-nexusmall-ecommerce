<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
      <p class="page-desc">配置角色权限、分配菜单与资源访问</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="角色名称">
          <el-input v-model="query.keyword" placeholder="请输入角色名" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="角色名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="adminCount" label="用户数" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v: boolean) => handleStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="allocMenu(row)">分配菜单</el-button>
            <el-button link type="primary" @click="allocResource(row)">分配资源</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="角色名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <el-tree ref="menuTreeRef" :data="allMenus" show-checkbox node-key="id" :default-checked-keys="checkedMenuIds" :props="{ label: 'title', children: 'children' }" />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenu" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配资源 -->
    <el-dialog v-model="resourceDialogVisible" title="分配资源" width="500px">
      <el-checkbox-group v-model="checkedResourceIds">
        <el-checkbox v-for="r in allResources" :key="r.id" :label="r.name" :value="r.id" />
      </el-checkbox-group>
      <template #footer>
        <el-button @click="resourceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResource" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElTree } from 'element-plus'
import { adminRequest } from '@mall/api-sdk'

interface Role { id: number; name?: string; description?: string; adminCount?: number; status?: number; createTime?: string }
interface Resource { id: number; name?: string }

const loading = ref(false)
const saving = ref(false)
const list = ref<Role[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const editForm = ref<Partial<Role>>({})

const menuDialogVisible = ref(false)
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const allMenus = ref<unknown[]>([])
const checkedMenuIds = ref<number[]>([])
let currentRoleId = 0

const resourceDialogVisible = ref(false)
const allResources = ref<Resource[]>([])
const checkedResourceIds = ref<number[]>([])

async function fetchList() {
  loading.value = true
  try {
    const { data } = await adminRequest.get('/role/list', { params: query })
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() { editForm.value = { status: 1 }; dialogVisible.value = true }
function handleEdit(row: Role) { editForm.value = { ...row }; dialogVisible.value = true }

async function handleSave() {
  saving.value = true
  try {
    if (editForm.value.id) {
      await adminRequest.post(`/role/update/${editForm.value.id}`, editForm.value)
    } else {
      await adminRequest.post('/role/create', editForm.value)
    }
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchList()
  } finally { saving.value = false }
}

async function handleStatus(row: Role, val: boolean) {
  await adminRequest.post(`/role/updateStatus/${row.id}`, null, { params: { status: val ? 1 : 0 } })
  row.status = val ? 1 : 0
}

async function handleDelete(row: Role) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await adminRequest.post('/role/delete', null, { params: { ids: row.id } })
  ElMessage.success('删除成功'); fetchList()
}

async function allocMenu(row: Role) {
  currentRoleId = row.id
  const [menuRes, allocRes] = await Promise.all([
    adminRequest.get('/menu/treeList'),
    adminRequest.get(`/role/listMenu/${row.id}`),
  ])
  allMenus.value = menuRes.data.data
  checkedMenuIds.value = (allocRes.data.data as { id: number }[]).map(m => m.id)
  menuDialogVisible.value = true
}

async function saveMenu() {
  saving.value = true
  try {
    const ids = menuTreeRef.value?.getCheckedKeys() ?? []
    await adminRequest.post('/role/allocMenu', null, { params: { roleId: currentRoleId, menuIds: (ids as number[]).join(',') } })
    ElMessage.success('分配成功'); menuDialogVisible.value = false
  } finally { saving.value = false }
}

async function allocResource(row: Role) {
  currentRoleId = row.id
  const [resAll, resAllocated] = await Promise.all([
    adminRequest.get('/resource/listAll'),
    adminRequest.get(`/role/listResource/${row.id}`),
  ])
  allResources.value = resAll.data.data
  checkedResourceIds.value = (resAllocated.data.data as { id: number }[]).map(r => r.id)
  resourceDialogVisible.value = true
}

async function saveResource() {
  saving.value = true
  try {
    await adminRequest.post('/role/allocResource', null, { params: { roleId: currentRoleId, resourceIds: checkedResourceIds.value.join(',') } })
    ElMessage.success('分配成功'); resourceDialogVisible.value = false
  } finally { saving.value = false }
}

onMounted(fetchList)
</script>
