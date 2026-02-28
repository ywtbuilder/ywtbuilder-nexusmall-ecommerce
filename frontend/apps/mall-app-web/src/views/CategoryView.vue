<template>
  <div class="category-page">
    <div class="category-search">
      <van-search v-model="searchKey" placeholder="搜索商品" shape="round" @search="goSearch" />
    </div>

    <div class="category-shell">
      <div class="category-body" :style="{ '--sidebar-width': `${sidebarWidth}px` }">
        <van-sidebar v-model="activeIndex" @change="onCategoryChange">
          <van-sidebar-item v-for="cat in categories" :key="cat.id" :title="cat.name" />
        </van-sidebar>

        <div class="sub-category">
          <!-- 子分类网格 -->
          <template v-if="subCategories.length">
            <van-grid :column-num="gridColumns" :gutter="gridGutter" :border="false">
              <van-grid-item
                v-for="sub in subCategories"
                :key="sub.id"
                :text="sub.name"
                @click="$router.push(`/search?categoryId=${sub.id}`)"
              >
                <template #icon>
                  <img
                    v-if="sub.icon"
                    :src="toThumbUrl(sub.icon)"
                    :style="{ width: `${iconSize}px`, height: `${iconSize}px`, objectFit: 'contain' }"
                    loading="lazy"
                    decoding="async"
                    @error="onImgError($event)"
                  />
                  <van-icon v-else name="photo-o" :size="iconSize" color="#ddd" />
                </template>
              </van-grid-item>
            </van-grid>
          </template>

          <!-- 叶子分类：直接展示商品 -->
          <template v-else-if="categoryProducts.length">
            <div class="cat-products-head">
              <span class="cat-products-title">全部商品</span>
              <span class="cat-products-more" @click="$router.push(`/search?categoryId=${currentCategoryId}`)">查看更多 ›</span>
            </div>
            <div class="cat-product-grid">
              <div
                v-for="p in categoryProducts"
                :key="p.id"
                class="cat-product-card"
                @click="$router.push(`/product/${p.id}`)"
              >
                <div class="cat-card-img-wrap">
                  <img :src="toThumbUrl(p.pic)" class="cat-card-img" loading="lazy" decoding="async" @error="onImgError($event)" />
                  <span v-if="(p.sale ?? 0) > 50" class="cat-card-badge">HOT</span>
                </div>
                <div class="cat-card-body">
                  <div class="cat-card-name">{{ p.name }}</div>
                  <div class="cat-card-footer">
                    <span class="cat-card-price"><em class="cat-price-unit">¥</em>{{ p.price }}</span>
                    <span v-if="(p.sale ?? 0) > 0" class="cat-card-sale">已售{{ p.sale }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <van-empty v-else-if="!loadingProducts" description="该分类暂无商品" />
          <div v-else class="cat-loading"><van-loading size="24px" color="var(--t-primary)" /></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search as VanSearch,
  Sidebar as VanSidebar,
  SidebarItem as VanSidebarItem,
  Grid as VanGrid,
  GridItem as VanGridItem,
  Empty as VanEmpty,
  Icon as VanIcon,
  Loading as VanLoading
} from 'vant'
import { homeApi, appProductApi } from '@mall/api-sdk/app'
import type { ProductCategory } from '@mall/api-sdk/app/home'
import { cancelWarmup, warmupImages } from '@/utils/imageWarmup'

interface CategoryProduct {
  id: number
  name: string
  pic: string
  price: number
  sale?: number
}

const router = useRouter()
const route = useRoute()
const searchKey = ref('')
const activeIndex = ref(0)
const categories = ref<ProductCategory[]>([])
const subCategoryMap = ref<Record<number, ProductCategory[]>>({})
const categoryProductsMap = ref<Record<number, CategoryProduct[]>>({})
const loadingProducts = ref(false)
const viewportWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 375)
const CATEGORY_WARMUP_SCOPE = 'category-view'

const currentCategoryId = computed(() => categories.value[activeIndex.value]?.id ?? 0)
const subCategories = computed(() => {
  const categoryId = currentCategoryId.value
  if (!categoryId) return []
  return subCategoryMap.value[categoryId] ?? []
})
const categoryProducts = computed(() => {
  const categoryId = currentCategoryId.value
  if (!categoryId) return []
  return categoryProductsMap.value[categoryId] ?? []
})

function resolveGridColumns(width: number) {
  if (width >= 1600) return 6
  if (width >= 1280) return 5
  if (width >= 1024) return 4
  if (width >= 768) return 3
  return 2
}

const gridColumns = computed(() => resolveGridColumns(viewportWidth.value))

