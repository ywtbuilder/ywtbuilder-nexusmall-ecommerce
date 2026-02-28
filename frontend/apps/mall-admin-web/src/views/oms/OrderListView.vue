<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
      <p class="page-desc">查询订单、处理发货及关闭操作</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-form inline>
        <el-form-item label="订单编号">
          <el-input v-model="query.orderSn" placeholder="请输入订单号" clearable @clear="fetchList" />
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="query.receiverKeyword" placeholder="姓名 / 手机号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已关闭" :value="4" />
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
        <el-table-column prop="orderSn" label="订单编号" width="180" />
        <el-table-column prop="memberUsername" label="用户" width="100" />
        <el-table-column prop="totalAmount" label="订单金额" width="100" />
        <el-table-column prop="payAmount" label="支付金额" width="100" />
        <el-table-column prop="payType" label="支付方式" width="100">
          <template #default="{ row }">{{ row.payType === 0 ? '未支付' : row.payType === 1 ? '支付宝' : '微信' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" link type="primary" @click="handleDelivery(row)">发货</el-button>
            <el-button v-if="row.status <= 1" link type="danger" @click="handleClose(row)">关闭</el-button>
            <el-button v-if="row.status === 4" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ detail.orderSn }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ detail.memberUsername }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ detail.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ detail.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ detail.receiverProvince }}{{ detail.receiverCity }}{{ detail.receiverRegion }}{{ detail.receiverDetailAddress }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.note || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">商品明细</h4>
        <el-table :data="detail.orderItemList" stripe size="small">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 8px">
                <el-image :src="row.productPic" style="width: 40px; height: 40px" fit="cover" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="productPrice" label="单价" width="90" />
          <el-table-column prop="productQuantity" label="数量" width="70" />
          <el-table-column prop="productAttr" label="规格" width="120" show-overflow-tooltip />
        </el-table>
      </template>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="deliveryVisible" title="订单发货" width="500px">
      <el-form :model="deliveryForm" label-width="100px">
        <el-form-item label="物流公司"><el-input v-model="deliveryForm.deliveryCompany" /></el-form-item>
        <el-form-item label="物流单号"><el-input v-model="deliveryForm.deliverySn" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliveryVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDelivery" :loading="saving">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@mall/api-sdk'
import type { Order, OrderDetail } from '@mall/api-sdk/admin/order'

const loading = ref(false)
const saving = ref(false)
const list = ref<Order[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, orderSn: '', receiverKeyword: '', status: undefined as number | undefined })
const detailVisible = ref(false)
const detail = ref<OrderDetail | null>(null)
const deliveryVisible = ref(false)
const deliveryForm = reactive({ orderId: 0, deliveryCompany: '', deliverySn: '' })

function statusText(s?: number) {
  const m: Record<number, string> = { 0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已关闭', 5: '无效' }
  return m[s ?? -1] ?? '未知'
}
function statusType(s?: number) {
  const m: Record<number, string> = { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger', 5: 'info' }
  return (m[s ?? -1] ?? 'info') as 'warning' | 'primary' | 'info' | 'success' | 'danger' | ''
}

async function fetchList() {
  loading.value = true
  try {
    const { data } = await orderApi.list(query)
    list.value = data.data.list
    total.value = data.data.total
  } finally { loading.value = false }
}

async function viewDetail(row: Order) {
  const { data } = await orderApi.detail(row.id)
  detail.value = data.data
  detailVisible.value = true
}

function handleDelivery(row: Order) {
  deliveryForm.orderId = row.id
  deliveryForm.deliveryCompany = ''
  deliveryForm.deliverySn = ''
  deliveryVisible.value = true
}

async function submitDelivery() {
  saving.value = true
  try {
    await orderApi.delivery([deliveryForm])
    ElMessage.success('发货成功')
    deliveryVisible.value = false
    fetchList()
  } finally { saving.value = false }
}

async function handleClose(row: Order) {
  const res = await ElMessageBox.prompt('请输入关闭原因', '关闭订单', { inputPlaceholder: '原因' })
  const reason = typeof res === 'string' ? res : (res as { value: string }).value
  await orderApi.close([row.id], reason)
  ElMessage.success('关闭成功')
  fetchList()
}

async function handleDelete(row: Order) {
  await ElMessageBox.confirm('确定删除该订单？', '提示', { type: 'warning' })
  await orderApi.delete([row.id])
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
