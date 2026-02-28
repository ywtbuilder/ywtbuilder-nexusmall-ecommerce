<template>
  <div class="jd-detail-page">

    <!-- ===== 收货地址选择弹窗 ===== -->
    <Transition name="addr-fade">
      <div class="addr-mask" v-if="showAddrPanel" @click.self="showAddrPanel = false">
        <div class="addr-panel">
          <div class="addr-panel-hd">
            <span class="addr-panel-title">选择收货地址</span>
            <button class="addr-panel-close" @click="showAddrPanel = false">✕</button>
          </div>
          <div class="addr-tabs">
            <button :class="['addr-tab', { active: addrTab === 'saved' }]" @click="addrTab = 'saved'">常用地址</button>
            <button :class="['addr-tab', { active: addrTab === 'new' }]"   @click="addrTab = 'new'">选择新地址</button>
          </div>

          <!-- 常用地址 -->
          <div v-show="addrTab === 'saved'" class="addr-saved-wrap">
            <div v-if="!addressList.length" class="addr-empty">
              <p>{{ isLoggedIn ? '暂无收货地址' : '请先登录查看收货地址' }}</p>
              <button class="addr-link-btn" @click="addrTab = 'new'">选择配送区域 ›</button>
            </div>
            <div
              v-for="addr in addressList" :key="addr.id"
              :class="['addr-item', { 'addr-item--sel': selectedAddrId === addr.id }]"
              @click="selectSavedAddr(addr)"
            >
              <div class="addr-item-top">
                <span class="addr-item-name">{{ addr.name }}</span>
                <span class="addr-item-tel">{{ addr.phoneNumber }}</span>
                <span v-if="addr.defaultStatus === 1" class="addr-default-tag">默认</span>
              </div>
              <div class="addr-item-full">{{ addr.province }}{{ addr.city }}{{ addr.region }}{{ addr.detailAddress }}</div>
            </div>
          </div>

          <!-- 选择新地址：三列级联 -->
          <div v-show="addrTab === 'new'" class="area-picker">
            <div class="area-col" v-for="(col, ci) in areaCols" :key="ci">
              <div class="area-col-hd">{{ ['省/直辖市', '市', '区/县'][ci] }}</div>
              <div class="area-col-body">
                <div
                  v-for="item in col" :key="item"
                  :class="['area-item', { active: areaSelected[ci] === item }]"
                  @click="selectArea(ci, item)"
                >{{ item }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 顶部导航 -->
    <div class="jd-nav">
      <button class="nav-back" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
      </button>
      <span class="nav-title">商品详情</span>
      <div class="nav-actions">
        <button class="nav-btn" @click="$router.push('/cart')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 01-8 0"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="jd-content" v-if="detail">

      <!-- 顶部主面板：桌面双列 / 移动单列 -->
      <div class="jd-top-panel">

        <!-- 左：画廊 -->
        <div class="jd-gallery">
          <div class="gallery-main">
            <img
              :src="albumPics[activeImg]"
              class="main-img"
              :alt="detail.name"
              @error="onImgError($event)"
            />
            <div class="img-counter" v-if="albumPics.length > 1">{{ activeImg + 1 }} / {{ albumPics.length }}</div>
            <button class="arrow arrow-left" @click="prevImg" v-if="albumPics.length > 1">&#8249;</button>
            <button class="arrow arrow-right" @click="nextImg" v-if="albumPics.length > 1">&#8250;</button>
          </div>
          <div class="gallery-thumbs" v-if="albumPics.length > 1">
            <img
              v-for="(pic, i) in albumPics"
              :key="i"
              :src="pic"
              :class="['thumb', { active: activeImg === i }]"
              loading="lazy"
              decoding="async"
              @click="activeImg = i"
              @error="onImgError($event)"
            />
          </div>
        </div>

        <!-- 右：商品信息 -->
        <div class="jd-info">
          <h1 class="product-name">{{ detail.name }}</h1>
          <p v-if="detail.subTitle" class="product-subtitle">{{ detail.subTitle }}</p>

          <!-- 价格 -->
          <div class="price-zone">
            <div class="price-row">
              <span class="price-label">促销价</span>
              <span class="price-current"><em>¥</em>{{ formatPrice(selectedSku ? selectedSku.price : detail.price) }}</span>
              <span v-if="detail.originalPrice && detail.originalPrice !== detail.price" class="price-origin">¥{{ formatPrice(detail.originalPrice) }}</span>
              <span v-if="discountRate" class="price-badge">{{ discountRate }}折</span>
            </div>
            <div class="price-row price-row-sub">
              <span class="price-label">已售</span>
              <span class="sold-num">{{ (detail.sale ?? 0).toLocaleString() }}件</span>
              <span class="stock-info" v-if="selectedSku">库存{{ selectedSku.stock }}件</span>
            </div>
          </div>

          <!-- 促销 -->
          <div class="promo-zone">
            <div class="promo-item">
              <span class="promo-tag">活动</span>
              <span class="promo-text">满1000减100 · 满2000减200</span>
            </div>
            <div class="promo-item">
              <span class="promo-tag">券</span>
              <span class="promo-text">领取50元优惠券，下单立用</span>
            </div>
          </div>

          <!-- SKU 规格选择 -->
          <div class="sku-zone" v-if="skuList.length">
            <div v-for="(vals, key) in skuGroups" :key="key" class="sku-group">
              <div class="sku-group-label">
                {{ key }}：<span class="sku-sel-val">{{ selectedAttrs[key] || '请选择' }}</span>
              </div>
              <div class="sku-opts">
                <button
                  v-for="val in vals" :key="val"
                  :class="['sku-opt', { active: selectedAttrs[key] === val, disabled: isDisabled(key, val) }]"
                  @click="selectAttr(key, val)"
                >{{ val }}</button>
              </div>
            </div>
          </div>

          <!-- 数量 + 配送 -->
          <div class="quantity-zone">
            <div class="qty-row">
              <span class="qty-label">数量</span>
              <div class="qty-ctrl">
                <button class="qty-btn" @click="qty > 1 && qty--">&#8722;</button>
                <span class="qty-num">{{ qty }}</span>
                <button class="qty-btn" @click="qty++">+</button>
              </div>
            </div>
            <!-- 送至（收货地址） -->
            <div class="qty-row deliver-row" @click="showAddrPanel = true">
              <span class="qty-label">送至</span>
              <span class="deliver-text">
                {{ currentAddrText }}
                <svg class="deliver-chevron" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 2l4 4-4 4"/></svg>
              </span>
              <span class="free-ship">包邮</span>
            </div>
            <!-- 配送时间 -->
            <div class="qty-row">
              <span class="qty-label">时间</span>
              <span class="deliver-time">{{ deliveryTimeText }}</span>
            </div>
          </div>

          <!-- 服务 -->
          <div class="service-zone">
            <span class="svc-item" v-for="s in services" :key="s">✓ {{ s }}</span>
          </div>

          <!-- 桌面操作按钮 -->
          <div class="action-zone desktop-only">
            <button class="btn-collect" :class="{ active: collected }" @click="toggleCollection">
              <svg viewBox="0 0 24 24" :fill="collected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
              {{ collected ? '已收藏' : '收藏' }}
            </button>
            <button class="btn-cart-d" @click="handleAddToCart($event)" :disabled="adding">加入购物车</button>
            <button class="btn-buy-d"  @click="buyNow">立即购买</button>
          </div>
        </div>
      </div>

      <!-- 标签页导航 -->
      <div class="jd-tabs-wrap">
        <div class="jd-tabs">
          <button
            v-for="tab in tabs" :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >{{ tab.label }}</button>
        </div>
      </div>

      <!-- 商品详情 -->
      <div v-show="activeTab === 'detail'" class="tab-panel" ref="detailPanel">
        <div class="detail-image-wrap" v-if="introImageList.length">
          <div class="section-title">简介长图</div>
          <div class="detail-image-list intro-list">
            <img
              v-for="(img, idx) in introImageList"
              :key="`intro-${idx}`"
              :src="img"
              loading="lazy"
              decoding="async"
              @error="onImgError($event)"
            />
          </div>
        </div>

        <div class="detail-image-wrap" v-if="detailImageList.length">
          <div class="section-title">商品长图</div>
          <div class="detail-image-list">
            <img
              v-for="(img, idx) in detailImageList"
              :key="`detail-${idx}`"
              :src="img"
              loading="lazy"
              decoding="async"
              @error="onImgError($event)"
            />
          </div>
        </div>

        <div class="spec-table intro-spec-table" v-if="introTableRows.length">
          <div class="section-title">商品简介表</div>
          <div class="spec-row" v-for="row in introTableRows" :key="`${row.k}-${row.v}`">
            <span class="sk">{{ row.k }}</span>
            <span class="sv">{{ row.v }}</span>
          </div>
        </div>

        <div class="spec-table" v-if="parameterRows.length">
          <div class="section-title">参数规格区</div>
          <div class="spec-row" v-for="row in parameterRows" :key="`${row.k}-${row.v}`">
            <span class="sk">{{ row.k }}</span>
            <span class="sv">{{ row.v }}</span>
          </div>
        </div>

        <div class="spec-table" v-if="afterSaleRows.length">
          <div class="section-title">售后保障</div>
          <div class="spec-row" v-for="row in afterSaleRows" :key="`${row.k}-${row.v}`">
            <span class="sk">{{ row.k }}</span>
            <span class="sv">{{ row.v }}</span>
          </div>
        </div>

        <div class="detail-html-wrap" v-if="!detailImageList.length && detailContent">
          <div class="section-title">图文详情</div>
          <div class="detail-html" v-html="detailContent" />
        </div>
      </div>

      <!-- 规格参数 -->
      <div v-show="activeTab === 'spec'" class="tab-panel">
        <div class="spec-table">
          <div class="spec-row" v-for="row in allSpecRows" :key="`${row.k}-${row.v}`">
            <span class="sk">{{ row.k }}</span>
            <span class="sv">{{ row.v }}</span>
          </div>
        </div>
      </div>

      <!-- 售后保障 -->
      <div v-show="activeTab === 'service'" class="tab-panel">
        <div class="service-list">
          <div class="svc-card" v-for="svc in serviceCards" :key="svc.title">
            <div class="svc-icon">{{ svc.icon }}</div>
            <div class="svc-body">
              <div class="svc-title">{{ svc.title }}</div>
              <div class="svc-desc">{{ svc.desc }}</div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- 骨架屏 -->
    <div v-else class="skeleton-wrap">
      <div class="skel-img"/><div class="skel-line w80"/><div class="skel-line w60"/><div class="skel-line w40"/>
    </div>

    <!-- 移动端底部操作栏 -->
    <div class="jd-bottom-bar mobile-only" v-if="detail">
      <button class="bar-icon-btn" @click="toggleCollection">
        <svg viewBox="0 0 24 24" :fill="collected?'#e4393c':'none'" stroke="#e4393c" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
        <span>{{ collected?'已收藏':'收藏' }}</span>
      </button>
      <button id="mobile-cart-icon" class="bar-icon-btn" @click="$router.push('/cart')">
        <svg viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 001.99 1.61h9.72a2 2 0 001.99-1.61L23 6H6"/></svg>
        <span>购物车</span>
      </button>
      <button class="bar-btn bar-cart" @click="handleAddToCart($event)" :disabled="adding">加入购物车</button>
      <button class="bar-btn bar-buy" @click="buyNow">立即购买</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import {
  appProductApi, cartApi, memberCollectionApi,
  memberAttentionApi, memberReadHistoryApi, memberAddressApi,
} from '@mall/api-sdk/app'
import type { ProductSpecInfo, SkuStockInfo } from '@mall/api-sdk/app/product'
import type { MemberAddress } from '@mall/api-sdk/app/member'
import { cancelWarmup, warmupImages } from '@/utils/imageWarmup'

interface ProductData {
  id: number; brandId?: number; productCategoryId?: number
  name?: string; pic?: string; albumPics?: string; subTitle?: string
  description?: string; price?: number; originalPrice?: number
  sale?: number; stock?: number; unit?: string; weight?: number
  detailHtml?: string; detailMobileHtml?: string; brandName?: string
  productCategoryName?: string; productSn?: string
  skuStockList?: SkuStockInfo[]
  specList?: ProductSpecInfo[]
  introImageUrls?: string[]
  detailImageUrls?: string[]
  productAttributeValueList?: unknown[]
}

const route  = useRoute()
const router = useRouter()
const detail = ref<ProductData | null>(null)
const adding = ref(false)
const collected = ref(false)
const qty = ref(1)
const activeImg = ref(0)
const activeTab = ref<'detail'|'spec'|'service'>('detail')
const selectedAttrs = reactive<Record<string, string>>({})
const detailPanel  = ref<HTMLElement | null>(null)
const DETAIL_WARMUP_SCOPE = 'product-detail-view'

const tabs = [
  { key: 'detail',  label: '商品详情' },
  { key: 'spec',    label: '规格参数' },
  { key: 'service', label: '售后保障' },
] as const

const services = ['7天无理由退换', '正品保障', '在线客服', '极速发货']
const serviceCards = [
  { icon: '🔄', title: '7天无理由退换', desc: '自收到商品之日起7日内支持退换货，部分商品15天' },
  { icon: '✅', title: '正品保障',      desc: '所有商品均为品牌官方正品，支持正品溯源查询' },
  { icon: '🛡️', title: '1年整机质保',  desc: '整机享受1年质保服务，售后电话：950800' },
  { icon: '🚀', title: '极速配送',      desc: '北京、上海等主要城市次日达，偏远地区3-5天' },
]

// ===== 地址选择器 =====
const showAddrPanel = ref(false)
const addrTab = ref<'saved' | 'new'>('saved')
const addressList = ref<MemberAddress[]>([])
const selectedAddrId = ref<number | null>(null)
const selectedAddrText = ref('')
const isLoggedIn = ref(false)

// 中国常用省市区数据（精简版）
const AREA_DATA: Record<string, Record<string, string[]>> = {
  '北京市': { '北京市': ['东城区','西城区','朝阳区','海淀区','丰台区','石景山区','通州区','顺义区','昌平区','大兴区'] },
  '上海市': { '上海市': ['黄浦区','徐汇区','长宁区','静安区','普陀区','虹口区','杨浦区','浦东新区','闵行区','宝山区'] },
  '广东省': {
    '广州市': ['越秀区','海珠区','荔湾区','天河区','白云区','黄埔区','番禺区','花都区','南沙区','增城区'],
    '深圳市': ['福田区','罗湖区','南山区','盐田区','宝安区','龙华区','光明区','坪山区','龙岗区','大鹏新区'],
    '佛山市': ['禅城区','南海区','顺德区','三水区','高明区'],
  },
  '浙江省': {
    '杭州市': ['上城区','拱墅区','西湖区','滨江区','萧山区','余杭区','临安区','富阳区'],
    '宁波市': ['海曙区','江北区','北仑区','镇海区','鄞州区','奉化区'],
    '温州市': ['鹿城区','龙湾区','瓯海区','洞头区'],
  },
  '江苏省': {
    '南京市': ['玄武区','秦淮区','建邺区','鼓楼区','浦口区','栖霞区','雨花台区','江宁区','六合区','溧水区'],
    '苏州市': ['姑苏区','虎丘区','吴中区','相城区','吴江区'],
    '无锡市': ['梁溪区','锡山区','惠山区','滨湖区','新吴区'],
  },
  '安徽省': {
    '合肥市': ['瑶海区','庐阳区','蜀山区','包河区'],
    '芜湖市': ['镜湖区','弋江区','鸠江区','湾沚区'],
    '马鞍山市': ['花山区','雨山区','博望区'],
  },
  '四川省': {
    '成都市': ['锦江区','青羊区','金牛区','武侯区','成华区','龙泉驿区','青白江区','新都区','温江区','双流区'],
    '绵阳市': ['涪城区','游仙区','安州区'],
  },
  '湖北省': {
    '武汉市': ['江岸区','江汉区','硚口区','汉阳区','武昌区','青山区','洪山区','东西湖区','蔡甸区','江夏区'],
    '宜昌市': ['西陵区','伍家岗区','点军区','猇亭区'],
  },
  '湖南省': {
    '长沙市': ['芙蓉区','天心区','岳麓区','开福区','雨花区','望城区'],
    '株洲市': ['荷塘区','芦淞区','石峰区','天元区'],
  },
  '河南省': {
    '郑州市': ['中原区','二七区','管城区','金水区','上街区','惠济区','中牟县'],
    '洛阳市': ['老城区','西工区','瀍河区','涧西区','偃师区'],
  },
  '山东省': {
    '济南市': ['历下区','市中区','槐荫区','天桥区','历城区','长清区','章丘区'],
    '青岛市': ['市南区','市北区','黄岛区','崂山区','李沧区','城阳区','即墨区'],
  },
  '陕西省': {
    '西安市': ['新城区','碑林区','莲湖区','灞桥区','未央区','雁塔区','阎良区','临潼区','长安区','高陵区'],
    '咸阳市': ['秦都区','渭城区','兴平市'],
  },
  '福建省': {
    '福州市': ['鼓楼区','台江区','仓山区','马尾区','晋安区','长乐区'],
    '厦门市': ['思明区','海沧区','湖里区','集美区','同安区','翔安区'],
  },
  '辽宁省': {
    '沈阳市': ['和平区','沈河区','大东区','皇姑区','铁西区','苏家屯区','浑南区','沈北新区','于洪区'],
    '大连市': ['中山区','西岗区','沙河口区','甘井子区','旅顺口区','金州区'],
  },
}
const provinces = Object.keys(AREA_DATA)
function citiesOf(prov: string) { return Object.keys(AREA_DATA[prov] || {}) }
function regionsOf(prov: string, city: string) { return (AREA_DATA[prov] || {})[city] || [] }

const areaSelected = ref(['安徽省', '合肥市', '包河区'])
const areaCols = computed(() => [
  provinces,
  citiesOf(areaSelected.value[0]),
  regionsOf(areaSelected.value[0], areaSelected.value[1]),
])

function selectArea(colIdx: number, item: string) {
  const next = [...areaSelected.value]
  next[colIdx] = item
  if (colIdx === 0) { next[1] = citiesOf(item)[0] || ''; next[2] = '' }
  if (colIdx <= 1) { next[2] = regionsOf(next[0], next[1])[0] || '' }
  areaSelected.value = next
  if (colIdx === 2) {
    // confirmed district selection
    selectedAddrText.value = `${next[0].replace(/省|市/, '')}${next[1].replace(/市/, '')}${next[2]}`
    showAddrPanel.value = false
  }
}

function selectSavedAddr(addr: MemberAddress) {
  selectedAddrId.value = addr.id ?? null
  selectedAddrText.value = `${addr.province || ''}${addr.city || ''}${addr.region || ''}`
  showAddrPanel.value = false
}

const currentAddrText = computed(() =>
  selectedAddrText.value || `${areaSelected.value[0].replace(/省|市/, '')}${areaSelected.value[1].replace(/市/, '')}${areaSelected.value[2]}`
)

// ===== 配送时间估算 =====
const deliveryTimeText = computed(() => {
  const now = new Date()
  const h = now.getHours(), m = now.getMinutes()
  const cutoff = 23 // 23:00截止
  const tomorrowFmt = (() => {
    const d = new Date(now); d.setDate(d.getDate() + 1)
    return `${d.getMonth() + 1}月${d.getDate()}日`
  })()
  const afterTomFmt = (() => {
    const d = new Date(now); d.setDate(d.getDate() + 2)
    return `${d.getMonth() + 1}月${d.getDate()}日`
  })()
  if (h < cutoff) {
    const remain = `${cutoff}:00`
    return `${remain}前付款，预计明天(${tomorrowFmt})送达`
  } else {
    return `预计后天(${afterTomFmt})送达`
  }
})

function formatPrice(p?: number | null) {
  if (p == null) return '--'
  return p.toFixed(2).replace(/\.00$/, '')
}

function normalizeProduct(raw: Record<string, unknown>): ProductData {
  if (raw.product && typeof raw.product === 'object' && 'id' in (raw.product as Record<string, unknown>)) {
    const p = raw.product as Record<string, unknown>
    return {
      ...p,
      skuStockList: (raw.skuStockList ?? p.skuStockList ?? []) as SkuStockInfo[],
      specList: (raw.specList ?? p.specList ?? []) as ProductSpecInfo[],
      introImageUrls: (raw.introImageUrls ?? p.introImageUrls ?? []) as string[],
      detailImageUrls: (raw.detailImageUrls ?? p.detailImageUrls ?? []) as string[],
    } as unknown as ProductData
  }
  return raw as unknown as ProductData
}

function parseSpData(spData?: string): Array<{ key: string; value: string }> {
  if (!spData) return []
  try {
    const p = JSON.parse(spData)
    if (Array.isArray(p)) return p.filter(x => x?.key && x?.value)
  } catch { /* skip */ }
  return []
}

// ── 图片 URL 标准化：仅内部 API 路径可用，CDN 地址已无代理
function normalizeImgUrl(url?: string | null): string {
  if (!url) return ''
  const clean = url.trim()
  // 已经是内部 API 地址，直接返回
  if (clean.startsWith('/api/asset/image/')) return clean
  // 本地静态资源直接返回
  if (clean.startsWith('/') && !clean.startsWith('//')) return clean
  // CDN 地址已无本地代理，降级为空（触发 onError 占位图）
  return ''
}

// ── 处理详情 HTML：确保图片 URL 使用内部 API + 添加懒加载 ──
function processDetailHtml(html: string): string {
  if (!html) return ''
  // 已使用 /api/asset/image/ 的 HTML 只需添加懒加载属性
  if (typeof DOMParser === 'undefined') {
    let out = html
    out = out.replace(/<img\b(?![^>]*\bloading=)/gi, '<img loading="lazy" decoding="async" ')
    return out
  }
  const doc = new DOMParser().parseFromString(html, 'text/html')
  const images = doc.querySelectorAll('img')
  images.forEach((img) => {
    const src = normalizeImgUrl(img.getAttribute('src'))
    if (src) img.setAttribute('src', src)
    if (!img.hasAttribute('loading')) img.setAttribute('loading', 'lazy')
    if (!img.hasAttribute('decoding')) img.setAttribute('decoding', 'async')
    if (img.hasAttribute('srcset')) img.removeAttribute('srcset')
  })
  return doc.body.innerHTML
}

function onImgError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = `data:image/svg+xml,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="600" height="600" fill="%23f5f5f5"><rect width="600" height="600"/><text x="300" y="300" text-anchor="middle" dominant-baseline="central" font-size="18" fill="%23bbb">暂无图片</text></svg>')}`
}