const sidebarWidth = computed(() => {
  if (viewportWidth.value >= 1600) return 220
  if (viewportWidth.value >= 1280) return 210
  if (viewportWidth.value >= 1024) return 190
  if (viewportWidth.value >= 768) return 180
  return 100
})

const iconSize = computed(() => {
  if (viewportWidth.value >= 1600) return 60
  if (viewportWidth.value >= 1024) return 52
  if (viewportWidth.value >= 768) return 46
  return 40
})

const gridGutter = computed(() => (viewportWidth.value >= 1024 ? 12 : 10))

function updateViewportWidth() {
  viewportWidth.value = window.innerWidth
}

function goSearch() {
  router.push(`/search?keyword=${searchKey.value}`)
}

function normalizeImageUrl(url?: string | null): string {
  if (!url) return ''
  const clean = url.trim()
  if (clean.startsWith('/api/asset/image/')) return clean
  if (clean.startsWith('data:image/')) return clean
  return ''
}

function toThumbUrl(url?: string | null): string {
  const normalized = normalizeImageUrl(url)
  if (!normalized || !normalized.startsWith('/api/asset/image/')) return normalized
  const [path, query = ''] = normalized.split('?', 2)
  const params = new URLSearchParams(query)
  params.set('variant', 'thumb')
  const serialized = params.toString()
  return serialized ? `${path}?${serialized}` : path
}

function onImgError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" fill="%23f5f5f5"><rect width="200" height="200"/><text x="100" y="100" text-anchor="middle" dominant-baseline="central" font-size="13" fill="%23bbb" font-family="sans-serif">暂无图片</text></svg>'
    )
}

function onCategoryChange(_index: number) {
  cancelWarmup(CATEGORY_WARMUP_SCOPE)
  loadSubCategoriesByActive()
}

function warmupCategoryProducts(products: CategoryProduct[]) {
  if (!products.length) return
  const urls = products.map((item) => toThumbUrl(item.pic)).filter(Boolean)
  warmupImages(urls.slice(0, 8), {
    scopeId: CATEGORY_WARMUP_SCOPE,
    priority: 'immediate',
    maxItems: 8,
  })
  warmupImages(urls.slice(8, 20), {
    scopeId: CATEGORY_WARMUP_SCOPE,
    priority: 'idle',
    maxItems: 12,
  })
}

async function loadCategoryProducts(categoryId: number) {
  if (!categoryId || categoryProductsMap.value[categoryId] !== undefined) return
  loadingProducts.value = true
  try {
    const { data } = await appProductApi.search({ productCategoryId: categoryId, pageSize: 40, pageNum: 1 })
    const list = ((data.data as { list?: Array<Record<string, unknown>> })?.list ?? [])
      .map((item) => {
        const id = Number(item.id)
        if (!Number.isFinite(id) || id <= 0) return null
        const pic = normalizeImageUrl(String(item.pic ?? '').trim())
        if (!pic) return null
        return {
          id,
          name: String(item.name ?? '未命名商品'),
          pic,
          price: Number(item.price ?? 0),
          sale: Number(item.sale ?? 0),
        } as CategoryProduct
      })
      .filter((item): item is CategoryProduct => item != null)
    categoryProductsMap.value[categoryId] = list
    warmupCategoryProducts(list)
  } catch {
    categoryProductsMap.value[categoryId] = []
  } finally {
    loadingProducts.value = false
  }
}

function parseCategoryList(data: unknown): ProductCategory[] {
  if (Array.isArray(data)) return data
  if (data && typeof data === 'object' && Array.isArray((data as { list?: ProductCategory[] }).list)) {
    return (data as { list: ProductCategory[] }).list
  }
  return []
}

async function loadSubCategories(parentId: number) {
  if (!parentId || subCategoryMap.value[parentId]) return
  try {
    const { data } = await homeApi.productCateList(parentId)
    subCategoryMap.value[parentId] = parseCategoryList(data.data)
  } catch {
    subCategoryMap.value[parentId] = []
  }
}

function loadSubCategoriesByActive() {
  const parentId = currentCategoryId.value
  if (!parentId) return
  void loadSubCategories(parentId).then(() => {
    // 如果该分类是叶子节点（无子分类）则加载商品
    if ((subCategoryMap.value[parentId] ?? []).length === 0) {
      void loadCategoryProducts(parentId)
    }
  })
}

function applyInitialCategoryFromRoute() {
  const idParam = route.query.id
  if (!idParam || !categories.value.length) return
  const id = Number(idParam)
  const idx = categories.value.findIndex(c => c.id === id)
  if (idx !== -1) activeIndex.value = idx
}

