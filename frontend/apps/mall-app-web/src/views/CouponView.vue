<template>
  <div class="coupon-page">
    <van-nav-bar title="我的优惠券" left-arrow @click-left="$router.back()" />

    <van-tabs v-model:active="activeTab" sticky offset-top="46" @change="reload">
      <van-tab title="未使用" name="1" />
      <van-tab title="已使用" name="2" />
      <van-tab title="已过期" name="3" />
    </van-tabs>

    <div class="list-wrap">
      <!-- 加载中骨架屏 -->
      <template v-if="loading && list.length === 0">
        <div v-for="i in 3" :key="i" class="coupon-skeleton">
          <van-skeleton :row="2" row-width="['60%','40%']" />
        </div>
      </template>

      <!-- 优惠券卡片列表 -->
      <div v-for="c in list" :key="c.id" :class="['coupon-card', statusClass]">
        <div class="coupon-left">
          <div class="coupon-amount">
            <span class="unit">¥</span>{{ c.couponAmount ?? 0 }}
          </div>
          <div class="coupon-threshold">
            满 {{ c.couponMinPoint ?? 0 }} 元可用
          </div>
        </div>
        <div class="coupon-divider">
          <div class="dot dot-top"></div>
          <div class="dash-line"></div>
          <div class="dot dot-bottom"></div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ c.couponName || '优惠券' }}</div>
          <div class="coupon-code" v-if="c.couponCode">券码：{{ c.couponCode }}</div>
          <div class="coupon-date" v-if="c.createTime">
            {{ activeTab === '1' ? '已领取' : activeTab === '2' ? '使用时间：' + formatDate(c.useTime) : '已过期' }}
          </div>
          <button v-if="activeTab === '1'" class="use-btn" @click="$router.push('/')">立即使用</button>
          <div v-else class="used-tag">{{ activeTab === '2' ? '已使用' : '已过期' }}</div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && list.length === 0" class="empty-wrap">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <rect x="10" y="22" width="60" height="36" rx="6" fill="#f5e6e6" stroke="#e3000b" stroke-width="1.5"/>
          <circle cx="10" cy="40" r="6" fill="#fff" stroke="#e3000b" stroke-width="1.5"/>
          <circle cx="70" cy="40" r="6" fill="#fff" stroke="#e3000b" stroke-width="1.5"/>
          <path d="M10 40 h60" stroke="#e3000b" stroke-width="1.2" stroke-dasharray="4 3"/>
          <text x="40" y="44" text-anchor="middle" font-size="13" fill="#e3000b" font-weight="bold">券</text>
        </svg>
        <p class="empty-title">{{ activeTab === '1' ? '暂无可用优惠券' : activeTab === '2' ? '暂无已使用记录' : '暂无已过期券' }}</p>
        <p class="empty-desc">{{ activeTab === '1' ? '去逛逛，说不定有惊喜' : '使用优惠券后会记录在这里' }}</p>
        <button v-if="activeTab === '1'" class="go-shop-btn" @click="$router.push('/')">去购物</button>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="error-tip">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><circle cx="12" cy="16" r="0.5" fill="#f59e0b"/></svg>
        {{ errorMsg }}
      </div>

      <div v-if="!loading && list.length > 0 && finished" class="bottom-tip">到底了</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { NavBar as VanNavBar, Tabs as VanTabs, Tab as VanTab, Skeleton as VanSkeleton, Empty as VanEmpty } from 'vant'
import { memberCouponApi } from '@mall/api-sdk/app'
import type { MemberCoupon } from '@mall/api-sdk/app/member'

const router = useRouter()
const activeTab = ref('1')
const list = ref<MemberCoupon[]>([])
const loading = ref(false)
const finished = ref(false)
const errorMsg = ref('')

const statusClass = computed(() => {
  if (activeTab.value === '2') return 'used'
  if (activeTab.value === '3') return 'expired'
  return ''
})

function formatDate(s?: string) {
  if (!s) return ''
  return s.slice(0, 10)
}

function reload() {
  list.value = []
  finished.value = false
  errorMsg.value = ''
  loadMore()
}

async function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await memberCouponApi.list(Number(activeTab.value))
    const data = res.data?.data
    if (Array.isArray(data)) list.value.push(...data)
    finished.value = true
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '加载失败，请稍后重试'
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 初次加载
loadMore()
</script>

<style scoped>
.coupon-page { min-height: 100vh; background: #f5f5f5; }

.list-wrap { padding: 12px; display: flex; flex-direction: column; gap: 12px; }

.coupon-skeleton { background: #fff; border-radius: 12px; padding: 20px 16px; }

/* 优惠券卡片 */
.coupon-card {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(227,0,11,.08);
  border: 1px solid #fde8e8;
}

.coupon-card.used, .coupon-card.expired {
  filter: grayscale(0.35);
  opacity: 0.8;
}

.coupon-left {
  background: linear-gradient(135deg, #e3000b 0%, #ff4d4d 100%);
  min-width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
  gap: 4px;
}

.coupon-amount {
  color: #fff;
  font-size: 32px;
  font-weight: 900;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.unit { font-size: 15px; font-weight: 600; vertical-align: super; }

.coupon-threshold {
  color: rgba(255,255,255,.85);
  font-size: 11px;
  white-space: nowrap;
}

/* 锯齿分隔线 */
.coupon-divider {
  width: 16px;
  flex-shrink: 0;
  background: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}
.dot {
  width: 16px;
  height: 8px;
  background: #f5f5f5;
  border-radius: 0 0 8px 8px;
}
.dot.dot-top { border-radius: 0 0 8px 8px; }
.dot.dot-bottom { border-radius: 8px 8px 0 0; }
.dash-line {
  flex: 1;
  width: 1px;
  background: repeating-linear-gradient(to bottom, #e8e8e8 0px, #e8e8e8 4px, transparent 4px, transparent 8px);
}

.coupon-right {
  flex: 1;
  padding: 16px 16px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 700;
  color: #222;
  line-height: 1.4;
}

.coupon-code {
  font-size: 11px;
  color: #999;
  font-family: monospace;
  letter-spacing: 0.04em;
}

.coupon-date { font-size: 11px; color: #bbb; }

.use-btn {
  margin-top: 8px;
  align-self: flex-start;
  background: #e3000b;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 5px 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 150ms;
}
.use-btn:hover { opacity: 0.85; }

.used-tag {
  margin-top: 8px;
  align-self: flex-start;
  background: #f5f5f5;
  color: #aaa;
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 11px;
}

/* 空状态 */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0 40px;
  gap: 8px;
}
.empty-title { font-size: 15px; font-weight: 600; color: #444; margin: 0; }
.empty-desc { font-size: 12px; color: #aaa; margin: 0; }
.go-shop-btn {
  margin-top: 12px;
  background: #e3000b;
  color: #fff;
  border: none;
  border-radius: 22px;
  padding: 10px 32px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 150ms;
}
.go-shop-btn:hover { opacity: 0.85; }

.error-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  color: #f59e0b;
  font-size: 13px;
  padding: 16px 0;
}

.bottom-tip {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 12px 0 4px;
}
</style>

