<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">菜单管理</h2>
      <p class="page-desc">配置系统菜单层级、显示及图标</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-button type="primary" @click="openAdd">新增菜单</el-button>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" row-key="id" stripe default-expand-all>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="菜单名称" min-width="200" />
        <el-table-column prop="level" label="级别" width="80" />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column label="是否显示" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.hidden === 0" @change="(v: boolean) => handleHidden(row, v)" />
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑菜单' : '新增菜单'" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="菜单名称"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="上级菜单">
          <el-select v-model="editForm.parentId" clearable placeholder="无（顶级）" style="width: 100%">
            <el-option v-for="m in topMenus" :key="m.id" :label="m.title" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="前端名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="editForm.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sort" :min="0" /></el-form-item>
        <el-form-item label="是否显示">
          <el-radio-group v-model="editForm.hidden">
            <el-radio :value="0">是</el-radio>
            <el-radio :value="1">否</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { adminRequest } from '@mall/api-sdk'

interface Menu { id: number; parentId?: number; title?: string; name?: string; icon?: string; level?: number; hidden?: number; sort?: number; children?: Menu[] }

const loading = ref(false)
const saving = ref(false)
const list = ref<Menu[]>([])
const dialogVisible = ref(false)
const editForm = ref<Partial<Menu>>({})

const topMenus = computed(() => list.value.filter(m => !m.parentId || m.parentId === 0))

async function fetchList() {
  loading.value = true
  try {
    const { data } = await adminRequest.get('/menu/treeList')
    list.value = data.data
  } finally { loading.value = false }
}

function openAdd() { editForm.value = { parentId: 0, hidden: 0, sort: 0 }; dialogVisible.value = true }
function handleEdit(row: Menu) { editForm.value = { ...row }; dialogVisible.value = true }

async function handleSave() {
  saving.value = true
  try {
    const formData = { ...editForm.value, level: editForm.value.parentId ? 1 : 0 }
    if (editForm.value.id) {
      await adminRequest.post(`/menu/update/${editForm.value.id}`, formData)
    } else {
      await adminRequest.post('/menu/create', formData)
    }
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchList()
  } finally { saving.value = false }
}

async function handleHidden(row: Menu, val: boolean) {
  await adminRequest.post(`/menu/updateHidden/${row.id}`, null, { params: { hidden: val ? 0 : 1 } })
  row.hidden = val ? 0 : 1
}

async function handleDelete(row: Menu) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await adminRequest.post(`/menu/delete/${row.id}`)
  ElMessage.success('删除成功'); fetchList()
}

onMounted(fetchList)
</script>
