<template>
  <div class="upcoming-page">
    <!-- ── 页头 ── -->
    <div class="up-header">
      <button class="up-back" @click="$router.back()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div class="up-title-wrap">
        <svg class="up-clock" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        <h1 class="up-title">即将上架</h1>
        <span class="up-countdown">开启 {{ countdown }}</span>
      </div>
      <span class="up-total">共 {{ products.length }} 件商品</span>
    </div>

    <!-- ── 商品网格 ── -->
    <div class="up-container">
      <div v-if="loading" class="up-loading">加载中...</div>
      <div v-else-if="!products.length" class="up-empty">暂无可展示商品</div>
      <div v-else class="up-grid">
        <div
          v-for="p in products"
          :key="p.id"
          class="up-card"
          @click="handleCardClick(p)"
        >
          <div class="up-img-wrap">
            <img
              :src="toThumbUrl(p.pic)"
              :alt="p.name"
              class="up-img"
              loading="lazy"
              decoding="async"
              @error="onImgError"
            />
            <span class="up-badge">预购中</span>
          </div>
          <div class="up-body">
            <div class="up-name">{{ p.name }}</div>
            <div class="up-footer">
              <div class="up-price-row">
                <span class="up-price"><em class="price-unit">¥</em>{{ p.price }}</span>
                <span v-if="p.origPrice !== p.price" class="up-orig">¥{{ p.origPrice }}</span>
              </div>
              <span class="up-sold">年售{{ formatSold(p.sold) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 底部提示 ── -->
      <div class="up-bottom">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        更多商品即将上架，敬请期待
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { appProductApi } from '@mall/api-sdk/app'
import { cancelWarmup, warmupImages } from '@/utils/imageWarmup'

interface UpcomingProduct {
  id: number
  name: string
  pic: string
  price: string
  origPrice: string
  sold: number
}

const router = useRouter()
const products = ref<UpcomingProduct[]>([])
const navigating = ref(false)
const loading = ref(false)
const UPCOMING_WARMUP_SCOPE = 'upcoming-view'

// 图片 URL 归一化：仅内部 API 路径可用，禁止本地文件与外链
function normalizeImgUrl(url?: string | null): string {
  if (!url) return ''
  const clean = url.trim()
  if (clean.startsWith('/api/asset/image/')) return clean
  if (clean.startsWith('data:image/')) return clean
  return ''
}

function toThumbUrl(url?: string | null): string {
  const normalized = normalizeImgUrl(url)
  if (!normalized || !normalized.startsWith('/api/asset/image/')) return normalized
  const [path, query = ''] = normalized.split('?', 2)
  const params = new URLSearchParams(query)
  params.set('variant', 'thumb')
  const serialized = params.toString()
  return serialized ? `${path}?${serialized}` : path
}

// ── 倒计时（固定 2h20m，演示用） ──
const TARGET_SECONDS = 2 * 3600 + 20 * 60
const remaining = ref(TARGET_SECONDS)
let timer: ReturnType<typeof setInterval> | null = null

const countdown = ref(formatRemaining(remaining.value))

function formatRemaining(s: number): string {
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  return `${h}h ${String(m).padStart(2, '0')}m ${String(sec).padStart(2, '0')}s`
}

function formatSold(sold: number): string {
  if (sold >= 1000000) return `${Math.floor(sold / 1000000)}00万+`
  if (sold >= 100000) return `${Math.floor(sold / 10000)}万+`
  if (sold >= 10000) return `${Math.floor(sold / 1000) / 10}w+`
  return `${sold}`
}

function toNumber(value: unknown, fallback = 0): number {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

function toUpcoming(item: Record<string, unknown>): UpcomingProduct | null {
  const id = toNumber(item.id, NaN)
  if (!Number.isFinite(id)) return null

  const pic = normalizeImgUrl(String(item.pic ?? '').trim())
  if (!pic) return null

  const price = toNumber(item.price, 0)
  const origPriceRaw = toNumber(item.originalPrice ?? item.price, price)
  const origPrice = origPriceRaw > 0 ? origPriceRaw : price
  const sold = toNumber(item.sale, 0)

  return {
    id,
    name: String(item.name ?? '未命名商品'),
    pic,
    price: `${price}`,
    origPrice: `${origPrice}`,
    sold,
  }
}

async function loadUpcomingProducts() {
  loading.value = true
  try {
    const { data } = await appProductApi.search({ pageNum: 1, pageSize: 30, sort: 0 }) as unknown as { data?: unknown }
    const payload = (data as { data?: unknown })?.data
    const list = (
      (payload as { list?: Array<Record<string, unknown>> } | undefined)?.list
      ?? (Array.isArray(payload) ? payload as Array<Record<string, unknown>> : [])
    )
    products.value = list
      .map((item) => toUpcoming(item))
      .filter((item): item is UpcomingProduct => item != null)
      .slice(0, 24)
    const urls = products.value.map((item) => toThumbUrl(item.pic))
    warmupImages(urls.slice(0, 8), {
      scopeId: UPCOMING_WARMUP_SCOPE,
      priority: 'immediate',
      maxItems: 8,
    })
    warmupImages(urls.slice(8, 20), {
      scopeId: UPCOMING_WARMUP_SCOPE,
      priority: 'idle',
      maxItems: 12,
    })
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  timer = setInterval(() => {
    if (remaining.value > 0) {
      remaining.value--
      countdown.value = formatRemaining(remaining.value)
    } else if (timer) {
      clearInterval(timer)
      countdown.value = '0h 00m 00s'
    }
  }, 1000)
  void loadUpcomingProducts()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  cancelWarmup(UPCOMING_WARMUP_SCOPE)
})

function onImgError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" fill="%23f5f5f5"><rect width="200" height="200"/><text x="100" y="100" text-anchor="middle" dominant-baseline="central" font-size="13" fill="%23bbb" font-family="sans-serif">暂无图片</text></svg>'
    )
}

async function handleCardClick(p: UpcomingProduct) {
  if (navigating.value) return
  navigating.value = true
  try {
    if (typeof p.id === 'number') {
      router.push(`/product/${p.id}`)
      return
    }
  } finally {
    navigating.value = false
  }
  router.push('/search?keyword=' + encodeURIComponent(p.name))
}
</script>

<style scoped>
.upcoming-page {
  min-height: 100vh;
  background: #f4f4f4;
}

/* ── 页头 ── */
.up-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 0 16px;
  height: 52px;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}