function uniqueUrls(urls: string[]): string[] {
  return Array.from(new Set(urls.filter(Boolean)))
}

function extractAssetUrlsFromHtml(html?: string): string[] {
  if (!html) return []
  const hits = html.match(/\/api\/asset\/image\/[0-9a-f]{64}/gi) ?? []
  return uniqueUrls(hits.map((x) => normalizeImgUrl(x)))
}

const albumPics = computed(() => {
  if (!detail.value) return ['']
  const list: string[] = []
  if (detail.value.pic) list.push(normalizeImgUrl(detail.value.pic))
  const extras = detail.value.albumPics?.split(',').map(s => s.trim()).filter(Boolean) ?? []
  for (const p of extras) {
    const n = normalizeImgUrl(p)
    if (!list.includes(n)) list.push(n)
  }
  return list.length ? list : ['']
})

const skuList = computed(() => detail.value?.skuStockList ?? [])
const specList = computed(() => detail.value?.specList ?? [])

const skuGroups = computed<Record<string, string[]>>(() => {
  const g: Record<string, string[]> = {}
  for (const sku of skuList.value) {
    for (const { key, value } of parseSpData((sku as unknown as Record<string,string>).spData)) {
      if (!g[key]) g[key] = []
      if (!g[key].includes(value)) g[key].push(value)
    }
  }
  return g
})

