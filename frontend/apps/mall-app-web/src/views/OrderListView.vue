<template>
  <div class="order-list-page">
    <!-- 顶部导航 -->
    <div class="ol-nav">
      <div class="ol-nav-inner">
        <span class="ol-nav-back" @click="$router.back()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        </span>
        <h1 class="ol-nav-title">我的订单</h1>
      </div>
    </div>

    <!-- Tab 筛选 -->
    <div class="ol-tabs">
      <div class="ol-tabs-inner">
        <button v-for="tab in tabs" :key="tab.value"
          :class="['ol-tab', activeTab === tab.value && 'ol-tab--active']"
          @click="switchTab(tab.value)">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="ol-body">
      <div class="ol-body-inner">
        <div v-for="order in list" :key="order.id" class="order-card"
          @click="goDetail(order.id)">
          <!-- 订单头 -->
          <div class="oc-header">
            <div class="oc-header-left">
              <span class="oc-time">{{ formatTime(order.createTime) }}</span>
              <span class="oc-sn">订单号：{{ order.orderSn }}</span>
            </div>
            <span :class="['oc-status', 'oc-status--' + order.status]">{{ statusText(order.status) }}</span>
          </div>

          <!-- 商品行 -->
          <div class="oc-items">
            <div v-for="item in (order.orderItemList || [])" :key="item.id" class="oc-item">
              <img class="oc-item-img" :src="normalizeOrderImage(item.productPic)" :alt="item.productName" />
              <div class="oc-item-info">
                <span class="oc-item-name">{{ item.productName }}</span>
                <span v-if="item.productAttr" class="oc-item-attr">{{ formatAttr(item.productAttr) }}</span>
              </div>
              <div class="oc-item-price">
                <span class="oc-item-unit-price">¥{{ (item.productPrice ?? 0).toFixed(2) }}</span>
                <span class="oc-item-qty">x{{ item.productQuantity }}</span>
              </div>
            </div>
            <div v-if="!order.orderItemList?.length" class="oc-item oc-item--empty">
              <span class="oc-item-name" style="color: #999">（暂无商品明细）</span>
            </div>
          </div>

          <!-- 底部：金额 + 操作 -->
          <div class="oc-footer">
            <div class="oc-total">
              共 {{ totalQty(order) }} 件商品&nbsp;&nbsp;合计：<b class="oc-pay-amount">¥{{ (order.payAmount ?? 0).toFixed(2) }}</b>
            </div>
            <div class="oc-actions" @click.stop>
              <button v-if="order.status === 0" class="oc-btn oc-btn--primary" @click="handlePay(order)">去付款</button>
              <button v-if="order.status === 0" class="oc-btn" @click="handleCancel(order)">取消订单</button>
              <button v-if="order.status === 2" class="oc-btn oc-btn--primary" @click="handleConfirm(order)">确认收货</button>
              <button v-if="order.status === 3 || order.status === 4" class="oc-btn oc-btn--danger" @click="handleDelete(order)">删除</button>
              <button class="oc-btn" @click="goDetail(order.id)">查看详情</button>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="!finished" class="ol-load-more">
          <button v-if="!loading" class="oc-btn" @click="loadMore">加载更多</button>
          <span v-else class="ol-loading-text">加载中...</span>
        </div>
        <div v-else-if="list.length" class="ol-finished">没有更多了</div>

        <!-- 空状态 -->
        <div v-if="!loading && !list.length" class="ol-empty">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
          <p>暂无订单</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { portalOrderApi } from '@mall/api-sdk/app'
import type { OrderDetail } from '@mall/api-sdk/app/order'

const router = useRouter()

const tabs = [
  { label: '全部', value: -1 },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
]

const activeTab = ref(-1)
const list = ref<OrderDetail[]>([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)

function statusText(s?: number) {
  return ['待付款', '待发货', '已发货', '已完成', '已关闭'][s ?? 0] ?? '未知'
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

function totalQty(order: OrderDetail) {
  return (order.orderItemList || []).reduce((s, i) => s + (i.productQuantity ?? 0), 0)
}

function goDetail(orderId: number) {
  router.push(`/order/detail/${orderId}`)
}

async function loadMore() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: pageNum.value, pageSize: 10 }
    if (activeTab.value >= 0) params.status = activeTab.value
    const { data } = await portalOrderApi.list(params)
    list.value.push(...data.data.list)
    if (data.data.list.length < 10) finished.value = true
    pageNum.value++
  } finally { loading.value = false }
}

function switchTab(val: number) {
  activeTab.value = val
  list.value = []
  pageNum.value = 1
  finished.value = false
  loadMore()
}

