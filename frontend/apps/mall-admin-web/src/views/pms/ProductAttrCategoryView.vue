<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">属性分类</h2>
      <p class="page-desc">管理商品规格参数的分类与属性</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-button type="primary" @click="openAdd">新增属性分类</el-button>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="attributeCount" label="属性数量" width="120">
          <template #default="{ row }">
            {{ (row.attributeList?.length ?? 0) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="viewAttributes(row)">属性列表</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑属性分类' : '新增属性分类'" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 属性列表弹窗 -->
    <el-dialog v-model="attrDialogVisible" :title="`属性列表 - ${currentCategory?.name}`" width="800px">
      <el-button type="primary" size="small" style="margin-bottom: 12px" @click="openAddAttr">新增属性</el-button>
      <el-table :data="attrList" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="属性名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.type === 0 ? '规格' : '参数' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inputType" label="录入方式" width="100">
          <template #default="{ row }">{{ row.inputType === 0 ? '手动' : '列表选择' }}</template>
        </el-table-column>
        <el-table-column prop="inputList" label="可选值" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditAttr(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteAttr(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 属性编辑弹窗 -->
    <el-dialog v-model="attrEditDialogVisible" :title="attrForm.id ? '编辑属性' : '新增属性'" width="600px">
      <el-form :model="attrForm" label-width="100px">
        <el-form-item label="属性名称"><el-input v-model="attrForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="attrForm.type">
            <el-radio :value="0">规格</el-radio>
            <el-radio :value="1">参数</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="录入方式">
          <el-radio-group v-model="attrForm.inputType">
            <el-radio :value="0">手动录入</el-radio>
            <el-radio :value="1">从列表选择</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="attrForm.inputType === 1" label="可选值">
          <el-input v-model="attrForm.inputList" placeholder="逗号分隔" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="attrForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attrEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAttr" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productAttrCategoryApi, productAttributeApi } from '@mall/api-sdk'
import type { ProductAttributeCategory, ProductAttribute } from '@mall/api-sdk/admin/product'

const loading = ref(false)
const saving = ref(false)
const list = ref<ProductAttributeCategory[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const editForm = ref<Partial<ProductAttributeCategory>>({})

// 属性列表
const attrDialogVisible = ref(false)
const attrEditDialogVisible = ref(false)
const currentCategory = ref<ProductAttributeCategory | null>(null)
const attrList = ref<ProductAttribute[]>([])
const attrForm = ref<Partial<ProductAttribute>>({})

async function fetchList() {
  loading.value = true
  try {
    const { data } = await productAttrCategoryApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() {
  editForm.value = {}
  dialogVisible.value = true
}

function handleEdit(row: ProductAttributeCategory) {
  editForm.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editForm.value.id) {
      await productAttrCategoryApi.update(editForm.value.id, editForm.value.name || '')
    } else {
      await productAttrCategoryApi.create(editForm.value.name || '')
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleDelete(row: ProductAttributeCategory) {
  await ElMessageBox.confirm('确定删除该属性分类？', '提示', { type: 'warning' })
  await productAttrCategoryApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function viewAttributes(row: ProductAttributeCategory) {
  currentCategory.value = row
  attrDialogVisible.value = true
  const { data } = await productAttributeApi.list(row.id, { pageNum: 1, pageSize: 100 })
  attrList.value = data.data.list
}

function openAddAttr() {
  attrForm.value = { productAttributeCategoryId: currentCategory.value?.id, type: 0, inputType: 0, sort: 0 }
  attrEditDialogVisible.value = true
}

function handleEditAttr(row: ProductAttribute) {
  attrForm.value = { ...row }
  attrEditDialogVisible.value = true
}

async function handleSaveAttr() {
  saving.value = true
  try {
    if (attrForm.value.id) {
      await productAttributeApi.update(attrForm.value.id, attrForm.value as ProductAttribute)
    } else {
      await productAttributeApi.create(attrForm.value as ProductAttribute)
    }
    ElMessage.success('保存成功')
    attrEditDialogVisible.value = false
    if (currentCategory.value) viewAttributes(currentCategory.value)
  } finally { saving.value = false }
}

async function handleDeleteAttr(row: ProductAttribute) {
  await ElMessageBox.confirm('确定删除该属性？', '提示', { type: 'warning' })
  await productAttributeApi.delete([row.id])
  ElMessage.success('删除成功')
  if (currentCategory.value) viewAttributes(currentCategory.value)
}

onMounted(fetchList)
</script>