const selectedSku = computed<SkuStockInfo | null>(() => {
  const keys = Object.keys(selectedAttrs)
  if (!keys.length) return skuList.value[0] ?? null
  return skuList.value.find(sku => {
    const attrs = parseSpData((sku as unknown as Record<string,string>).spData)
    return keys.every(k => attrs.find(a => a.key === k)?.value === selectedAttrs[k])
  }) ?? null
})

const discountRate = computed(() => {
  if (!detail.value?.originalPrice || !detail.value?.price) return ''
  const r = detail.value.price / detail.value.originalPrice
  if (r >= 1) return ''
  return (r * 10).toFixed(1)
})

const introImageList = computed(() => {
  const fromApi = uniqueUrls((detail.value?.introImageUrls ?? []).map((x) => normalizeImgUrl(x)))
  if (fromApi.length) return fromApi
  const fallback = albumPics.value.filter(Boolean)
  return fallback.length ? [fallback[0]] : []
})

const detailImageList = computed(() => {
  const fromApi = uniqueUrls((detail.value?.detailImageUrls ?? []).map((x) => normalizeImgUrl(x)))
  if (fromApi.length) return fromApi
  return extractAssetUrlsFromHtml(detail.value?.detailMobileHtml || detail.value?.detailHtml || '')
})

