<template>
  <div class="search-page">
    <div class="search-sticky">
      <van-search
        v-model="keyword"
        placeholder="搜索商品"
        show-action
        autofocus
        @search="doSearch"
        @cancel="$router.back()"
      />
    </div>

    <van-list v-model:loading="loading" :finished="finished" :immediate-check="false" finished-text="没有更多了" @load="loadMore">
      <div class="product-grid">
        <div v-for="p in list" :key="p.id" class="product-card" @click="$router.push(`/product/${p.id}`)">
          <div class="card-img-wrap">
            <img :src="toSearchThumbUrl(p.pic)" class="card-img" loading="lazy" decoding="async" @error="(e) => { (e.target as HTMLImageElement).style.opacity='0.3' }" />
            <span v-if="(p.sale ?? 0) > 50" class="card-badge badge-hot">HOT</span>
          </div>
          <div class="card-body">
            <div class="card-name">{{ p.name }}</div>
            <div class="card-footer">
              <span class="card-price"><em class="price-unit">¥</em>{{ p.price }}</span>
              <span v-if="(p.sale ?? 0) > 0" class="card-sale">已售{{ p.sale }}</span>
            </div>
          </div>
        </div>
      </div>
    </van-list>

    <van-empty v-if="!loading && searched && !list.length" description="暂无搜索结果" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search as VanSearch, List as VanList, Empty as VanEmpty } from 'vant'
import { searchApi } from '@mall/api-sdk/app'
import type { EsProduct } from '@mall/api-sdk/app/search'
import axios from 'axios'
import { cancelWarmup, warmupImages } from '@/utils/imageWarmup'

// 图片 URL 归一化：仅内部 API 路径可用，CDN 代理已移除
function normalizeImgUrl(url?: string | null): string {
  if (!url) return ''
  const clean = url.trim()
  if (clean.startsWith('/api/asset/image/')) return clean
  if (clean.startsWith('data:image/')) return clean
  return ''
}

function toSearchThumbUrl(url?: string | null): string {
  const normalized = normalizeImgUrl(url)
  if (!normalized || !normalized.startsWith('/api/asset/image/')) return normalized
  const [path, query = ''] = normalized.split('?', 2)
  const params = new URLSearchParams(query)
  params.set('variant', 'search')
  const serialized = params.toString()
  return serialized ? `${path}?${serialized}` : path
}

const PAGE_BATCH_SIZE = 8
const MAX_RESULT_COUNT = 20
const SEARCH_WARMUP_SCOPE = 'search-view'

const route = useRoute()
const keyword = ref((route.query.keyword as string) ?? '')
const list = ref<EsProduct[]>([])
const loading = ref(false)
const finished = ref(false)
const searched = ref(false)
const pageNum = ref(1)
const userTriggeredLoad = ref(false)
const autoFillAllowance = ref(1)
let activeController: AbortController | null = null

function cancelInFlightRequest() {
  if (activeController) {
    activeController.abort()
    activeController = null
  }
}

function warmupSearchResults(items: EsProduct[]) {
  if (!items.length) return
  const urls = items.map((item) => toSearchThumbUrl(item.pic))
  warmupImages(urls.slice(0, 6), {
    scopeId: SEARCH_WARMUP_SCOPE,
    priority: 'immediate',
    maxItems: 6,
  })
  warmupImages(urls.slice(6, 20), {
    scopeId: SEARCH_WARMUP_SCOPE,
    priority: 'idle',
    maxItems: 14,
  })
}

function markUserTriggeredLoad() {
  userTriggeredLoad.value = true
  if (!loading.value && !finished.value && pageNum.value > 1) {
    void loadMore()
  }
}

async function doSearch() {
  cancelInFlightRequest()
  cancelWarmup(SEARCH_WARMUP_SCOPE)
  list.value = []
  pageNum.value = 1
  finished.value = false
  userTriggeredLoad.value = false
  autoFillAllowance.value = 1
  searched.value = true
  await loadMore()
}

