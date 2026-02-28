<template>
  <div :class="['app-shell', headerVariant === 'inner' ? 'app-shell--inner' : '', headerVariant === 'mine' ? 'app-shell--mine' : '']">    
    <!-- ====== 1. Topbar: 顶部灰字工具条 ====== -->
    <div class="topbar">
      <div class="topbar-inner">
        <div class="topbar-left">
          <router-link to="/" class="topbar-link">京东首页</router-link>
          <span class="topbar-sep">|</span>
          <span class="topbar-link">京东全球 ▾</span>
        </div>
        <div class="topbar-right">
          <template v-if="!userStore.userInfo">
            <router-link to="/login" class="topbar-link">你好，请登录</router-link>
            <span class="topbar-sep">|</span>
            <router-link to="/register" class="topbar-link topbar-link--accent">免费注册</router-link>
          </template>
          <template v-else>
            <span class="topbar-link">你好，{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
          </template>
          <span class="topbar-sep">|</span>
          <router-link to="/order/list" class="topbar-link" :class="{ 'topbar-link--active': route.path === '/order/list' }">我的订单</router-link>
          <span class="topbar-sep">|</span>
          <router-link to="/cart" class="topbar-link" :class="{ 'topbar-link--active': route.path === '/cart' }">购物车</router-link>
          <span class="topbar-sep">|</span>
          <router-link to="/mine" class="topbar-link" :class="{ 'topbar-link--active': route.path === '/mine' }">我的</router-link>
          <span class="topbar-sep">|</span>
          <span class="topbar-link">客户服务</span>
          <span class="topbar-sep">|</span>
          <span class="topbar-link">网站导航</span>
        </div>
      </div>
    </div>

    <!-- ====== 2. MainHeader: Logo + Search + Cart ====== -->
    <header class="main-header" :data-variant="headerVariant">
      <div class="main-header-inner">
        <!-- Logo区 -->
        <div class="header-logo-area">
          <router-link to="/" class="header-logo">
            <img class="header-logo-mark" :src="siteLogoSrc" alt="河北工业大学校徽" />
            <span class="header-logo-text">好物星球</span>
          </router-link>
          <template v-if="headerVariant === 'mine'">
            <span class="header-mine-divider"></span>
            <span class="header-mine-title">我的京东</span>
          </template>
        </div>

        <!-- Search区 (京东设计风：定宽 + 剧中) -->
        <div class="header-search">
          <div class="header-search-box">
            <input
              v-model="searchKeyword"
              type="text"
              class="header-search-input"
              placeholder="搜索商品、品牌、分类..."
              @keyup.enter="doSearch"
              @focus="searchFocused = true"
              @blur="searchFocused = false"
            />
            <button class="header-search-btn" @click="doSearch">搜索</button>
          </div>
          <!-- MAIL ICON - positioned floating so it doesn't affect the search width -->
          <router-link to="/mine" class="header-mail-btn">
             <!-- envelop icon -->
             <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#666" stroke-width="2" stroke-linecap="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
             <span>我的Mail</span>
          </router-link>
          <!-- 热词导航栏（移动至搜索框正下方） -->
          <div class="header-hot-terms">
            <span v-for="(term, index) in hotTerms" :key="term" class="header-hot-term" :class="{ 'hot-term--highlight': index === 0 }" @click="router.push('/search?keyword='+term)">{{ term }}</span>
          </div>
        </div>

        <!-- Cart区 -->
        <div class="header-cart-wrap">
          <router-link to="/cart" class="header-cart" id="global-cart-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2" stroke-linecap="round">
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
            </svg>
            <span class="header-cart-text">我的购物车</span>
            <span v-if="cartStore.count > 0" class="header-cart-badge">{{ cartStore.count > 99 ? '99+' : cartStore.count }}</span>
          </router-link>
        </div>
      </div>
    </header>

    <!-- ====== 3. Main Content ====== -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <component :is="Component" :key="route.path" />
      </router-view>
    </main>

    <!-- ====== 4. Mobile Bottom Tabbar ====== -->
    <van-tabbar v-if="showTabbar" v-model="activeTab" route class="mobile-tabbar" active-color="#e3000b" inactive-color="#666">
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/category" icon="apps-o">分类</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="cart-o" :badge="cartStore.count > 0 ? String(cartStore.count) : ''">购物车</van-tabbar-item>
      <van-tabbar-item to="/mine" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Tabbar as VanTabbar, TabbarItem as VanTabbarItem } from 'vant'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const siteLogoSrc = '/api/asset/image/e979c3eaceb410ae43b5a0f1dd91de0b5fdd30ae3b57c195c08015b6b299ff54'