function warmupDetailImages() {
  const albumQueue = albumPics.value.filter(Boolean).slice(1, 3)
  const introQueue = introImageList.value.filter(Boolean).slice(0, 2)
  const detailQueue = detailImageList.value.filter(Boolean).slice(0, 2)

  warmupImages(albumQueue, {
    scopeId: DETAIL_WARMUP_SCOPE,
    priority: 'immediate',
    maxItems: 4,
  })
  warmupImages([...introQueue, ...detailQueue], {
    scopeId: DETAIL_WARMUP_SCOPE,
    priority: 'idle',
    maxItems: 8,
  })
}

const groupedSpecRows = computed(() => {
  const intro: Array<{ k: string; v: string }> = []
  const params: Array<{ k: string; v: string }> = []
  const afterSale: Array<{ k: string; v: string }> = []
  for (const raw of specList.value) {
    const group = (raw.specGroup || '').trim()
    const name = (raw.specName || '').trim()
    const value = (raw.specValue || '').trim()
    if (!name || !value) continue
    const row = { k: name, v: value }
    if (group.includes('商品详情')) {
      intro.push(row)
    } else if (group.includes('售后')) {
      afterSale.push(row)
    } else {
      params.push(row)
    }
  }
  return { intro, params, afterSale }
})

const introTableRows = computed(() => {
  if (groupedSpecRows.value.intro.length) return groupedSpecRows.value.intro
  const rows: Array<{ k: string; v: string }> = []
  if (detail.value?.brandName) rows.push({ k: '品牌', v: detail.value.brandName })
  if (detail.value?.productSn) rows.push({ k: '商品编号', v: detail.value.productSn })
  if (detail.value?.name) rows.push({ k: '型号', v: detail.value.name })
  return rows
})

