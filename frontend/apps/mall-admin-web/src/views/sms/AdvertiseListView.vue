<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">广告管理</h2>
      <p class="page-desc">配置首页轮播广告位、图片及投放状态</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="广告名称">
          <el-input v-model="query.name" placeholder="请输入名称" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增广告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <el-image :src="row.pic" style="width: 80px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">{{ row.type === 0 ? 'PC首页' : 'APP首页' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v: boolean) => handleStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column prop="clickCount" label="点击量" width="80" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑广告' : '新增广告'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editForm.type" style="width: 100%">
            <el-option label="PC首页" :value="0" />
            <el-option label="APP首页" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片"><el-input v-model="editForm.pic" placeholder="图片URL" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="editForm.url" placeholder="跳转URL" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sort" :min="0" /></el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.note" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { advertiseApi } from '@mall/api-sdk'
import type { HomeAdvertise } from '@mall/api-sdk/admin/marketing'

const loading = ref(false)
const saving = ref(false)
const list = ref<HomeAdvertise[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, name: '' })
const dialogVisible = ref(false)
const editForm = ref<Partial<HomeAdvertise>>({})
const dateRange = ref<[string, string] | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const { data } = await advertiseApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() {
  editForm.value = { type: 0, sort: 0, status: 1 }
  dateRange.value = null
  dialogVisible.value = true
}

function handleEdit(row: HomeAdvertise) {
  editForm.value = { ...row }
  dateRange.value = row.startTime && row.endTime ? [row.startTime, row.endTime] : null
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const d = { ...editForm.value } as HomeAdvertise
    if (dateRange.value) { d.startTime = dateRange.value[0]; d.endTime = dateRange.value[1] }
    if (d.id) { await advertiseApi.update(d.id, d) } else { await advertiseApi.create(d) }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleStatus(row: HomeAdvertise, val: boolean) {
  await advertiseApi.updateStatus(row.id, val ? 1 : 0)
  row.status = val ? 1 : 0
}

async function handleDelete(row: HomeAdvertise) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await advertiseApi.delete([row.id])
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
