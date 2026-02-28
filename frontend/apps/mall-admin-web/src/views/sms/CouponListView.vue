<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">优惠券管理</h2>
      <p class="page-desc">创建和管理优惠券、查看领取使用情况</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="优惠券名称">
          <el-input v-model="query.name" placeholder="请输入名称" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width: 120px">
            <el-option label="全场通用" :value="0" />
            <el-option label="指定分类" :value="1" />
            <el-option label="指定商品" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">搜索</el-button>
          <el-button @click="openAdd">新增优惠券</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="data-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ ['全场通用', '指定分类', '指定商品'][row.type] ?? '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="面额" width="80" />
        <el-table-column prop="minPoint" label="使用门槛" width="90" />
        <el-table-column prop="count" label="总量" width="70" />
        <el-table-column prop="receiveCount" label="已领取" width="70" />
        <el-table-column prop="useCount" label="已使用" width="70" />
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

    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑优惠券' : '新增优惠券'" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editForm.type" style="width: 100%">
            <el-option label="全场通用" :value="0" />
            <el-option label="指定分类" :value="1" />
            <el-option label="指定商品" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="面额"><el-input-number v-model="editForm.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="使用门槛"><el-input-number v-model="editForm.minPoint" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="发行量"><el-input-number v-model="editForm.count" :min="0" /></el-form-item>
        <el-form-item label="每人限领"><el-input-number v-model="editForm.perLimit" :min="1" /></el-form-item>
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
import { couponApi } from '@mall/api-sdk'
import type { Coupon, CouponParam } from '@mall/api-sdk/admin/marketing'

const loading = ref(false)
const saving = ref(false)
const list = ref<Coupon[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, name: '', type: undefined as number | undefined })
const dialogVisible = ref(false)
const editForm = ref<Partial<CouponParam>>({})
const dateRange = ref<[string, string] | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const { data } = await couponApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function openAdd() {
  editForm.value = { type: 0, perLimit: 1, count: 100, amount: 0, minPoint: 0 }
  dateRange.value = null
  dialogVisible.value = true
}

function handleEdit(row: Coupon) {
  editForm.value = { ...row }
  dateRange.value = row.startTime && row.endTime ? [row.startTime, row.endTime] : null
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const formData = { ...editForm.value } as CouponParam
    if (dateRange.value) {
      formData.startTime = dateRange.value[0]
      formData.endTime = dateRange.value[1]
    }
    if (editForm.value.id) {
      await couponApi.update(editForm.value.id, formData)
    } else {
      await couponApi.create(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleDelete(row: Coupon) {
  await ElMessageBox.confirm('确定删除该优惠券？', '提示', { type: 'warning' })
  await couponApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