/* ── Header Variant ── */
// 首页/分类/搜索/商品详情 → home（完整大搜索）
// 我的个人中心            → mine（Logo|我的京东 + 搜索，无购物车）
// 其他内页（购物车/订单等）→ inner（紧凑版）
const headerVariant = computed(() => {
  const p = route.path
  if (p === '/' || p.startsWith('/category') || p.startsWith('/search') || p.startsWith('/product/') || p.startsWith('/jd-item')) return 'home'
  if (p === '/mine') return 'mine'
  return 'inner'
})

/* ── Search ── */
const searchKeyword = ref('')
const searchFocused = ref(false)
const hotTerms = ['平板电脑', '爆款耳机', '手机', '女装', '游戏本', '运动鞋']

function doSearch() {
  const kw = searchKeyword.value.trim()
  if (kw) {
    router.push(`/search?keyword=${encodeURIComponent(kw)}`)
  } else {
    router.push('/search')
  }
}

/* ── Init ── */
onMounted(() => {
  userStore.fetchUser()
  cartStore.fetchCount()
})

/* 路由变化时刷新购物车数量 */
watch(() => route.path, () => {
  if (route.path === '/cart' || route.path === '/') {
    cartStore.fetchCount()
  }
})

/* ── Tabbar ── */
const activeTab = ref(0)
const hiddenTabbarPages = ['/login', '/search', '/register']
const showTabbar = computed(() => {
  const path = route.path
  return !hiddenTabbarPages.includes(path) && !path.startsWith('/product/') && !path.startsWith('/order/')
})
</script>

<style>
/* ============================================================
   全站统一 Layout — Topbar + MainHeader + Content
   ============================================================ */