const parameterRows = computed(() => {
  if (groupedSpecRows.value.params.length) return groupedSpecRows.value.params
  const rows: Array<{ k: string; v: string }> = []
  if (detail.value?.weight) rows.push({ k: '重量', v: `${detail.value.weight}g` })
  if (detail.value?.unit) rows.push({ k: '单位', v: detail.value.unit })
  for (const [k, vals] of Object.entries(skuGroups.value)) {
    rows.push({ k, v: vals.join(' / ') })
  }
  return rows
})

const afterSaleRows = computed(() => groupedSpecRows.value.afterSale)
const allSpecRows = computed(() => [...introTableRows.value, ...parameterRows.value, ...afterSaleRows.value])

const detailContent = computed(() => {
  const d = detail.value
  if (!d) return ''
  const raw = d.detailMobileHtml || d.detailHtml || ''
  if (!raw) return ''
  return processDetailHtml(raw)
})

function prevImg() { activeImg.value = (activeImg.value - 1 + albumPics.value.length) % albumPics.value.length }
function nextImg() { activeImg.value = (activeImg.value + 1) % albumPics.value.length }

function isDisabled(key: string, val: string) {
  const test = { ...selectedAttrs, [key]: val }
  const ks = Object.keys(test)
  return !skuList.value.some(sku => {
    const attrs = parseSpData((sku as unknown as Record<string,string>).spData)
    return ks.every(k => !test[k] || attrs.find(a => a.key === k)?.value === test[k])
  })
}

function selectAttr(key: string, val: string) {
  if (selectedAttrs[key] === val) { delete selectedAttrs[key] } else { selectedAttrs[key] = val }
}

async function syncMemberFlags() {
  if (!localStorage.getItem('token') || !detail.value) return
  try { const { data } = await memberCollectionApi.detail(detail.value.id); collected.value = !!data.data } catch { collected.value = false }
}

async function createReadHistory() {
  if (!localStorage.getItem('token') || !detail.value) return
  try {
    await memberReadHistoryApi.create({
      productId: detail.value.id, productName: detail.value.name,
      productPic: detail.value.pic, productSubTitle: detail.value.subTitle,
      productPrice: detail.value.price != null ? String(detail.value.price) : undefined,
    })
  } catch { /* non-blocking */ }
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) { showToast('商品参数错误'); return }
  try {
    const { data } = await appProductApi.detail(id)
    const normalized = normalizeProduct(data.data as unknown as Record<string, unknown>)
    detail.value = normalized
    if (normalized.skuStockList?.length) {
      for (const { key, value } of parseSpData((normalized.skuStockList[0] as unknown as Record<string,string>).spData))
        selectedAttrs[key] = value
    }
    warmupDetailImages()
    await Promise.all([syncMemberFlags(), createReadHistory()])
  } catch { showToast('加载失败') }
  // 尝试加载用户收货地址（需登录）
  try {
    const token = localStorage.getItem('token')
    if (token) {
      isLoggedIn.value = true
      const res = await memberAddressApi.list()
      const list = res.data.data || []
      addressList.value = list
      // 预选默认地址
      const def = list.find((a: MemberAddress) => a.defaultStatus === 1) || list[0]
      if (def) {
        selectedAddrId.value = def.id ?? null
        selectedAddrText.value = `${def.province || ''}${def.city || ''}${def.region || ''}`
        if (def.province) areaSelected.value[0] = def.province
        if (def.city) areaSelected.value[1] = def.city
        if (def.region) areaSelected.value[2] = def.region
      }
    }
  } catch { /* 未登录时忽略 */ }
})

async function toggleCollection() {
  if (!localStorage.getItem('token')) { router.push('/login'); return }
  if (!detail.value) return
  try {
    if (collected.value) {
      await memberCollectionApi.delete(detail.value.id); collected.value = false; showToast('已取消收藏')
    } else {
      await memberCollectionApi.add({ productId: detail.value.id, productName: detail.value.name, productPic: detail.value.pic, productSubTitle: detail.value.subTitle, productPrice: detail.value.price != null ? String(detail.value.price) : undefined })
      collected.value = true; showToast('已收藏')
    }
  } catch (e: unknown) { showToast((e as Error).message || '操作失败') }
}

async function addToCart() {
  if (!localStorage.getItem('token')) { router.push('/login'); return }
  if (!detail.value) return
  adding.value = true
  try {
    const sku = selectedSku.value
    const attrs = sku ? parseSpData((sku as unknown as Record<string,string>).spData).map(a => `${a.key}:${a.value}`).join(';') : ''
    await cartApi.add({
      id: 0, productId: detail.value.id, productSkuId: sku?.id,
      quantity: qty.value, price: sku?.price ?? detail.value.price,
      productPic: detail.value.pic, productName: detail.value.name,
      productSubTitle: detail.value.subTitle, productSkuCode: sku?.skuCode,
      productCategoryId: detail.value.productCategoryId,
      productBrand: detail.value.brandName, productSn: detail.value.productSn,
      productAttr: attrs,
    } as never)
    showToast('已加入购物车')
  } catch (e: unknown) { showToast((e as Error).message || '加入失败') }
  finally { adding.value = false }
}

async function buyNow() {
  if (!localStorage.getItem('token')) { router.push('/login'); return }
  await addToCart(); router.push('/cart')
}

