<template>
  <div class="order-detail-page">
    <!-- 顶部导航 -->
    <div class="od-nav">
      <div class="od-nav-inner">
        <span class="od-nav-back" @click="$router.back()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        </span>
        <h1 class="od-nav-title">订单详情</h1>
      </div>
    </div>

    <div v-if="loading" class="od-loading">加载中...</div>

    <div v-else-if="order" class="od-body">
      <div class="od-body-inner">

        <!-- 订单状态 -->
        <section class="od-card od-status-card">
          <div class="od-status-header">
            <span :class="['od-status-badge', 'od-status--' + order.status]">{{ statusText(order.status) }}</span>
            <span class="od-status-hint">{{ statusHint(order.status) }}</span>
          </div>
          <!-- 状态进度条 -->
          <div class="od-progress">
            <div v-for="(step, idx) in steps" :key="idx"
              :class="['od-step', stepActive(idx) && 'od-step--active', stepDone(idx) && 'od-step--done']">
              <div class="od-step-dot"></div>
              <span class="od-step-label">{{ step.label }}</span>
              <span v-if="step.time" class="od-step-time">{{ formatTime(step.time) }}</span>
            </div>
          </div>
        </section>

        <!-- 物流信息 -->
        <section v-if="(order.status ?? 0) >= 2 && order.deliveryCompany" class="od-card">
          <h3 class="od-card-title">物流信息</h3>
          <div class="od-info-row">
            <span class="od-info-label">快递公司</span>
            <span class="od-info-value">{{ order.deliveryCompany }}</span>
          </div>
          <div v-if="order.deliverySn" class="od-info-row">
            <span class="od-info-label">运单号</span>
            <span class="od-info-value">{{ order.deliverySn }}</span>
          </div>
        </section>

        <!-- 收货信息 -->
        <section class="od-card">
          <h3 class="od-card-title">收货信息</h3>
          <div class="od-info-row">
            <span class="od-info-label">收货人</span>
            <span class="od-info-value">{{ order.receiverName }}　{{ maskPhone(order.receiverPhone) }}</span>
          </div>
          <div class="od-info-row">
            <span class="od-info-label">收货地址</span>
            <span class="od-info-value">{{ order.receiverDetailAddress }}</span>
          </div>
        </section>

        <!-- 商品清单 -->
        <section class="od-card">
          <h3 class="od-card-title">商品清单</h3>
          <div v-for="item in (order.orderItemList || [])" :key="item.id" class="od-product">
            <img class="od-product-img" :src="normalizeOrderImage(item.productPic)" :alt="item.productName" />
            <div class="od-product-info">
              <span class="od-product-name">{{ item.productName }}</span>
              <span v-if="item.productAttr" class="od-product-attr">{{ formatAttr(item.productAttr) }}</span>
            </div>
            <div class="od-product-price-area">
              <span class="od-product-price">¥{{ (item.productPrice ?? 0).toFixed(2) }}</span>
              <span class="od-product-qty">x{{ item.productQuantity }}</span>
            </div>
          </div>
        </section>

        <!-- 价格明细 -->
        <section class="od-card">
          <h3 class="od-card-title">价格明细</h3>
          <div class="od-info-row">
            <span class="od-info-label">商品总价</span>
            <span class="od-info-value">¥{{ (order.totalAmount ?? 0).toFixed(2) }}</span>
          </div>
          <div class="od-info-row">
            <span class="od-info-label">运费</span>
            <span class="od-info-value">¥{{ (order.freightAmount ?? 0).toFixed(2) }}</span>
          </div>
          <div class="od-info-row od-info-row--total">
            <span class="od-info-label">实付款</span>
            <span class="od-info-value od-total-price">¥{{ (order.payAmount ?? 0).toFixed(2) }}</span>
          </div>
        </section>

        <!-- 订单信息 -->
        <section class="od-card">
          <h3 class="od-card-title">订单信息</h3>
          <div class="od-info-row">
            <span class="od-info-label">订单编号</span>
            <span class="od-info-value">{{ order.orderSn }}</span>
          </div>
          <div class="od-info-row">
            <span class="od-info-label">下单时间</span>
            <span class="od-info-value">{{ formatTime(order.createTime) }}</span>
          </div>
          <div v-if="order.paymentTime" class="od-info-row">
            <span class="od-info-label">付款时间</span>
            <span class="od-info-value">{{ formatTime(order.paymentTime) }}</span>
          </div>
          <div v-if="order.deliveryTime" class="od-info-row">
            <span class="od-info-label">发货时间</span>
            <span class="od-info-value">{{ formatTime(order.deliveryTime) }}</span>
          </div>
          <div v-if="order.receiveTime" class="od-info-row">
            <span class="od-info-label">收货时间</span>
            <span class="od-info-value">{{ formatTime(order.receiveTime) }}</span>
          </div>
          <div class="od-info-row">
            <span class="od-info-label">支付方式</span>
            <span class="od-info-value">{{ payTypeText(order.payType) }}</span>
          </div>
          <div v-if="order.note" class="od-info-row">
            <span class="od-info-label">备注</span>
            <span class="od-info-value">{{ order.note }}</span>
          </div>
        </section>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div v-if="order" class="od-bottom-bar">
      <div class="od-bottom-inner">
        <button v-if="order.status === 0" class="od-action od-action--primary" @click="handlePay">去付款</button>
        <button v-if="order.status === 0" class="od-action" @click="handleCancel">取消订单</button>
        <button v-if="order.status === 2" class="od-action od-action--primary" @click="handleConfirm">确认收货</button>
        <button v-if="order.status === 3 || order.status === 4" class="od-action od-action--danger" @click="handleDelete">删除订单</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { portalOrderApi } from '@mall/api-sdk/app'