body {
  margin: 0;
  background: var(--t-bg, #f5f5f5);
}

.app-shell {
  min-height: 100vh;
  padding-bottom: 60px; /* mobile tabbar space */
}

/* ── 全站统一 container 宽度 ── */
.site-container {
  width: 100%;
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 0 16px;
  box-sizing: border-box;
}

/* ─────────────────────────────────────────────
   Topbar — 32px 灰色工具条（仅桌面端）
   ───────────────────────────────────────────── */
.topbar { display: none; }

/* ─────────────────────────────────────────────
   MainHeader — Logo + Search + Cart
   ───────────────────────────────────────────── */
.main-header { display: none; }

/* ── Hot terms (hidden on mobile) ── */
.header-hot-terms { display: none; }

/* ── Mobile: Search bar simplified ── */
@media (max-width: 1023px) {
  .main-header {
    display: block;
    position: sticky;
    top: 0;
    z-index: 100;
    background: #fff;
    border-bottom: 1px solid #eee;
    padding: 8px 12px;
  }
  .main-header-inner {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .header-logo {
    display: flex;
    align-items: center;
    gap: 5px;
    text-decoration: none;
    flex-shrink: 0;
  }
  .header-logo-mark {
    width: 28px;
    height: 28px;
    display: block;
    object-fit: cover;
    border-radius: 50%;
  }
  .header-logo-text {
    font-size: 18px;
    font-weight: 800;
    color: #e3000b;
    letter-spacing: -0.03em;
  }
  .header-search { flex: 1; min-width: 0; }
  .header-search-box {
    display: flex;
    align-items: center;
    border: 1.5px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
    height: 36px;
    background: #fff;
    transition: border-color 180ms;
  }
  .header-search-box:focus-within {
    border-color: #e3000b;
    box-shadow: 0 0 0 2px rgba(227,0,11,.08);
  }
  .header-search-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 0 12px;
    font-size: 14px;
    height: 100%;
    background: transparent;
    color: #333;
  }
  .header-search-input::placeholder { color: #bbb; }
  .header-search-btn {
    flex-shrink: 0;
    height: 100%;
    padding: 0 16px;
    background: #e3000b;
    color: #fff;
    border: none;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
  }
  .header-cart {
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    text-decoration: none;
    color: #333;
    flex-shrink: 0;
    width: 36px;
    height: 36px;
  }
  .header-cart-text { display: none; }
  .header-cart-badge {
    position: absolute;
    top: -2px;
    right: -4px;
    min-width: 16px;
    height: 16px;
    background: #e3000b;
    color: #fff;
    font-size: 10px;
    font-weight: 600;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
    line-height: 1;
  }
}

/* ── Mobile Bottom Tabbar ── */
.mobile-tabbar.van-tabbar {
  box-shadow: 0 -1px 8px rgba(0,0,0,.08);
}

/* ─────────────────────────────────────────────
   Desktop: min-width 1024px
   ───────────────────────────────────────────── */
@media (min-width: 1024px) {
  .app-shell {
    padding-bottom: 0;
    padding-top: 132px; /* 32px topbar + 100px main header */
  }

  /* Hide mobile tabbar */
  .mobile-tabbar.van-tabbar { display: none !important; }

  /* ── Topbar ── */
  .topbar {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1001;
    height: 32px;
    background: #f5f5f5;
    border-bottom: 1px solid #e8e8e8;
  }
  .topbar-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    max-width: var(--app-content-max-width, 1200px);
    margin: 0 auto;
    height: 100%;
    padding: 0 16px;
    box-sizing: border-box;
  }
  .topbar-left,
  .topbar-right {
    display: flex;
    align-items: center;
  }
  .topbar-link {
    font-size: 12px;
    color: #999;
    cursor: pointer;
    text-decoration: none;
    padding: 0 8px;
    line-height: 32px;
    transition: color 120ms;
    white-space: nowrap;
  }
  .topbar-link:hover { color: #e3000b; }
  .topbar-link--accent { color: #e3000b; font-weight: 500; }
  .topbar-link--active {
    color: #333;
    font-weight: 500;
    position: relative;
  }
  .topbar-link--active::after {
    content: '';
    position: absolute;
    bottom: 2px;
    left: 50%;
    transform: translateX(-50%);
    width: 16px;
    height: 2px;
    background: #e3000b;
    border-radius: 1px;
  }
  .topbar-sep { color: #ddd; font-size: 11px; line-height: 32px; }

  /* ── MainHeader ── */
  .main-header {
    display: block;
    position: fixed;
    top: 32px;
    left: 0;
    right: 0;
    z-index: 1000;
    height: 100px;
    background: #fff;
    border-bottom: 1px solid #eee;
  }
  .main-header-inner {
    display: grid;
    grid-template-columns: 200px 1fr 190px; /* Maps exactly to Sidebar + Banner + Right-panel */
    gap: 16px; /* Maps exactly to home body gap */
    align-items: center; /* row alignment */
    width: 100%;
    max-width: var(--app-content-max-width, 1200px);
    margin: 0 auto;
    height: 100%;
    padding: 0 16px;
    box-sizing: border-box;
  }

  /* Logo */
  .header-logo-area {
    width: auto;
    flex-shrink: 0;
    margin-top: 0;
  }
  .header-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    text-decoration: none;
  }
  .header-logo-mark {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }
  .header-logo-text {
    font-size: 26px;
    font-weight: 800;
    color: #e3000b;
    letter-spacing: -0.02em;
    font-family: var(--t-font);
    line-height: 1;
    white-space: nowrap;
  }

  /* Search */
  .header-search {
    width: 100%; /* 充满 1fr 格子，与轮播图宽度一致 */
    min-width: 0; 
    position: relative;
    display: flex;
    justify-content: flex-start;
    margin: 0; /* 不再居中，直接撑满 */
  }
  .header-search-box {
    display: flex;
    align-items: center;
    width: 100%;
    border: 2px solid #e3000b;
    border-radius: 0;
    overflow: hidden;
    height: 38px;
    background: #fff;
    transition: box-shadow 180ms;
  }
  .header-search-box:focus-within {
    box-shadow: 0 0 4px rgba(227,0,11,.3);
  }
  .header-search-input {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    padding: 0 16px;
    font-size: 14px;
    height: 100%;
    background: transparent;
    color: #333;
    font-family: var(--t-font);
  }
  .header-search-input::placeholder { color: #bbb; }
  .header-search-btn {
    flex-shrink: 0;
    height: 100%;
    padding: 0 32px;
    background: #e3000b;
    color: #fff;
    border: none;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    font-family: var(--t-font);
    border-radius: 0;
  }
  .header-search-btn:hover { background: #c00; }
  
  /* Mail Button */
  .header-mail-btn {
    position: absolute;
    right: -80px; 
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    text-decoration: none;
    color: #888;
    font-size: 11px;
    gap: 2px;
  }
  .header-mail-btn:hover { color: #e3000b; }

  /* Hot terms */
  .header-hot-terms {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    display: flex;
    gap: 12px;
    align-items: center;
    margin-top: 6px;
    height: 18px;
    overflow: hidden;
  }
  .header-hot-term {
    font-size: 12px;
    color: #999;
    cursor: pointer;
    line-height: 1;
    transition: color 150ms;
    white-space: nowrap;
  }
  .header-hot-term.hot-term--highlight {
    color: #e3000b;
  }
  .header-hot-term:not(.hot-term--highlight):hover { color: #e3000b; }

  /* Cart */
  .header-cart-wrap {
    margin-top: 0;
    width: auto;
    display: flex;
    justify-content: flex-end; /* right-align */
  }
  .header-cart {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    text-decoration: none;
    color: #e3000b;
    background: #fff;
    border: 1px solid #e0e0e0;
    border-radius: 0;
    padding: 0;
    height: 38px;
    width: 100%; /* Force Cart to be exact width of container to match panel */
    position: relative;
    transition: border-color 180ms;
    white-space: nowrap;
    flex-shrink: 0;
  }
  .header-cart:hover {
    border-color: #e3000b;
  }
  .header-cart svg { flex-shrink: 0; }
  .header-cart-text {
    font-size: 14px;
    font-weight: 600;
  }
  .header-cart-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    min-width: 18px;
    height: 18px;
    background: #e3000b;
    color: #fff;
    font-size: 11px;
    font-weight: 700;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 4px;
    line-height: 1;
    box-shadow: 0 1px 3px rgba(227,0,11,.3);
  }
}

/* ─────────────────────────────────────────────
   Large Desktop (≥ 1440px): 匹配更大的侧边栏与右侧边栏
   ───────────────────────────────────────────── */
@media (min-width: 1440px) {
  .main-header-inner {
    grid-template-columns: 220px 1fr 208px; /* SAME AS 1440px sidebars */
  }
}

/* ── Main Content Area ── */
.app-main {
  width: 100%;
}

/* ─────────────────────────────────────────────
   Desktop inner variant — 紧凑 Header (64px)
   ───────────────────────────────────────────── */
@media (min-width: 1024px) {
  /* 内页 padding-top: 32px topbar + 56px inner header */
  .app-shell--inner {
    padding-top: 88px;
  }

  /* 内页 Header 高度缩为 56px */
  .main-header[data-variant="inner"] {
    height: 56px;
  }

  /* 内页：inner header 内容行占满整个 header */
  .main-header[data-variant="inner"] .main-header-inner {
    align-items: center;
  }
  .main-header[data-variant="inner"] .header-logo-area {
    margin-top: 0;
  }
  .main-header[data-variant="inner"] .header-search {
    flex: 1;
    max-width: 560px;
    margin: 0 16px;
  }
  .main-header[data-variant="inner"] .header-cart-wrap {
    margin-top: 0;
  }

  /* 内页隐藏热词行 */
  .main-header[data-variant="inner"] .header-hot-terms {
    display: none !important;
  }

  /* 内页 Logo 字体缩小 */
  .main-header[data-variant="inner"] .header-logo-mark { width: 28px; height: 28px; }
  .main-header[data-variant="inner"] .header-logo-text { font-size: 20px; }

  /* 内页搜索框：宽度限制 + 高度缩小 */
  .main-header[data-variant="inner"] .header-search-box {
    height: 34px;
    border-width: 1.5px;
  }
  .main-header[data-variant="inner"] .header-search-btn {
    padding: 0 20px;
    font-size: 14px;
  }

  /* 内页购物车：改为纯图标按钮 */
  .main-header[data-variant="inner"] .header-cart {
    width: 40px;
    height: 40px;
    padding: 0;
    border: none;
    background: transparent;
    border-radius: 50%;
    justify-self: center;
  }
  .main-header[data-variant="inner"] .header-cart:hover {
    background: #f5f5f5;
    border-color: transparent;
    color: #e3000b;
  }
  .main-header[data-variant="inner"] .header-cart-text { display: none; }
  .main-header[data-variant="inner"] .header-cart-badge {
    top: -4px;
    right: -4px;
  }
}

/* ─────────────────────────────────────────────
   Desktop mine variant — Logo|我的京东 + 搜索，无购物车
   ───────────────────────────────────────────── */
@media (min-width: 1024px) {
  /* mine 页面 padding-top: 32px topbar + 64px inner header */
  .app-shell--mine {
    padding-top: 96px;
  }

  /* mine Header 高度 64px */
  .main-header[data-variant="mine"] {
    height: 64px;
  }

  /* mine 两列：logo-area(auto) + search(1fr)，无购物车列 */
  .main-header[data-variant="mine"] .main-header-inner {
    align-items: center;
  }
  .main-header[data-variant="mine"] .header-logo-area {
    margin-top: 0;
    display: flex;
    align-items: center;
  }

  /* 普通变体下 logo-area 内的 logo-link 保持 200px 宽体验 */
  .main-header:not([data-variant="mine"]) .header-logo-area {
    width: 100%;
  }

  /* 竖分隔线 */
  .header-mine-divider {
    display: inline-block;
    width: 1px;
    height: 20px;
    background: #ddd;
    margin: 0 16px;
    flex-shrink: 0;
  }

  /* 我的京东 文字 */
  .header-mine-title {
    font-size: 18px;
    font-weight: 400;
    color: #333;
    white-space: nowrap;
  }

  /* mine 搜索框：高度 36px，无热词行 */
  .main-header[data-variant="mine"] .header-search {
    margin: 0 0 0 auto;
    max-width: 520px;
  }
  .main-header[data-variant="mine"] .header-search-box {
    height: 36px;
    border-width: 1.5px;
  }
  .main-header[data-variant="mine"] .header-search-btn {
    padding: 0 20px;
    font-size: 14px;
  }
  .main-header[data-variant="mine"] .header-hot-terms {
    display: none !important;
  }

  /* mine 隐藏购物车列（grid 已无第三列，直接隐藏元素） */
  .main-header[data-variant="mine"] .header-cart {
    display: none;
  }

  /* mine logo 缩小到与 inner 一致 */
  .main-header[data-variant="mine"] .header-logo-mark { width: 28px; height: 28px; }
  .main-header[data-variant="mine"] .header-logo-text { font-size: 20px; }
}

/* ── Page Transitions ── */
.page-enter-active {
  animation: fadeUp 200ms ease both;
}
.page-leave-active {
  position: absolute;
  opacity: 0;
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