onMounted(async () => {
  updateViewportWidth()
  window.addEventListener('resize', updateViewportWidth, { passive: true })

  try {
    const { data } = await homeApi.productCateList(0)
    categories.value = parseCategoryList(data.data)
    applyInitialCategoryFromRoute()
    loadSubCategoriesByActive()
  } catch {
    // ignore
  }
})

watch(() => route.query.id, () => {
  cancelWarmup(CATEGORY_WARMUP_SCOPE)
  applyInitialCategoryFromRoute()
  loadSubCategoriesByActive()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportWidth)
  cancelWarmup(CATEGORY_WARMUP_SCOPE)
})
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  background: var(--t-bg);
}

.category-search {
  position: sticky;
  top: 0;
  z-index: 20;
  background: var(--t-bg-top);
  border-bottom: 1px solid var(--t-border-light);
}

.category-search :deep(.van-search) {
  margin: 0 auto;
  max-width: calc(var(--app-content-max-width) + var(--app-content-padding) * 2);
  padding-left: var(--app-content-padding) !important;
  padding-right: var(--app-content-padding) !important;
}

.category-shell {
  width: min(calc(100% - (var(--app-content-padding) * 2)), var(--app-content-max-width));
  margin: 0 auto;
  padding-top: 10px;
  padding-bottom: 16px;
}

.category-body {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.category-body :deep(.van-sidebar) {
  width: var(--sidebar-width);
  flex: 0 0 var(--sidebar-width);
  border-radius: var(--t-radius-md);
  overflow: hidden;
}

.category-body :deep(.van-sidebar-item) {
  padding: 12px 10px;
  font-size: 13px;
  line-height: 1.35;
}

.sub-category {
  min-width: 0;
  flex: 1;
  padding: 10px;
  border-radius: var(--t-radius-md);
  background: var(--t-bg-card);
  box-shadow: var(--t-shadow-sm);
}

.sub-category :deep(.van-grid-item__content) {
  padding: 10px 4px;
  border-radius: var(--t-radius-sm);
  transition: background var(--t-dur) var(--t-ease);
}

.sub-category :deep(.van-grid-item__text) {
  margin-top: 8px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.35;
  text-align: center;
  font-size: 12px;
}

@media (hover: hover) and (pointer: fine) {
  .sub-category :deep(.van-grid-item__content:hover) {
    background: var(--t-bg-hover);
  }
}

@media (min-width: 1024px) {
  /* 桌面端搜索栏已在全局 MainHeader，隐藏页面自带搜索 */
  .category-search { display: none; }

  .category-body {
    gap: 12px;
  }

  .sub-category {
    padding: 12px;
  }

  .category-body :deep(.van-sidebar-item) {
    padding: 14px 12px;
    font-size: 14px;
  }

  .sub-category :deep(.van-grid-item__text) {
    font-size: 13px;
  }
}

/* ── 叶子分类商品网格 ── */
.cat-products-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}
.cat-products-title {
  font-size: 15px;
  font-weight: 700;
  color: #111;
}
.cat-products-more {
  font-size: 12px;
  color: #e3000b;
  cursor: pointer;
  transition: opacity 120ms;
}
.cat-products-more:hover { opacity: 0.75; }

.cat-product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
@media (min-width: 600px) {
  .cat-product-grid { grid-template-columns: repeat(3, 1fr); gap: 12px; }
}
@media (min-width: 1024px) {
  .cat-product-grid { grid-template-columns: repeat(4, 1fr); gap: 14px; }
}
@media (min-width: 1280px) {
  .cat-product-grid { grid-template-columns: repeat(5, 1fr); }
}

/* ── 卡片（与首页 .product-card 完全对齐） ── */
.cat-product-card {
  overflow: hidden;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,.07);
  transition: transform 180ms, box-shadow 180ms;
  border: 1px solid #f0f0f0;
}
.cat-product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,.12);
}
.cat-product-card:active { transform: scale(0.97); }

.cat-card-img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;
}
.cat-card-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 450ms;
}
.cat-product-card:hover .cat-card-img { transform: scale(1.05); }

.cat-card-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 7px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.06em;
  border-radius: 20px;
  line-height: 1.6;
  background: #fff1f0;
  color: #e3000b;
}

.cat-card-body {
  padding: 8px 10px 10px;
}
.cat-card-name {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-height: 1.45;
  color: #333;
  font-size: 12px;
  font-weight: 500;
}
.cat-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}
.cat-card-price {
  color: #e3000b;
  font-size: 15px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  font-style: normal;
}
.cat-price-unit {
  font-size: 12px;
  font-weight: 600;
  font-style: normal;
  margin-right: 1px;
}
.cat-card-sale {
  color: #999;
  font-size: 11px;
}

.cat-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 120px;
}
</style>