async function handlePay(order: OrderDetail) {
  await portalOrderApi.paySuccess(order.id, 1)
  showToast('支付成功')
  switchTab(activeTab.value)
}

async function handleCancel(order: OrderDetail) {
  await showConfirmDialog({ title: '确认取消订单？' })
  await portalOrderApi.cancelUserOrder(order.id)
  showToast('已取消')
  switchTab(activeTab.value)
}

async function handleConfirm(order: OrderDetail) {
  await showConfirmDialog({ title: '确认收货？' })
  await portalOrderApi.confirmReceiveOrder(order.id)
  showToast('已确认收货')
  switchTab(activeTab.value)
}

async function handleDelete(order: OrderDetail) {
  await showConfirmDialog({ title: '确认删除？' })
  await portalOrderApi.deleteOrder(order.id)
  showToast('已删除')
  list.value = list.value.filter(o => o.id !== order.id)
}

onMounted(() => loadMore())
</script>

<style scoped>
/* ============================================================
   订单列表页 — JD 风格
   ============================================================ */
.order-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* ── 顶部导航 ── */
.ol-nav {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.ol-nav-inner {
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 0 16px;
  height: 50px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.ol-nav-back {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
  padding: 4px;
  border-radius: 50%;
  transition: background 150ms;
}
.ol-nav-back:hover { background: #f5f5f5; }
.ol-nav-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

/* ── Tabs ── */
.ol-tabs {
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
}
.ol-tabs-inner {
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  gap: 0;
}
.ol-tab {
  position: relative;
  background: none;
  border: none;
  padding: 14px 24px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 150ms;
  white-space: nowrap;
}
.ol-tab:hover { color: #333; }
.ol-tab--active {
  color: #e3000b;
  font-weight: 600;
}
.ol-tab--active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: #e3000b;
  border-radius: 1px;
}

/* ── Body ── */
.ol-body { padding: 16px 0; }
.ol-body-inner {
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 0 16px;
}

/* ── Order Card ── */
.order-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 200ms;
  border: 1px solid #f0f0f0;
}
.order-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,.08); }

.oc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.oc-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}
.oc-sn { color: #666; }
.oc-status {
  font-size: 13px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 4px;
}
.oc-status--0 { color: #e3000b; background: #fff0f0; }
.oc-status--1 { color: #1890ff; background: #e6f7ff; }
.oc-status--2 { color: #fa8c16; background: #fff7e6; }
.oc-status--3 { color: #52c41a; background: #f6ffed; }
.oc-status--4 { color: #999; background: #f5f5f5; }

.oc-items { padding: 0 16px; }
.oc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f8f8f8;
}
.oc-item:last-child { border-bottom: none; }
.oc-item-img {
  width: 80px; height: 80px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f5f5;
  flex-shrink: 0;
  border: 1px solid #eee;
}
.oc-item-info {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 6px;
}
.oc-item-name {
  font-size: 14px; color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.oc-item-attr {
  font-size: 12px; color: #999;
  background: #f7f7f7;
  padding: 2px 8px; border-radius: 3px;
  width: fit-content;
}
.oc-item-price {
  flex-shrink: 0;
  display: flex; flex-direction: column;
  align-items: flex-end; gap: 4px;
}
.oc-item-unit-price { font-size: 14px; font-weight: 600; color: #333; }
.oc-item-qty { font-size: 12px; color: #999; }

.oc-footer {
  display: flex; align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap; gap: 8px;
}
.oc-total { font-size: 13px; color: #666; }
.oc-pay-amount { font-size: 16px; color: #e3000b; font-weight: 700; }
.oc-actions { display: flex; gap: 8px; flex-wrap: wrap; }

.oc-btn {
  padding: 6px 16px; font-size: 13px;
  border-radius: 16px;
  border: 1px solid #ddd; background: #fff; color: #666;
  cursor: pointer; transition: all 150ms; white-space: nowrap;
}
.oc-btn:hover { border-color: #999; color: #333; }
.oc-btn--primary { background: #e3000b; color: #fff; border-color: #e3000b; }
.oc-btn--primary:hover { opacity: 0.88; border-color: #c00; }
.oc-btn--danger { color: #e3000b; border-color: #e3000b; }
.oc-btn--danger:hover { background: #fff0f0; }

.ol-load-more, .ol-finished {
  text-align: center; padding: 16px; font-size: 13px; color: #999;
}
.ol-loading-text { color: #bbb; }
.ol-empty { text-align: center; padding: 60px 0; color: #ccc; }
.ol-empty p { margin-top: 12px; font-size: 14px; }

@media (min-width: 1024px) {
  .ol-tab { padding: 14px 32px; }
  .oc-item-img { width: 100px; height: 100px; }
}
</style>

