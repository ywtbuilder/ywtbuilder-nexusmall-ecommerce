<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商品分类</h2>
      <p class="page-desc">管理商品分类层级、导航及显示设置</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-button type="primary" @click="openAdd">新增分类</el-button>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" row-key="id" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图标" width="80">
          <template #default="{ row }">
            <el-image v-if="row.icon" :src="row.icon" style="width: 40px; height: 40px" fit="contain" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.level === 0 ? '一级' : '二级' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productCount" label="商品数量" width="100" />
        <el-table-column prop="productUnit" label="单位" width="80" />
        <el-table-column label="导航栏" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.navStatus === 1" @change="(v: boolean) => handleNavStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="是否显示" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.showStatus === 1" @change="(v: boolean) => handleShowStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑分类' : '新增分类'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="分类名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="editForm.parentId" clearable placeholder="无（一级分类）" style="width: 100%">
            <el-option v-for="c in topCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位"><el-input v-model="editForm.productUnit" style="width: 120px" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="editForm.icon" placeholder="图标URL" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sort" :min="0" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="editForm.keywords" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productCategoryApi } from '@mall/api-sdk'
import type { ProductCategory } from '@mall/api-sdk/admin/product'

const loading = ref(false)
const saving = ref(false)
const list = ref<ProductCategory[]>([])
const dialogVisible = ref(false)
const editForm = ref<Partial<ProductCategory>>({})

const topCategories = computed(() => list.value.filter(c => c.level === 0))

async function fetchList() {
  loading.value = true
  try {
    const { data } = await productCategoryApi.listWithChildren()
    // flatten the tree for display
    const flat: ProductCategory[] = []
    for (const cat of data.data) {
      flat.push(cat)
      if (cat.children) {
        for (const child of cat.children) {
          flat.push(child)
        }
      }
    }
    list.value = flat
  } finally { loading.value = false }
}

function openAdd() {
  editForm.value = { parentId: 0, sort: 0, level: 0 }
  dialogVisible.value = true
}

function handleEdit(row: ProductCategory) {
  editForm.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const formData = { ...editForm.value, level: editForm.value.parentId ? 1 : 0 }
    if (editForm.value.id) {
      await productCategoryApi.update(editForm.value.id, formData as Record<string, unknown>)
    } else {
      await productCategoryApi.create(formData as Record<string, unknown>)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleDelete(row: ProductCategory) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await productCategoryApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleNavStatus(row: ProductCategory, val: boolean) {
  await productCategoryApi.updateNavStatus([row.id], val ? 1 : 0)
  row.navStatus = val ? 1 : 0
}

async function handleShowStatus(row: ProductCategory, val: boolean) {
  await productCategoryApi.updateShowStatus([row.id], val ? 1 : 0)
  row.showStatus = val ? 1 : 0
}

onMounted(fetchList)
</script>