import type { OrderDetail } from '@mall/api-sdk/app/order'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const order = ref<OrderDetail | null>(null)

function statusText(s?: number) {
  return ['待付款', '待发货', '已发货', '已完成', '已关闭'][s ?? 0] ?? '未知'
}

function statusHint(s?: number) {
  switch (s) {
    case 0: return '请尽快完成支付'
    case 1: return '商家正在处理，请耐心等待'
    case 2: return '商品已发出，请注意查收'
    case 3: return '交易已完成'
    case 4: return '订单已关闭'
    default: return ''
  }
}

function payTypeText(t?: number) {
  switch (t) {
    case 0: return '未支付'
    case 1: return '支付宝'
    case 2: return '微信'
    default: return '未知'
  }
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 19)
}

function formatAttr(attr?: string) {
  if (!attr) return ''
  try {
    const arr = JSON.parse(attr)
    if (Array.isArray(arr)) return arr.map((a: { key?: string; value?: string }) => `${a.key || ''}：${a.value || ''}`).join('　')
  } catch { /* not JSON */ }
  return attr
}

function maskPhone(phone?: string) {
  if (!phone || phone.length < 7) return phone ?? ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const orderItemPlaceholder =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" fill="%23f5f5f5"><rect width="120" height="120"/><text x="60" y="62" text-anchor="middle" dominant-baseline="central" font-size="12" fill="%23bbbbbb">暂无图片</text></svg>'
  )

function normalizeOrderImage(url?: string | null): string {
  if (!url) return orderItemPlaceholder
  const clean = url.trim()
  if (clean.startsWith('/api/asset/image/')) return clean
  if (clean.startsWith('data:image/')) return clean
  return orderItemPlaceholder
}

const steps = computed(() => {
  if (!order.value) return []
  const o = order.value
  return [
    { label: '下单', time: o.createTime },
    { label: '付款', time: o.paymentTime },
    { label: '发货', time: o.deliveryTime },
    { label: '收货', time: o.receiveTime },
    { label: '完成', time: o.status === 3 ? o.receiveTime : undefined },
  ]
})

function stepActive(idx: number) {
  const s = order.value?.status ?? 0
  if (s === 4) return false
  return idx === Math.min(s, 4)
}

function stepDone(idx: number) {
  const s = order.value?.status ?? 0
  if (s === 4) return false
  return idx < s
}

async function fetchDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const { data } = await portalOrderApi.detail(id)
    order.value = data.data
  } finally { loading.value = false }
}

async function handlePay() {
  if (!order.value) return
  await portalOrderApi.paySuccess(order.value.id, 1)
  showToast('支付成功')
  fetchDetail()
}

async function handleCancel() {
  if (!order.value) return
  await showConfirmDialog({ title: '确认取消订单？' })
  await portalOrderApi.cancelUserOrder(order.value.id)
  showToast('已取消')
  fetchDetail()
}

async function handleConfirm() {
  if (!order.value) return
  await showConfirmDialog({ title: '确认收货？' })
  await portalOrderApi.confirmReceiveOrder(order.value.id)
  showToast('已确认收货')
  fetchDetail()
}

async function handleDelete() {
  if (!order.value) return
  await showConfirmDialog({ title: '确认删除？' })
  await portalOrderApi.deleteOrder(order.value.id)
  showToast('已删除')
  router.replace('/order/list')
}

onMounted(fetchDetail)
</script>

<style scoped>
/* ============================================================
   订单详情页 — JD 风格
   ============================================================ */
.order-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 70px;
}