async function loadMore() {
  if (activeController) {
    return
  }
  if (finished.value) {
    return
  }
  if (!keyword.value && !route.query.categoryId) {
    finished.value = true
    return
  }
  if (list.value.length >= MAX_RESULT_COUNT) {
    finished.value = true
    return
  }
  const doc = document.documentElement
  const noScrollableSpace = doc.scrollHeight <= window.innerHeight + 16
  const useAutoFill =
    pageNum.value > 1 &&
    !userTriggeredLoad.value &&
    autoFillAllowance.value > 0 &&
    noScrollableSpace
  if (pageNum.value > 1 && !userTriggeredLoad.value && !useAutoFill) {
    loading.value = false
    return
  }

  const controller = new AbortController()
  activeController = controller
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize: PAGE_BATCH_SIZE
    }
    if (route.query.categoryId) {
      params.productCategoryId = route.query.categoryId
    }

    const { data } = await searchApi.search(params, { signal: controller.signal })
    const items = data.data.list ?? []
    warmupSearchResults(items)
    const remain = Math.max(0, MAX_RESULT_COUNT - list.value.length)
    list.value.push(...items.slice(0, remain))
    pageNum.value++
    if (useAutoFill) {
      autoFillAllowance.value = Math.max(0, autoFillAllowance.value - 1)
    }
    if (pageNum.value > 2) {
      // 每次加载完一页后重置许可，要求用户继续下滚再触发下一页
      userTriggeredLoad.value = false
    }
    if (items.length < PAGE_BATCH_SIZE || list.value.length >= MAX_RESULT_COUNT) {
      finished.value = true
    }
  } catch (error) {
    if (!axios.isCancel(error) && !(error instanceof DOMException && error.name === 'AbortError')) {
      finished.value = true
    }
  } finally {
    if (activeController === controller) {
      activeController = null
    }
    loading.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', markUserTriggeredLoad, { passive: true })
  window.addEventListener('wheel', markUserTriggeredLoad, { passive: true })
  window.addEventListener('touchmove', markUserTriggeredLoad, { passive: true })
  if (keyword.value || route.query.categoryId) {
    searched.value = true
    loadMore()
  }
})

// 当 URL query 变化时（如从分类A切换到分类B），重置并重新加载
watch(
  () => route.query,
  (newQuery, oldQuery) => {
    if (newQuery.categoryId !== oldQuery?.categoryId || newQuery.keyword !== oldQuery?.keyword) {
      keyword.value = (newQuery.keyword as string) ?? ''
      cancelInFlightRequest()
      cancelWarmup(SEARCH_WARMUP_SCOPE)
      list.value = []
      pageNum.value = 1
      finished.value = false
      userTriggeredLoad.value = false
      autoFillAllowance.value = 1
      if (newQuery.keyword || newQuery.categoryId) {
        searched.value = true
        void loadMore()
      }
    }
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('scroll', markUserTriggeredLoad)
  window.removeEventListener('wheel', markUserTriggeredLoad)
  window.removeEventListener('touchmove', markUserTriggeredLoad)
  cancelInFlightRequest()
  cancelWarmup(SEARCH_WARMUP_SCOPE)
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: var(--t-bg);
}

.search-sticky {
  position: sticky;
  top: 0;
  z-index: 20;
  background: var(--t-bg-top);
  border-bottom: 1px solid var(--t-border-light);
}

.search-sticky :deep(.van-search) {
  margin: 0 auto;
  max-width: calc(var(--app-content-max-width) + var(--app-content-padding) * 2);
  padding-left: var(--app-content-padding) !important;
  padding-right: var(--app-content-padding) !important;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  width: min(calc(100% - 24px), 1280px);
  margin: 0 auto;
  padding-top: 12px;
  padding-bottom: 16px;
}
@media (min-width: 600px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); gap: 12px; }
}
@media (min-width: 900px) {
  .product-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (min-width: 1200px) {
  .product-grid { grid-template-columns: repeat(5, 1fr); }
}
@media (min-width: 1500px) {
  .product-grid { grid-template-columns: repeat(6, 1fr); gap: 14px; }
}

/* ── 卡片（与首页 .product-card 完全对齐） ── */
.product-card {
  overflow: hidden;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,.07);
  transition: transform 180ms, box-shadow 180ms;
  border: 1px solid #f0f0f0;
  contain: layout style;
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,.12);
}
.product-card:active { transform: scale(0.97); }

.card-img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;
  contain: layout paint;
}
.card-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 280ms ease;
  will-change: transform;
}
.product-card:hover .card-img { transform: scale(1.05); }

.card-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 7px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.06em;
  border-radius: 20px;
  line-height: 1.6;
}
.badge-hot { background: #fff1f0; color: #e3000b; }

.card-body { padding: 8px 10px 10px; }
.card-name {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.45;
  color: #333;
  font-size: 12px;
  font-weight: 500;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}
.card-price {
  color: #e3000b;
  font-size: 15px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  font-style: normal;
}
.price-unit {
  font-size: 12px;
  font-weight: 600;
  font-style: normal;
  margin-right: 1px;
}
.card-sale { color: #999; font-size: 11px; }
</style>

