<template>
  <div class="dashboard">
    <!-- ═══ Stat Cards — Bento Grid ═══ -->
    <div class="bento-grid stagger">
      <div class="bento-card stat-card animate-fade-in-up" v-for="(stat, i) in statCards" :key="i">
        <div class="stat-icon" :style="{ background: stat.iconBg }">
          <div v-html="stat.icon"></div>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-value">{{ stat.value }}</div>
        </div>
        <div class="stat-trend" v-if="stat.trend">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
            <polyline points="17 6 23 6 23 12"/>
          </svg>
          <span>{{ stat.trend }}</span>
        </div>
      </div>

      <!-- ═══ Quick Actions Card ═══ -->
      <div class="bento-card actions-card animate-fade-in-up">
        <div class="card-header">
          <span class="card-title">快捷操作</span>
        </div>
        <div class="action-grid">
          <button class="action-btn" @click="$router.push('/pms/product')">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            添加商品
          </button>
          <button class="action-btn" @click="$router.push('/oms/order')">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            处理订单
          </button>
          <button class="action-btn" @click="$router.push('/sms/coupon')">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M20 12V8H6a2 2 0 01-2-2c0-1.1.9-2 2-2h12v4"/><path d="M4 6v12c0 1.1.9 2 2 2h14v-4"/></svg>
            发放优惠券
          </button>
          <button class="action-btn" @click="$router.push('/sms/advertise')">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
            配置广告
          </button>
        </div>
      </div>
    </div>

    <!-- ═══ Recent Orders Table ═══ -->
    <div class="recent-orders animate-fade-in-up" style="animation-delay: 300ms">
      <div class="section-header">
        <h3 class="section-title">最近订单</h3>
        <button class="view-all-btn" @click="$router.push('/oms/order')">
          查看全部
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>
      <el-card>
        <el-table :data="recentOrders" stripe>
          <el-table-column prop="orderSn" label="订单编号" min-width="180" />
          <el-table-column prop="memberUsername" label="用户" width="120" />
          <el-table-column label="支付金额" width="120">
            <template #default="{ row }">
              <span class="amount">¥{{ row.payAmount?.toFixed(2) ?? '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="orderStatusType(row.status)" size="small">
                {{ orderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { orderApi } from '@mall/api-sdk'
import type { Order } from '@mall/api-sdk/admin/order'

const stats = reactive({ productCount: 0, orderCount: 0, brandCount: 0, couponCount: 0 })
const recentOrders = ref<Order[]>([])

const statCards = ref([
  {
    label: '商品总数',
    value: '--',
    trend: '+12%',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg>',
    iconBg: 'rgba(212,168,83,.1)',
  },
  {
    label: '订单总数',
    value: '--',
    trend: '+8%',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>',
    iconBg: 'rgba(93,171,128,.1)',
  },
  {
    label: '品牌总数',
    value: '--',
    trend: '',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>',
    iconBg: 'rgba(122,139,168,.1)',
  },
  {
    label: '优惠券总数',
    value: '--',
    trend: '+3%',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M20 12V8H6a2 2 0 01-2-2c0-1.1.9-2 2-2h12v4"/><path d="M4 6v12c0 1.1.9 2 2 2h14v-4"/></svg>',
    iconBg: 'rgba(204,136,68,.1)',
  },
])

function orderStatusText(status?: number) {
  const map: Record<number, string> = { 0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已关闭', 5: '无效' }
  return map[status ?? -1] ?? '未知'
}

function orderStatusType(status?: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger', 5: 'info' }
  return (map[status ?? -1] ?? 'info') as 'warning' | 'primary' | 'info' | 'success' | 'danger'
}

onMounted(async () => {
  try {
    const { data } = await orderApi.list({ pageNum: 1, pageSize: 8 })
    recentOrders.value = data.data.list
    stats.orderCount = data.data.total
    statCards.value[1].value = String(data.data.total)
  } catch { /* backend may not be running */ }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

/* ── Bento Grid ── */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.bento-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-card);
  transition: border-color var(--duration-normal) var(--ease-out),
              transform var(--duration-normal) var(--ease-out);
}
.bento-card:hover {
  border-color: var(--border-default);
  transform: translateY(-2px);
}

/* ── Stat Cards ── */
.stat-card {
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  height: 80px;
  border-radius: 0 var(--radius-md) 0 80px;
  opacity: 0.03;
  background: var(--accent-gold);
  pointer-events: none;
}
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  color: var(--accent-gold);
}
.stat-body {}
.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 500;
  color: var(--accent-green);
  background: rgba(93,171,128,.08);
  padding: 2px 8px;
  border-radius: 100px;
}

/* ── Actions Card ── */
.actions-card {
  grid-column: span 4;
}
.card-header {
  margin-bottom: 16px;
}
.card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
}
.action-btn:hover {
  background: var(--bg-hover);
  border-color: var(--border-default);
  color: var(--accent-gold);
  transform: translateY(-1px);
}

/* ── Recent Orders ── */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.view-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--accent-gold);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: opacity var(--duration-fast);
}
.view-all-btn:hover {
  opacity: 0.8;
}

.amount {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
}

@media (max-width: 960px) {
  .bento-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .actions-card {
    grid-column: span 2;
  }
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