/* ── 导航 ── */
.od-nav {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.od-nav-inner {
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 0 16px;
  height: 50px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.od-nav-back {
  display: flex; align-items: center;
  cursor: pointer; color: #666;
  padding: 4px; border-radius: 50%;
  transition: background 150ms;
}
.od-nav-back:hover { background: #f5f5f5; }
.od-nav-title {
  font-size: 18px; font-weight: 700; color: #333; margin: 0;
}

.od-loading {
  text-align: center; padding: 60px 0; color: #999; font-size: 14px;
}

/* ── 主体 ── */
.od-body { padding: 16px 0; }
.od-body-inner {
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 0 16px;
  display: flex; flex-direction: column; gap: 12px;
}

.od-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}
.od-card-title {
  font-size: 15px; font-weight: 700; color: #333;
  margin: 0 0 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}

/* ── 状态卡 ── */
.od-status-card { padding: 20px 16px; }
.od-status-header {
  display: flex; align-items: center; gap: 12px; margin-bottom: 20px;
}
.od-status-badge {
  font-size: 18px; font-weight: 700;
  padding: 4px 14px; border-radius: 6px;
}
.od-status--0 { color: #e3000b; background: #fff0f0; }
.od-status--1 { color: #1890ff; background: #e6f7ff; }
.od-status--2 { color: #fa8c16; background: #fff7e6; }
.od-status--3 { color: #52c41a; background: #f6ffed; }
.od-status--4 { color: #999; background: #f5f5f5; }
.od-status-hint { font-size: 13px; color: #999; }

/* ── 进度条 ── */
.od-progress {
  display: flex; justify-content: space-between;
  position: relative;
}
.od-progress::before {
  content: '';
  position: absolute;
  top: 8px; left: 8px; right: 8px;
  height: 2px; background: #e8e8e8;
}
.od-step {
  display: flex; flex-direction: column; align-items: center;
  gap: 6px; position: relative; z-index: 1;
}
.od-step-dot {
  width: 16px; height: 16px; border-radius: 50%;
  background: #e8e8e8; border: 2px solid #fff;
  box-shadow: 0 0 0 1px #e8e8e8;
  transition: all 200ms;
}
.od-step--done .od-step-dot { background: #52c41a; box-shadow: 0 0 0 1px #52c41a; }
.od-step--active .od-step-dot {
  background: #e3000b; box-shadow: 0 0 0 2px rgba(227, 0, 11, .3);
  width: 18px; height: 18px;
}
.od-step-label { font-size: 12px; color: #999; }
.od-step--done .od-step-label,
.od-step--active .od-step-label { color: #333; font-weight: 600; }
.od-step-time { font-size: 10px; color: #bbb; }

/* ── 信息行 ── */
.od-info-row {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 8px 0; font-size: 13px;
}
.od-info-label { color: #999; flex-shrink: 0; min-width: 70px; }
.od-info-value { color: #333; text-align: right; word-break: break-all; }
.od-info-row--total {
  margin-top: 8px; padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}
.od-total-price { font-size: 18px; font-weight: 700; color: #e3000b; }

/* ── 商品行 ── */
.od-product {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f8f8f8;
}
.od-product:last-child { border-bottom: none; }
.od-product-img {
  width: 80px; height: 80px;
  border-radius: 6px;
  object-fit: cover; background: #f5f5f5;
  flex-shrink: 0;
  border: 1px solid #eee;
}
.od-product-info {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 6px;
}
.od-product-name {
  font-size: 14px; color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.od-product-attr {
  font-size: 12px; color: #999;
  background: #f7f7f7;
  padding: 2px 8px; border-radius: 3px;
  width: fit-content;
}
.od-product-price-area {
  flex-shrink: 0;
  display: flex; flex-direction: column;
  align-items: flex-end; gap: 4px;
}
.od-product-price { font-size: 14px; font-weight: 600; color: #333; }
.od-product-qty { font-size: 12px; color: #999; }

/* ── 底部操作 ── */
.od-bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; border-top: 1px solid #eee;
  z-index: 100;
}
.od-bottom-inner {
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 10px 16px;
  display: flex; justify-content: flex-end; gap: 10px;
}
.od-action {
  padding: 8px 24px; font-size: 14px;
  border-radius: 20px;
  border: 1px solid #ddd; background: #fff; color: #666;
  cursor: pointer; transition: all 150ms;
}
.od-action:hover { border-color: #999; color: #333; }
.od-action--primary { background: #e3000b; color: #fff; border-color: #e3000b; }
.od-action--primary:hover { opacity: .88; }
.od-action--danger { color: #e3000b; border-color: #e3000b; }
.od-action--danger:hover { background: #fff0f0; }

@media (min-width: 1024px) {
  .od-product-img { width: 100px; height: 100px; }
}
</style>