.up-back {
  background: none;
  border: none;
  padding: 4px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  border-radius: 4px;
  transition: background 150ms;
  flex-shrink: 0;
}
.up-back:hover { background: #f5f5f5; }

.up-title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}
.up-clock { flex-shrink: 0; }
.up-title {
  font-size: 16px;
  font-weight: 700;
  color: #111;
  margin: 0;
}
.up-countdown {
  font-size: 12px;
  color: #e3000b;
  background: #fff0f0;
  border: 1px solid #ffa8a8;
  border-radius: 20px;
  padding: 2px 9px;
  font-weight: 600;
  white-space: nowrap;
}
.up-total {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

/* ── 容器 ── */
.up-container {
  width: min(calc(100% - 0px), 1400px);
  margin: 0 auto;
  padding: 14px 12px 40px;
}

.up-loading,
.up-empty {
  padding: 28px 0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* ── 网格 ── */
.up-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

/* ── 单卡 ── */
.up-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  transition: transform 180ms, box-shadow 180ms;
}
.up-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,.10);
}

.up-img-wrap {
  position: relative;
  aspect-ratio: 1;
  background: #f8f8f8;
  overflow: hidden;
}
.up-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 250ms;
}
.up-card:hover .up-img { transform: scale(1.04); }

.up-badge {
  position: absolute;
  top: 7px;
  right: 7px;
  background: linear-gradient(135deg, #e3000b, #ff6b35);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 12px;
}

.up-body {
  padding: 9px 10px 11px;
}
.up-name {
  font-size: 13px;
  color: #222;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}
.up-footer {
  margin-top: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
.up-price-row {
  display: flex;
  align-items: baseline;
  gap: 5px;
}
.price-unit {
  font-style: normal;
  font-size: 11px;
  vertical-align: 1px;
}
.up-price {
  color: #e3000b;
  font-size: 16px;
  font-weight: 800;
}
.up-orig {
  color: #bbb;
  font-size: 11px;
  text-decoration: line-through;
}
.up-sold {
  margin-left: auto;
  font-size: 11px;
  color: #aaa;
  white-space: nowrap;
}

/* ── 底部提示 ── */
.up-bottom {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #bbb;
  font-size: 13px;
  padding: 28px 0 8px;
}

/* ━━ 响应式 ━━ */
@media (min-width: 480px) {
  .up-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (min-width: 768px) {
  .up-grid { grid-template-columns: repeat(4, 1fr); gap: 12px; }
  .up-container { padding: 16px 16px 40px; }
}
@media (min-width: 1024px) {
  .up-header { top: 56px; }
  .up-grid { grid-template-columns: repeat(5, 1fr); gap: 14px; }
  .up-container { padding: 20px 20px 50px; }
}
@media (min-width: 1440px) {
  .up-grid { grid-template-columns: repeat(6, 1fr); }
}
</style>
