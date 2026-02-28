<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">秒杀活动</h2>
      <p class="page-desc">管理限时秒杀活动及场次设置</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="活动名称">
          <el-input v-model="query.keyword" placeholder="请输入关键词" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增活动</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="活动名称" min-width="200" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上线' : '下线' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="170" />
        <el-table-column prop="endDate" label="结束日期" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewSessions(row)">场次</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleStatus(row)">
              {{ row.status === 1 ? '下线' : '上线' }}
            </el-button>
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

    <!-- 编辑 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑活动' : '新增活动'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="活动名称"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="活动日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 场次管理 -->
    <el-dialog v-model="sessionVisible" :title="`秒杀场次 - ${currentFlash?.title}`" width="700px">
      <el-button type="primary" size="small" style="margin-bottom: 12px" @click="openAddSession">新增场次</el-button>
      <el-table :data="sessions" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v: boolean) => handleSessionStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="editSession(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteSession(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 场次编辑 -->
    <el-dialog v-model="sessionEditVisible" :title="sessionForm.id ? '编辑场次' : '新增场次'" width="500px">
      <el-form :model="sessionForm" label-width="100px">
        <el-form-item label="场次名称"><el-input v-model="sessionForm.name" /></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="sessionForm.startTime" /></el-form-item>
        <el-form-item label="结束时间"><el-time-picker v-model="sessionForm.endTime" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sessionEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSession" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flashApi, flashSessionApi } from '@mall/api-sdk'
import type { FlashPromotion, FlashPromotionSession } from '@mall/api-sdk/admin/marketing'

const loading = ref(false)
const saving = ref(false)
const list = ref<FlashPromotion[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const editForm = ref<Partial<FlashPromotion>>({})
const dateRange = ref<[string, string] | null>(null)

// 场次
const sessionVisible = ref(false)
const sessionEditVisible = ref(false)
const currentFlash = ref<FlashPromotion | null>(null)
const sessions = ref<FlashPromotionSession[]>([])
const sessionForm = ref<Partial<FlashPromotionSession>>({})

async function fetchList() {
  loading.value = true
  try {
    const { data } = await flashApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() {
  editForm.value = {}
  dateRange.value = null
  dialogVisible.value = true
}

function handleEdit(row: FlashPromotion) {
  editForm.value = { ...row }
  dateRange.value = row.startDate && row.endDate ? [row.startDate, row.endDate] : null
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const d = { ...editForm.value } as FlashPromotion
    if (dateRange.value) { d.startDate = dateRange.value[0]; d.endDate = dateRange.value[1] }
    if (d.id) { await flashApi.update(d.id, d) } else { await flashApi.create(d) }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleStatus(row: FlashPromotion) {
  const newStatus = row.status === 1 ? 0 : 1
  await flashApi.updateStatus(row.id, newStatus)
  row.status = newStatus
}

async function handleDelete(row: FlashPromotion) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await flashApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function viewSessions(row: FlashPromotion) {
  currentFlash.value = row
  const { data } = await flashSessionApi.selectList(row.id)
  sessions.value = data.data
  sessionVisible.value = true
}

function openAddSession() {
  sessionForm.value = { status: 1 }
  sessionEditVisible.value = true
}

function editSession(row: FlashPromotionSession) {
  sessionForm.value = { ...row }
  sessionEditVisible.value = true
}

async function saveSession() {
  saving.value = true
  try {
    const d = sessionForm.value as FlashPromotionSession
    if (d.id) { await flashSessionApi.update(d.id, d) } else { await flashSessionApi.create(d) }
    ElMessage.success('保存成功')
    sessionEditVisible.value = false
    if (currentFlash.value) viewSessions(currentFlash.value)
  } finally { saving.value = false }
}

async function handleSessionStatus(row: FlashPromotionSession, val: boolean) {
  await flashSessionApi.updateStatus(row.id, val ? 1 : 0)
  row.status = val ? 1 : 0
}

async function deleteSession(row: FlashPromotionSession) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await flashSessionApi.delete(row.id)
  if (currentFlash.value) viewSessions(currentFlash.value)
}

onMounted(fetchList)
</script>
