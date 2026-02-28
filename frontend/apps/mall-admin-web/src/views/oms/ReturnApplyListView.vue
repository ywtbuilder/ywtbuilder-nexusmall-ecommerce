<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">退货申请</h2>
      <p class="page-desc">审核退货退款申请、查看处理进度</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="处理状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="退货中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已拒绝" :value="3" />
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
        <el-table-column prop="memberUsername" label="用户" width="100" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-image :src="row.productPic" style="width: 40px; height: 40px" fit="cover" />
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="productPrice" label="单价" width="90" />
        <el-table-column prop="productCount" label="数量" width="70" />
        <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : row.status === 2 ? 'success' : row.status === 3 ? 'danger' : 'info'">
              {{ ['待处理', '退货中', '已完成', '已拒绝'][row.status] ?? '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="退货详情" width="600px">
      <template v-if="currentItem">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户">{{ currentItem.memberUsername }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ currentItem.productName }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ currentItem.productPrice }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentItem.productCount }}</el-descriptions-item>
          <el-descriptions-item label="原因" :span="2">{{ currentItem.reason }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentItem.description || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentItem.status === 0" style="margin-top: 16px; text-align: right">
          <el-input v-model="handleNote" placeholder="处理备注" style="width: 300px; margin-right: 8px" />
          <el-button type="success" @click="handleApprove">同意</el-button>
          <el-button type="danger" @click="handleReject">拒绝</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminReturnApplyApi } from '@mall/api-sdk'
import type { ReturnApply } from '@mall/api-sdk/admin/order'

const loading = ref(false)
const list = ref<ReturnApply[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: undefined as number | undefined })
const detailVisible = ref(false)
const currentItem = ref<ReturnApply | null>(null)
const handleNote = ref('')

async function fetchList() {
  loading.value = true
  try {
    const { data } = await adminReturnApplyApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

function viewDetail(row: ReturnApply) {
  currentItem.value = row
  handleNote.value = ''
  detailVisible.value = true
}

async function handleApprove() {
  if (!currentItem.value) return
  await adminReturnApplyApi.updateStatus(currentItem.value.id, { status: 1, handleNote: handleNote.value })
  ElMessage.success('已同意退货')
  detailVisible.value = false
  fetchList()
}

async function handleReject() {
  if (!currentItem.value) return
  await adminReturnApplyApi.updateStatus(currentItem.value.id, { status: 3, handleNote: handleNote.value })
  ElMessage.success('已拒绝退货')
  detailVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>