// ========= 加入购物车飞入动画（仿京东）=========
// 全局注入购物车弹跳动画 CSS（只注入一次）
function ensureCartBounceCSS() {
  if (document.getElementById('cart-bounce-style')) return
  const style = document.createElement('style')
  style.id = 'cart-bounce-style'
  style.textContent = `
    @keyframes cartBounce {
      0%   { transform: scale(1); }
      30%  { transform: scale(1.35); }
      60%  { transform: scale(0.88); }
      80%  { transform: scale(1.15); }
      100% { transform: scale(1); }
    }
    .cart-icon-bounce {
      animation: cartBounce 0.5s ease !important;
    }
  `
  document.head.appendChild(style)
}

function flyToCart(event: MouseEvent) {
  ensureCartBounceCSS()
  // 找目标购物车图标：优先底栏可见的，其次桌面导航栏的
  const mobileCart = document.getElementById('mobile-cart-icon') as HTMLElement | null
  const globalCart = document.getElementById('global-cart-icon') as HTMLElement | null
  // 取第一个在视口中有实际尺寸的元素
  const cartTarget = [mobileCart, globalCart].find(el => {
    if (!el) return false
    const r = el.getBoundingClientRect()
    return r.width > 0 && r.height > 0
  }) || null
  if (!cartTarget) return

  const btn = event.currentTarget as HTMLElement
  const btnRect = btn.getBoundingClientRect()
  const cartRect = cartTarget.getBoundingClientRect()

  const startX = btnRect.left + btnRect.width / 2
  const startY = btnRect.top + btnRect.height / 2
  const endX = cartRect.left + cartRect.width / 2
  const endY = cartRect.top + cartRect.height / 2

  // 创建飞球
  const ball = document.createElement('div')
  ball.style.cssText = `
    position:fixed;
    width:18px;height:18px;
    border-radius:50%;
    background:linear-gradient(135deg,#f60,#e3000b);
    box-shadow:0 2px 8px rgba(255,100,0,.5);
    z-index:99999;
    pointer-events:none;
    left:${startX - 9}px;
    top:${startY - 9}px;
  `
  document.body.appendChild(ball)

  const duration = 750
  const startTime = performance.now()

  function animate(now: number) {
    const t = Math.min((now - startTime) / duration, 1)
    // ease-in-out
    const ease = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t
    const x = startX + (endX - startX) * ease - 9
    // 抛物线弧度（向上弯）
    const arc = -Math.sin(Math.PI * t) * 120
    const y = startY + (endY - startY) * t + arc - 9
    const scale = 1 - t * 0.6
    const opacity = t > 0.75 ? (1 - t) / 0.25 : 1

    ball.style.left = `${x}px`
    ball.style.top = `${y}px`
    ball.style.transform = `scale(${scale})`
    ball.style.opacity = String(opacity)

    if (t < 1) {
      requestAnimationFrame(animate)
    } else {
      document.body.removeChild(ball)
      // 购物车图标抖动反馈
      if (cartTarget) {
        cartTarget.classList.add('cart-icon-bounce')
        setTimeout(() => cartTarget.classList.remove('cart-icon-bounce'), 500)
      }
    }
  }
  requestAnimationFrame(animate)
}

async function handleAddToCart(event: MouseEvent) {
  flyToCart(event)
  await addToCart()
}

onBeforeUnmount(() => {
  cancelWarmup(DETAIL_WARMUP_SCOPE)
})
</script>

