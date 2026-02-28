<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">资源管理</h2>
      <p class="page-desc">管理后台 API 资源及其分类授权</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="资源名称">
          <el-input v-model="query.nameKeyword" placeholder="请输入名称" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item label="资源路径">
          <el-input v-model="query.urlKeyword" placeholder="请输入路径" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增资源</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="url" label="路径" min-width="200" />
        <el-table-column prop="categoryId" label="分类" width="120">
          <template #default="{ row }">
            {{ categories.find((c: ResourceCategory) => c.id === row.categoryId)?.name ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑资源' : '新增资源'" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="editForm.url" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" /></el-form-item>
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
import { adminRequest } from '@mall/api-sdk'

interface Resource { id: number; name?: string; url?: string; categoryId?: number; description?: string; createTime?: string }
interface ResourceCategory { id: number; name?: string }

const loading = ref(false)
const saving = ref(false)
const list = ref<Resource[]>([])
const total = ref(0)
const categories = ref<ResourceCategory[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, nameKeyword: '', urlKeyword: '', categoryId: undefined as number | undefined })
const dialogVisible = ref(false)
const editForm = ref<Partial<Resource>>({})

async function fetchList() {
  loading.value = true
  try {
    const { data } = await adminRequest.get('/resource/list', { params: query })
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

async function fetchCategories() {
  const { data } = await adminRequest.get('/resourceCategory/listAll')
  categories.value = data.data
}

function openAdd() { editForm.value = {}; dialogVisible.value = true }
function handleEdit(row: Resource) { editForm.value = { ...row }; dialogVisible.value = true }

async function handleSave() {
  saving.value = true
  try {
    if (editForm.value.id) {
      await adminRequest.post(`/resource/update/${editForm.value.id}`, editForm.value)
    } else {
      await adminRequest.post('/resource/create', editForm.value)
    }
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchList()
  } finally { saving.value = false }
}

async function handleDelete(row: Resource) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await adminRequest.post(`/resource/delete/${row.id}`)
  ElMessage.success('删除成功'); fetchList()
}

onMounted(() => { fetchList(); fetchCategories() })
</script>
