<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <p class="page-desc">管理商品信息、上下架及推荐状态</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="商品名称">
          <el-input v-model="query.keyword" placeholder="请输入关键词" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-select v-model="query.publishStatus" clearable placeholder="全部" style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image :src="row.pic" style="width: 56px; height: 56px; border-radius: 6px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="productSn" label="货号" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="cell-amount">¥{{ row.price?.toFixed(2) ?? '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sale" label="销量" width="80" />
        <el-table-column label="上架" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.publishStatus === 1" @change="(v: boolean) => handlePublish(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.recommandStatus === 1" @change="(v: boolean) => handleRecommend(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        @change="fetchList"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑商品' : '新增商品'" width="700px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="商品名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="editForm.subTitle" /></el-form-item>
        <el-form-item label="货号"><el-input v-model="editForm.productSn" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="editForm.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="editForm.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="editForm.stock" :min="0" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="editForm.unit" style="width: 120px" /></el-form-item>
        <el-form-item label="商品图片"><el-input v-model="editForm.pic" placeholder="图片URL" /></el-form-item>
        <el-form-item label="商品描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
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
import { productApi } from '@mall/api-sdk'
import type { Product } from '@mall/api-sdk/admin/product'

const loading = ref(false)
const saving = ref(false)
const list = ref<Product[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', publishStatus: undefined as number | undefined })
const dialogVisible = ref(false)
const editForm = ref<Partial<Product>>({})

async function fetchList() {
  loading.value = true
  try {
    const { data } = await productApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

function handleEdit(row: Product) {
  editForm.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editForm.value.id) {
      await productApi.update(editForm.value.id, editForm.value as Product)
    } else {
      await productApi.create(editForm.value as Product)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: Product) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await productApi.updateDeleteStatus([row.id], 1)
  ElMessage.success('删除成功')
  fetchList()
}

async function handlePublish(row: Product, val: boolean) {
  await productApi.updatePublishStatus([row.id], val ? 1 : 0)
  row.publishStatus = val ? 1 : 0
}

async function handleRecommend(row: Product, val: boolean) {
  await productApi.updateRecommendStatus([row.id], val ? 1 : 0)
  row.recommandStatus = val ? 1 : 0
}

onMounted(fetchList)
</script>

<style scoped>
.cell-amount { font-weight: 600; font-variant-numeric: tabular-nums; color: var(--text-primary); }
</style>
