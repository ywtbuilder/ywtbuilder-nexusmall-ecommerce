<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">品牌管理</h2>
      <p class="page-desc">管理品牌信息、Logo及显示状态</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="品牌名称">
          <el-input v-model="query.keyword" placeholder="请输入关键词" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增品牌</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="Logo" width="100">
          <template #default="{ row }">
            <el-image :src="row.logo" style="width: 48px; height: 48px; border-radius: 6px" fit="contain" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="品牌名称" min-width="150" />
        <el-table-column prop="firstLetter" label="首字母" width="80" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="是否显示" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.showStatus === 1" @change="(v: boolean) => handleShowStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="品牌制造商" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.factoryStatus === 1" @change="(v: boolean) => handleFactoryStatus(row, v)" />
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑品牌' : '新增品牌'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="品牌名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="首字母"><el-input v-model="editForm.firstLetter" style="width: 80px" /></el-form-item>
        <el-form-item label="Logo"><el-input v-model="editForm.logo" placeholder="图片URL" /></el-form-item>
        <el-form-item label="大图"><el-input v-model="editForm.bigPic" placeholder="图片URL" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sort" :min="0" /></el-form-item>
        <el-form-item label="品牌故事"><el-input v-model="editForm.brandStory" type="textarea" :rows="3" /></el-form-item>
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
import { brandApi } from '@mall/api-sdk'
import type { Brand } from '@mall/api-sdk/admin/product'

const loading = ref(false)
const saving = ref(false)
const list = ref<Brand[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const editForm = ref<Partial<Brand>>({})

async function fetchList() {
  loading.value = true
  try {
    const { data } = await brandApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() {
  editForm.value = { sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row: Brand) {
  editForm.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editForm.value.id) {
      await brandApi.update(editForm.value.id, editForm.value as Brand)
    } else {
      await brandApi.create(editForm.value as Brand)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleDelete(row: Brand) {
  await ElMessageBox.confirm('确定删除该品牌？', '提示', { type: 'warning' })
  await brandApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleShowStatus(row: Brand, val: boolean) {
  await brandApi.updateShowStatus([row.id], val ? 1 : 0)
  row.showStatus = val ? 1 : 0
}

async function handleFactoryStatus(row: Brand, val: boolean) {
  await brandApi.updateFactoryStatus([row.id], val ? 1 : 0)
  row.factoryStatus = val ? 1 : 0
}

onMounted(fetchList)
</script>