<style scoped>
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
.jd-detail-page{min-height:100vh;background:#f5f5f5;font-family:'PingFang SC','Microsoft YaHei',sans-serif;color:#333;padding-bottom:64px}

/* 导航 */
.jd-nav{position:sticky;top:0;z-index:100;height:48px;display:flex;align-items:center;padding:0 12px;background:#fff;box-shadow:0 1px 3px rgba(0,0,0,.08)}
.nav-back{width:36px;height:36px;border:none;background:none;cursor:pointer;display:flex;align-items:center;justify-content:center;color:#333}
.nav-back svg{width:22px;height:22px}
.nav-title{flex:1;text-align:center;font-size:16px;font-weight:600}
.nav-actions{display:flex;gap:8px}
.nav-btn{width:36px;height:36px;border:none;background:none;cursor:pointer;color:#333;display:flex;align-items:center;justify-content:center}
.nav-btn svg{width:22px;height:22px}

/* 内容 */
.jd-content{max-width:1200px;margin:0 auto}

/* 顶部面板 */
.jd-top-panel{display:flex;flex-direction:column;background:#fff;margin-bottom:8px}
@media(min-width:900px){.jd-top-panel{flex-direction:row;padding:24px 28px;gap:32px;align-items:flex-start}}

/* 画廊 */
.jd-gallery{width:100%;background:#fff;flex-shrink:0}
@media(min-width:900px){.jd-gallery{width:400px}}
.gallery-main{position:relative;background:#fafafa;aspect-ratio:1/1;overflow:hidden}
.main-img{width:100%;height:100%;object-fit:contain;display:block}
.img-counter{position:absolute;bottom:10px;right:14px;background:rgba(0,0,0,.45);color:#fff;font-size:11px;padding:2px 8px;border-radius:10px}
.arrow{position:absolute;top:50%;transform:translateY(-50%);background:rgba(0,0,0,.35);color:#fff;border:none;font-size:28px;line-height:1;width:36px;height:48px;cursor:pointer;border-radius:4px;transition:background .2s;display:flex;align-items:center;justify-content:center}
.arrow:hover{background:rgba(0,0,0,.6)}
.arrow-left{left:8px}
.arrow-right{right:8px}
.gallery-thumbs{display:flex;gap:6px;padding:8px 10px;overflow-x:auto;background:#fff}
.thumb{width:56px;height:56px;object-fit:contain;border:2px solid transparent;border-radius:4px;cursor:pointer;flex-shrink:0;background:#fafafa;transition:border-color .15s}
.thumb.active,.thumb:hover{border-color:#e4393c}

/* 商品信息 */
.jd-info{flex:1;padding:16px;display:flex;flex-direction:column;gap:0}
.product-name{font-size:16px;font-weight:700;line-height:1.5;color:#111;margin-bottom:6px}
@media(min-width:900px){.product-name{font-size:19px}}
.product-subtitle{font-size:13px;color:#666;line-height:1.6;margin-bottom:12px}

/* 价格 */
.price-zone{background:linear-gradient(135deg,#d4000a,#c41230);padding:14px 16px;margin:0 -16px 14px}
.price-row{display:flex;align-items:baseline;gap:10px;flex-wrap:wrap}
.price-row-sub{margin-top:6px}
.price-label{color:rgba(255,255,255,.7);font-size:12px;min-width:38px}
.price-current{color:#fff;font-size:34px;font-weight:800;line-height:1}
.price-current em{font-size:16px;font-style:normal}
.price-origin{color:rgba(255,255,255,.6);font-size:13px;text-decoration:line-through}
.price-badge{background:rgba(255,255,255,.25);color:#fff;font-size:11px;padding:2px 6px;border-radius:3px}
.sold-num{color:rgba(255,255,255,.85);font-size:13px}
.stock-info{color:rgba(255,255,255,.65);font-size:12px}

/* 促销 */
.promo-zone{margin-bottom:14px;display:flex;flex-direction:column;gap:7px}
.promo-item{display:flex;align-items:flex-start;gap:8px;font-size:13px}
.promo-tag{background:#e4393c;color:#fff;font-size:11px;padding:1px 6px;border-radius:3px;white-space:nowrap;margin-top:1px;flex-shrink:0}
.promo-text{color:#555;line-height:1.5}

/* SKU */
.sku-zone{margin-bottom:14px;display:flex;flex-direction:column;gap:12px;padding:12px 0;border-top:1px solid #f0f0f0}
.sku-group{display:flex;flex-direction:column;gap:8px}
.sku-group-label{font-size:13px;color:#555}
.sku-sel-val{color:#e4393c;font-weight:600}
.sku-opts{display:flex;gap:8px;flex-wrap:wrap}
.sku-opt{padding:5px 14px;border:1px solid #ddd;border-radius:4px;font-size:13px;cursor:pointer;background:#fff;transition:all .15s}
.sku-opt:hover{border-color:#e4393c;color:#e4393c}
.sku-opt.active{border-color:#e4393c;color:#e4393c;background:#fff5f5}
.sku-opt.disabled{opacity:.4;cursor:not-allowed}

/* 数量 / 配送 */
.quantity-zone{margin-bottom:14px;display:flex;flex-direction:column;gap:8px;padding-bottom:12px;border-bottom:1px solid #f0f0f0}
.qty-row{display:flex;align-items:center;gap:14px}
.qty-label{font-size:13px;color:#555;min-width:36px}
.qty-ctrl{display:flex;align-items:center;border:1px solid #ddd;border-radius:4px;overflow:hidden}
.qty-btn{width:32px;height:32px;border:none;background:#f5f5f5;font-size:18px;cursor:pointer;line-height:1;transition:background .15s}
.qty-btn:hover{background:#e8e8e8}
.qty-num{width:44px;text-align:center;font-size:14px;font-weight:600}
.ship-info{font-size:13px;color:#555}

/* 服务保障 */
.service-zone{display:flex;flex-wrap:wrap;gap:8px 16px;margin-bottom:16px;padding:10px 12px;background:#f9f9f9;border-radius:6px}
.svc-item{font-size:12px;color:#555}

/* 桌面操作按钮 */
.desktop-only{display:none}
.mobile-only{display:flex}
@media(min-width:900px){.desktop-only{display:flex}.mobile-only{display:none}}
.action-zone{display:flex;align-items:center;gap:10px;margin-top:8px}
.btn-collect{display:flex;flex-direction:column;align-items:center;gap:2px;border:1px solid #ddd;background:#fff;border-radius:6px;padding:8px 14px;cursor:pointer;color:#888;font-size:12px;transition:all .15s}
.btn-collect svg{width:20px;height:20px}
.btn-collect.active{color:#e4393c;border-color:#e4393c}
.btn-cart-d{flex:1;height:46px;border:none;border-radius:6px;background:#f60;color:#fff;font-size:15px;font-weight:700;cursor:pointer;transition:background .15s}
.btn-cart-d:hover{background:#e65c00}
.btn-buy-d{flex:1;height:46px;border:none;border-radius:6px;background:#e4393c;color:#fff;font-size:15px;font-weight:700;cursor:pointer;transition:background .15s}
.btn-buy-d:hover{background:#c41230}

/* 标签页 */
.jd-tabs-wrap{position:sticky;top:48px;z-index:90;background:#fff;border-bottom:1px solid #eee;box-shadow:0 2px 4px rgba(0,0,0,.04)}
.jd-tabs{display:flex;max-width:1200px;margin:0 auto}
.tab-btn{flex:1;height:44px;border:none;background:none;font-size:14px;font-weight:500;color:#666;cursor:pointer;position:relative;transition:color .15s}
.tab-btn.active{color:#e4393c;font-weight:700}
.tab-btn.active::after{content:'';position:absolute;bottom:0;left:50%;transform:translateX(-50%);width:32px;height:3px;background:#e4393c;border-radius:2px}

/* Tab内容 */
.tab-panel{background:#fff;min-height:200px}

/* 规格快览 */
.spec-quick{display:grid;grid-template-columns:1fr 1fr;gap:0;padding:14px 16px;border-bottom:1px solid #f0f0f0}
@media(min-width:600px){.spec-quick{grid-template-columns:repeat(3,1fr)}}
.spec-q-row{padding:8px;border-bottom:1px solid #f8f8f8}
.spec-k{font-size:11px;color:#999;display:block;margin-bottom:3px}
.spec-v{font-size:13px;color:#333;font-weight:500}

/* 详情HTML */
.detail-html-wrap{background:#fff}
.section-title{font-size:14px;font-weight:700;color:#333;padding:14px 16px 10px;border-top:4px solid #e4393c;letter-spacing:.03em}
.detail-image-wrap{background:#fff}
.detail-image-list{padding:0 16px 16px}
.detail-image-list img{display:block;width:100%;height:auto;border-radius:4px;margin:0 0 12px}
.detail-image-list img:last-child{margin-bottom:0}
.intro-spec-table{margin-top:4px}
.detail-html :deep(*){max-width:100%}
.detail-html :deep(img){display:block;width:100%;height:auto;margin:0 auto}
.detail-html :deep(p){font-size:14px;line-height:1.8;color:#444;padding:4px 16px}
.detail-html :deep(div){max-width:100%;overflow:hidden}

/* detail-html 内嵌的商品参数表格（jd-spec-section）*/
.detail-html :deep(.jd-spec-section){padding:16px;background:#fff;margin-bottom:8px}
.detail-html :deep(.spec-title){font-size:15px;font-weight:700;color:#111;padding:0 0 12px;border-bottom:2px solid #e4393c;margin-bottom:0}
.detail-html :deep(.spec-tbl){width:100%;border-collapse:collapse;margin-top:4px}
.detail-html :deep(.spec-tbl td){padding:9px 12px;font-size:13px;border:1px solid #f0f0f0;vertical-align:top;line-height:1.5}
.detail-html :deep(.spec-tbl .spec-k){width:100px;color:#666;background:#f7f7f7;font-weight:500;white-space:nowrap}
.detail-html :deep(.spec-tbl .spec-v){color:#333;min-width:80px}
/* 两列布局：每行 k-v-k-v，spec-v 各占剩余宽度的一半 */
.detail-html :deep(.spec-tbl-2col .spec-v){width:calc(50% - 100px)}
.detail-html :deep(.jd-detail-imgs img){display:block;width:100%;height:auto;margin:0}

/* 规格表 */
.spec-table{padding:10px 0}
.spec-row{display:flex;border-bottom:1px solid #f5f5f5;padding:10px 16px}
.sk{width:90px;min-width:90px;color:#999;font-size:13px}
.sv{flex:1;font-size:13px;color:#333}

/* 售后 */
.service-list{padding:16px;display:flex;flex-direction:column;gap:14px}
.svc-card{display:flex;align-items:flex-start;gap:14px;padding:14px;background:#f9f9f9;border-radius:8px}
.svc-icon{font-size:28px;line-height:1}
.svc-title{font-size:14px;font-weight:700;margin-bottom:4px}
.svc-desc{font-size:12px;color:#777;line-height:1.6}

/* 移动底部 */
.jd-bottom-bar{position:fixed;bottom:0;left:0;right:0;z-index:100;height:56px;background:#fff;box-shadow:0 -1px 6px rgba(0,0,0,.1);align-items:stretch}
.bar-icon-btn{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:2px;border:none;background:none;cursor:pointer;font-size:10px;color:#555;width:56px;flex-shrink:0}
.bar-icon-btn svg{width:20px;height:20px}
.bar-btn{flex:1;border:none;font-size:15px;font-weight:700;cursor:pointer}
.bar-cart{background:#f60;color:#fff}
.bar-buy{background:#e4393c;color:#fff}

/* 骨架屏 */
.skeleton-wrap{padding:16px;display:flex;flex-direction:column;gap:12px}
.skel-img{height:300px;background:#eee;border-radius:8px;animation:shimmer 1.5s infinite}
.skel-line{height:16px;background:#eee;border-radius:6px;animation:shimmer 1.5s infinite}
.w80{width:80%}.w60{width:60%}.w40{width:40%}
@keyframes shimmer{0%,100%{opacity:.5}50%{opacity:1}}

/* ===== 送至 / 配送时间 ===== */
.deliver-row{cursor:pointer}
.deliver-row:hover .deliver-text{color:#e4393c}
.deliver-text{flex:1;display:flex;align-items:center;gap:4px;font-size:13px;color:#333}
.deliver-chevron{width:10px;height:10px;flex-shrink:0;opacity:.5}
.free-ship{font-size:12px;color:#e4393c;border:1px solid #e4393c;border-radius:2px;padding:0 4px;margin-left:6px;white-space:nowrap}
.deliver-time{font-size:13px;color:#333;line-height:1.6}
.deliver-time em{font-style:normal;font-weight:700;color:#333}

/* ===== 地址选择弹窗 ===== */
.addr-mask{position:fixed;inset:0;z-index:2000;background:rgba(0,0,0,.45);display:flex;align-items:flex-end}
.addr-panel{width:100%;max-height:75vh;background:#fff;border-radius:16px 16px 0 0;display:flex;flex-direction:column;overflow:hidden}
.addr-panel-hd{display:flex;align-items:center;justify-content:space-between;padding:16px 20px 0;flex-shrink:0}
.addr-panel-title{font-size:16px;font-weight:700;color:#111}
.addr-panel-close{background:none;border:none;font-size:22px;color:#888;cursor:pointer;padding:0 4px;line-height:1}
.addr-tabs{display:flex;border-bottom:1px solid #f0f0f0;padding:0 12px;margin-top:8px;flex-shrink:0}
.addr-tab{padding:10px 16px;font-size:14px;border:none;background:none;cursor:pointer;color:#666;position:relative}
.addr-tab.active{color:#e4393c;font-weight:600}
.addr-tab.active::after{content:'';position:absolute;bottom:0;left:16px;right:16px;height:2px;background:#e4393c;border-radius:2px}

/* 常用地址列表 */
.addr-saved-wrap{flex:1;overflow-y:auto;padding:8px 0}
.addr-empty{display:flex;flex-direction:column;align-items:center;gap:10px;padding:30px 20px;color:#999;font-size:14px}
.addr-link-btn{border:1px solid #e4393c;color:#e4393c;background:none;border-radius:4px;padding:6px 16px;cursor:pointer;font-size:13px}
.addr-item{position:relative;padding:14px 20px;border-bottom:1px solid #f5f5f5;cursor:pointer;transition:background .15s}
.addr-item:hover{background:#fffafa}
.addr-item--sel{background:#fff8f8}
.addr-item--sel::before{content:'';position:absolute;left:0;top:0;bottom:0;width:3px;background:#e4393c;border-radius:0 2px 2px 0}
.addr-item-top{display:flex;align-items:center;gap:8px;margin-bottom:4px}
.addr-item-name{font-size:14px;font-weight:600;color:#111}
.addr-item-tel{font-size:13px;color:#666}
.addr-default-tag{font-size:11px;color:#e4393c;border:1px solid #e4393c;border-radius:2px;padding:0 4px}
.addr-item-full{font-size:13px;color:#555;line-height:1.5}

/* 三列级联地区选择器 */
.area-picker{flex:1;display:flex;overflow:hidden}
.area-col{flex:1;display:flex;flex-direction:column;border-right:1px solid #f5f5f5}
.area-col:last-child{border-right:none}
.area-col-hd{padding:10px 12px;font-size:12px;color:#999;border-bottom:1px solid #f5f5f5;flex-shrink:0;text-align:center;background:#fafafa}
.area-col-body{flex:1;overflow-y:auto}
.area-item{padding:12px 10px;font-size:13px;color:#333;cursor:pointer;text-align:center;line-height:1.4;border-bottom:1px solid #fafafa}
.area-item:hover{background:#fff5f5}
.area-item.active{color:#e4393c;font-weight:600;background:#fff0f0}

/* Transition */
.addr-fade-enter-active,.addr-fade-leave-active{transition:opacity .2s ease}
.addr-fade-enter-from,.addr-fade-leave-to{opacity:0}
.addr-fade-enter-active .addr-panel,.addr-fade-leave-active .addr-panel{transition:transform .25s ease}
.addr-fade-enter-from .addr-panel,.addr-fade-leave-to .addr-panel{transform:translateY(100%)}</style>

