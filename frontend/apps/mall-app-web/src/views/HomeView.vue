<template>
  <div class="home">
    <!-- ====== 搜索栏 ====== -->
    <div class="search-bar-wrap">
      <div class="search-inner">
        <div class="search-box" @click="goSearch">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#aaa" stroke-width="2.2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <span class="search-placeholder">搜索商品、品牌、分类…</span>
          <button class="search-btn" @click.stop="goSearch">搜索</button>
        </div>
        <div class="search-hot-terms">
          <span v-for="term in hotTerms" :key="term" class="hot-term" @click.stop="router.push('/search?keyword='+term)">{{ term }}</span>
        </div>
      </div>
    </div>

    <div class="home-body">
      <!-- ====== 移动端：横向分类滚动条 ====== -->
      <div v-if="deferredSectionsReady" class="mobile-cat-strip">
        <div v-for="cat in categories" :key="cat.name" class="mobile-cat-item" @click="goCategory(cat)">
          <div class="mobile-cat-icon" v-html="cat.icon"></div>
          <span class="mobile-cat-label">{{ cat.name }}</span>
        </div>
      </div>

      <!-- ====== 首行三栏：sidebar + banner + right-panel (CSS Grid) ====== -->
      <div :class="['home-row-top', { 'home-row-top--focus': !deferredSectionsReady }]">
      <!-- ====== 桌面端：左侧分类导航 ====== -->
      <aside v-if="deferredSectionsReady" class="category-sidebar">
        <div class="cat-title">全部分类</div>
        <ul class="cat-list">
          <li v-for="cat in categories" :key="cat.name"
              :class="['cat-item', { active: activeCat === cat.name }]"
              @click="goCategory(cat)">
            <span class="cat-icon" v-html="cat.icon"></span>
            <span class="cat-label">{{ cat.name }}</span>
            <span class="cat-arrow">›</span>
          </li>
        </ul>
      </aside>

    <!-- ====== JD 主内容区（中列） ====== -->
    <div class="jd-center-col">
    <div class="home-container">
      <!-- ====== 轮播 Banner ====== -->
      <div class="swipe-wrap terra-animate">
        <van-swipe
          ref="swipeRef"
          :autoplay="4000"
          lazy-render
          indicator-color="#fff"
          @mousedown.prevent="onSwipeMouseDown"
          @mousemove.prevent="onSwipeMouseMove"
          @mouseup="onSwipeMouseUp"
          @mouseleave="onSwipeMouseUp"
        >
          <van-swipe-item v-for="(slide, i) in slides" :key="slide.id || i" @click="goSlide(slide.link)">
            <div class="slide-inner">
              <img :src="slide.bgPic" class="slide-bg" :alt="slide.name || '首页轮播图'" loading="lazy" decoding="async" @error="onBannerError($event)" />
              <img
                v-if="slide.fgPic"
                :src="slide.fgPic"
                class="slide-fg"
                :alt="`${slide.name || '首页轮播图'} 前景`"
                loading="lazy"
                decoding="async"
                @error="onBannerFgError($event)"
              />
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>
    </div><!-- /.home-container -->

    <!-- ====== banner 下方：紧凑推荐面板（仿京东秒杀/今日推荐） ====== -->
    <div v-if="deferredSectionsReady && hotFillProducts.length" class="hot-fill-section">
      <div class="hot-fill-tabs">
        <span
          v-for="(tab, ti) in hotFillTabs"
          :key="ti"
          :class="['hft-tab', { 'hft-active': hotFillActiveTab === ti }]"
          @click="hotFillActiveTab = ti"
        >
          <svg v-if="ti === 0" width="13" height="13" viewBox="0 0 24 24" fill="#e3000b" style="vertical-align:-1px;margin-right:2px"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          <svg v-if="ti === 1" width="13" height="13" viewBox="0 0 24 24" fill="#e3000b" style="vertical-align:-1px;margin-right:2px"><path d="M12 2C9.5 6 7 8.5 7 12a5 5 0 0010 0c0-1.7-.6-3.2-1.6-4.5C14.5 9 14 10.2 13 11c.3-3-1.5-6-1-9z"/></svg>
          <svg v-if="ti === 2" width="13" height="13" viewBox="0 0 24 24" fill="#e3000b" style="vertical-align:-1px;margin-right:2px"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
          {{ tab }}
        </span>
        <span class="hft-more" @click="router.push('/search')">查看全部 ›</span>
      </div>
      <div class="hot-fill-grid">
        <div
          v-for="p in hotFillProducts"
          :key="p.id"
          class="hot-fill-card"
          @click="goProduct(p.id)"
        >
          <div class="hot-fill-img-wrap">
            <img :src="toThumbUrl(p.pic)" :alt="p.name" loading="lazy" decoding="async" @error="onImageError($event)" />
          </div>
          <p class="hot-fill-name">{{ p.name }}</p>
          <p class="hot-fill-price"><em>¥</em>{{ Number(p.price).toFixed(0) }}</p>
        </div>
      </div>
    </div>
    <!-- 无产品数据时：京东服务保障（参考 global.jd.com 四大承诺） -->
    <div v-else-if="deferredSectionsReady" class="jd-promise-section">
      <div class="promise-header">
        <span class="promise-title">购物保障</span>
      </div>
      <!-- 四大承诺 -->
      <div class="promise-grid">
        <div class="promise-item" @click="router.push('/search')">
          <div class="promise-icon pi-multi">多</div>
          <div class="promise-text">
            <strong>品类齐全</strong>
            <span>轻松购物</span>
          </div>
        </div>
        <div class="promise-item" @click="router.push('/search')">
          <div class="promise-icon pi-fast">快</div>
          <div class="promise-text">
            <strong>极速配送</strong>
            <span>多仓直发</span>
          </div>
        </div>
        <div class="promise-item" @click="router.push('/search')">
          <div class="promise-icon pi-good">好</div>
          <div class="promise-text">
            <strong>正品行货</strong>
            <span>精致服务</span>
          </div>
        </div>
        <div class="promise-item" @click="router.push('/search')">
          <div class="promise-icon pi-save">省</div>
          <div class="promise-text">
            <strong>天天低价</strong>
            <span>畅选无忧</span>
          </div>
        </div>
      </div>
      <!-- 品类速达 -->
      <div class="promise-cat-row">
        <div class="promise-cat-item" @click="router.push('/search?categoryId=61')">
          <div class="pci-icon" style="color:#e3000b">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12" y2="18"/></svg>
          </div>
          <span>手机数码</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=58')">
          <div class="pci-icon" style="color:#1890ff">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
          </div>
          <span>电脑办公</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=63')">
          <div class="pci-icon" style="color:#722ed1">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="4"/><rect x="8" y="2" width="8" height="6" rx="1"/><rect x="8" y="16" width="8" height="6" rx="1"/></svg>
          </div>
          <span>智能穿戴</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=60')">
          <div class="pci-icon" style="color:#13c2c2">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="2" width="16" height="20" rx="2"/><line x1="12" y1="18" x2="12" y2="18"/></svg>
          </div>
          <span>平板</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=65')">
          <div class="pci-icon" style="color:#fa8c16">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="8" rx="2"/><path d="M7 11v6M12 11v6M17 11v6"/></svg>
          </div>
          <span>家用电器</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=59')">
          <div class="pci-icon" style="color:#eb2f96">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.38 3.46L16 2a4 4 0 01-8 0L3.62 3.46a2 2 0 00-1.34 2.23l.58 3.57a1 1 0 00.99.86H6v10c0 1.1.9 2 2 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.86l.58-3.57a2 2 0 00-1.34-2.23z"/></svg>
          </div>
          <span>服装鞋帽</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=62')">
          <div class="pci-icon" style="color:#52c41a">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
          </div>
          <span>运动户外</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=67')">
          <div class="pci-icon" style="color:#f5222d">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8h1a4 4 0 010 8h-1"/><path d="M2 8h16v9a4 4 0 01-4 4H6a4 4 0 01-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/></svg>
          </div>
          <span>食品饮料</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=66')">
          <div class="pci-icon" style="color:#ff4d9e">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
          </div>
          <span>美妆个护</span>
        </div>
        <div class="promise-cat-item" @click="router.push('/search?categoryId=64')">
          <div class="pci-icon" style="color:#1890ff">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
          </div>
          <span>图书文娱</span>
        </div>
      </div>
      <!-- 专享权益 -->
      <div class="promise-rights-row">
        <div class="promise-right-tag prt-genuine">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          正品保证
        </div>
        <div class="promise-right-tag prt-return">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
          30天退换
        </div>
        <div class="promise-right-tag prt-refund">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
          极速退款
        </div>
        <div class="promise-right-tag prt-discount">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
          限时折扣
        </div>
        <div class="promise-right-tag prt-free">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 4v4h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
          免费配送
        </div>
        <div class="promise-right-tag prt-vip">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          会员专享
        </div>
      </div>
      <!-- 活动入口 -->
      <div class="promise-banner-row">
        <div class="pb-card pb-new" @click="router.push('/search?keyword=新品')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
          <span>新品首发</span>
        </div>
        <div class="pb-card pb-hot" @click="router.push('/search')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          <span>超级排行</span>
        </div>
        <div class="pb-card pb-coupon" @click="router.push('/search')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
          <span>领优惠券</span>
        </div>
        <div class="pb-card pb-flash" @click="router.push('/search')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          <span>限时秒杀</span>
        </div>
      </div>
    </div>
    </div><!-- /.jd-center-col -->

    <!-- ====== 右侧信息面板（桌面端） ====== -->
    <aside v-if="deferredSectionsReady" class="jd-right-panel">
      <!-- 欢迎 + 登录/用户信息（响应登录状态） -->
      <div class="jrp-greeting">
        <!-- 未登录 -->
        <template v-if="!userStore.userInfo">
          <div class="jrp-hi">Hi，欢迎来到京东！</div>
          <div class="jrp-btns">
            <button class="jrp-login-btn" @click="router.push('/login')">登录</button>
            <button class="jrp-reg-btn" @click="router.push('/register')">免费注册</button>
          </div>
          <div class="jrp-links">
            <span @click="router.push('/order/list')">我的订单</span>
            <span @click="router.push('/cart')">购物车</span>
            <span @click="router.push('/mine')">我的京东</span>
          </div>
        </template>
        <!-- 已登录 -->
        <template v-else>
          <div class="jrp-user-row">
            <div class="jrp-avatar">
              <img v-if="userStore.avatarSrc" :src="userStore.avatarSrc" class="jrp-avatar-img" @error="($event.target as HTMLImageElement).style.display='none'" />
              <div v-else class="jrp-avatar-ring">
                {{ (userStore.userInfo.nickname || userStore.userInfo.username || '用').charAt(0).toUpperCase() }}
              </div>
            </div>
            <div class="jrp-user-meta">
              <div class="jrp-username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</div>
              <div class="jrp-plus">✦ PLUS会员</div>
            </div>
          </div>
          <div class="jrp-links jrp-links-loggedin">
            <span @click="router.push('/order/list')">我的订单</span>
            <span @click="router.push('/cart')">购物车</span>
            <span @click="router.push('/mine')">我的</span>
          </div>
          <button class="jrp-logout-btn" @click="handlePanelLogout">退出登录</button>
        </template>
      </div>
      <!-- 全球特讯 -->
      <div class="jrp-news">
        <div class="jrp-news-title">全球特讯</div>
        <ul class="jrp-news-list">
          <li @click="goSearch">📦 国际快递追踪升级，查件更快</li>
          <li @click="goSearch">🎁 新人专享：首单立减 30 元</li>
          <li @click="goSearch">📱 京东 App 扫码享专属折扣</li>
          <li @click="goSearch">🌏 海外仓直发，无需等待</li>
        </ul>
      </div>
      <!-- 常用服务 -->
      <div class="jrp-quick-svc">
        <div class="jrp-pay-title">常用服务</div>
        <div class="jrp-svc-grid">
          <div class="svc-item" @click="goSearch">
            <div class="svc-icon-wrap">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="1.8" stroke-linecap="round"><rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 4v4h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
            </div>
            <span class="svc-name">物流服务</span>
          </div>
          <div class="svc-item" @click="goSearch">
            <div class="svc-icon-wrap">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#52c41a" stroke-width="1.8" stroke-linecap="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81 19.79 19.79 0 01.4 1.11 2 2 0 012.18 0h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.91 7.91a16 16 0 006.06 6.06l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 14.92z"/></svg>
            </div>
            <span class="svc-name">客户服务</span>
          </div>
          <div class="svc-item" @click="goSearch">
            <div class="svc-icon-wrap">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fa8c16" stroke-width="1.8" stroke-linecap="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            </div>
            <span class="svc-name">售后政策</span>
          </div>
        </div>
      </div>
      <!-- 联系 -->
      <div class="jrp-service">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2" stroke-linecap="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.7A2 2 0 012.18 0h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.91 7.91a16 16 0 006.06 6.06l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 14.92z"/></svg>
        客服热线：400-606-7733
      </div>
    </aside>

      </div><!-- /.home-row-top -->

      <!-- ====== 频道广场 ====== -->
      <section v-if="deferredSectionsReady" class="channel-plaza terra-animate" style="animation-delay: 40ms">
        <div class="section-head">
          <h3 class="section-title jd-title">频道广场</h3>
          <span class="section-more" @click="goSearch">更多频道</span>
        </div>
        <div class="channel-grid">
          <div class="channel-card" @click="router.push('/search?keyword=新品')">
            <div class="channel-icon" style="background:#fff0f0">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2" stroke-linecap="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
            </div>
            <span class="channel-name">新品首发</span>
          </div>
          <div class="channel-card" @click="router.push('/search?keyword=排行')">
            <div class="channel-icon" style="background:#fff7e6">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#fa8c16" stroke-width="2" stroke-linecap="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            </div>
            <span class="channel-name">超级排行榜</span>
          </div>
          <div class="channel-card" @click="goCategory({keyword:'笔记本', name:'电脑数码'})">
            <div class="channel-icon" style="background:#e6f7ff">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="2" stroke-linecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="2" y1="20" x2="22" y2="20"/></svg>
            </div>
            <span class="channel-name">电脑数码</span>
          </div>
          <div class="channel-card" @click="goCategory({keyword:'服饰', name:'京东服饰'})">
            <div class="channel-icon" style="background:#fff7e6">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#fa8c16" stroke-width="2" stroke-linecap="round"><path d="M20.38 3.46L16 2a4 4 0 01-8 0L3.62 3.46a2 2 0 00-1.34 2.23l.58 3.57a1 1 0 00.99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.84l.58-3.57a2 2 0 00-1.34-2.23z"/></svg>
            </div>
            <span class="channel-name">京东服饰</span>
          </div>
          <div class="channel-card" @click="router.push('/search?keyword=精选')">
            <div class="channel-icon" style="background:#f6ffed">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#52c41a" stroke-width="2" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            </div>
            <span class="channel-name">京东京造</span>
          </div>
          <div class="channel-card" @click="router.push('/search?keyword=特惠')">
            <div class="channel-icon" style="background:#e6f7ff">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            </div>
            <span class="channel-name">京喜自营</span>
          </div>
        </div>
      </section>

      <!-- ====== 即将上架 — JD 商品展示 ====== -->
      <section v-if="deferredSectionsReady" class="section jd-upcoming terra-animate" style="animation-delay: 60ms">
        <div class="section-head">
          <h3 class="section-title jd-title">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2.2" stroke-linecap="round" style="vertical-align:-2px;margin-right:5px"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>即将上架
          </h3>
          <div class="upcoming-meta">
            <span class="countdown-label">开启2h 20m 后</span>
            <span class="section-more upcoming-browse-btn" @click="router.push('/upcoming')">浏览全部</span>
          </div>
        </div>
        <div class="upcoming-grid">
          <div
            v-for="p in upcomingProducts"
            :key="p.id"
            class="upcoming-card"
            @click="goProduct(p.id)"
          >
            <div class="upcoming-img-wrap">
              <img :src="toThumbUrl(p.pic)" class="upcoming-img" loading="lazy" decoding="async" @error="onImageError($event)" />
              <span class="upcoming-badge">预购中</span>
            </div>
            <div class="upcoming-body">
              <div class="upcoming-name">{{ p.name }}</div>
              <div class="upcoming-footer">
                <span class="upcoming-price"><em class="price-unit">¥</em>{{ p.price }}</span>
                <span v-if="p.origPrice && p.origPrice !== p.price" class="upcoming-orig">¥{{ p.origPrice }}</span>
                <span class="upcoming-sold">年售{{ formatSold(p.sold) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ====== 新品上架 — Bento Grid ====== -->
      <section v-if="deferredSectionsReady && content.newProductList?.length" class="section terra-animate" style="animation-delay: 120ms">
        <div class="section-head">
          <h3 class="section-title">新品上架</h3>
          <span class="section-more" @click="goSearch">全部</span>
        </div>
        <div class="bento-grid">
          <div
            v-for="(p, i) in visibleNewProducts"
            :key="p.id"
            :class="['product-card', { 'card-hero': i === 0 }]"
            @click="goProduct(p.id)"
          >
            <div class="card-img-wrap">
              <img :src="toThumbUrl(p.pic)" class="card-img" loading="lazy" decoding="async" @error="onImageError($event)" />
              <span class="card-badge badge-new">NEW</span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ p.name }}</div>
              <div class="card-footer">
                <span class="card-price">¥{{ p.price }}</span>
              </div>
            </div>
          </div>
        </div>
        <div ref="newListSentinel" class="list-sentinel" aria-hidden="true"></div>
      </section>

      <!-- ====== 为你推荐 ====== -->
      <section v-if="deferredSectionsReady && content.hotProductList?.length" class="section terra-animate jd-recommend-section" style="animation-delay: 180ms">
        <div class="section-head">
          <h3 class="section-title jd-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="#e3000b" stroke="none" style="vertical-align:-2px;margin-right:5px"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>为你推荐
          </h3>
          <span class="section-more" @click="goSearch">查看更多</span>
        </div>
        <div class="product-grid jd-recommend-grid">
          <div
            v-for="p in visibleHotProducts"
            :key="p.id"
            class="product-card"
            @click="goProduct(p.id)"
          >
            <div class="card-img-wrap">
              <img :src="toThumbUrl(p.pic)" class="card-img" loading="lazy" decoding="async" @error="onImageError($event)" />
              <span v-if="(p.sale ?? 0) > 50" class="card-badge badge-hot">HOT</span>
            </div>
            <div class="card-body">
              <div class="card-name">{{ p.name }}</div>
              <div class="card-footer">
                <span class="card-price"><em class="price-unit">¥</em>{{ p.price }}</span>
                <span class="card-sale">{{ (p.sale ?? 0) > 0 ? `已售${p.sale}` : '' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div ref="hotListSentinel" class="list-sentinel" aria-hidden="true"></div>
      </section>

      <!-- ====== 底部留白 ====== -->
      <div class="home-footer">
        <span>— 到底了 —</span>
      </div>

    </div><!-- /.home-body -->
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Swipe as VanSwipe, SwipeItem as VanSwipeItem, showToast } from 'vant'
import type { SwipeInstance } from 'vant'
import { homeApi } from '@mall/api-sdk/app'
import type { HomeContent } from '@mall/api-sdk/app/home'
import { useUserStore } from '@/stores/user'
import { cancelWarmup, warmupImages } from '@/utils/imageWarmup'

const router = useRouter()
const userStore = useUserStore()
const content = ref<Partial<HomeContent>>({})
const deferredSectionsReady = ref(false)
const hotFillProducts = ref<Array<{ id: number; name: string; pic: string; price: number }>>([])
const upcomingProducts = ref<Array<{ id: number; name: string; pic: string; price: string; origPrice: string; sold: number }>>([])
const hotFillTabs = ['秒杀', '热卖榜', '品质优选']
const hotFillActiveTab = ref(0)
const activeCat = ref('')
const newListSentinel = ref<HTMLElement | null>(null)
const hotListSentinel = ref<HTMLElement | null>(null)
const newRenderCount = ref(12)
const hotRenderCount = ref(12)
const INITIAL_RENDER_COUNT = 12
const RENDER_STEP = 12
let sectionObserver: IntersectionObserver | null = null
const HOME_WARMUP_SCOPE = 'home-view'

interface HomeSlide {
  id: number
  name: string
  bgPic: string
  fgPic?: string
  mode: 'bg-only' | 'overlay'
  link: string
  sort: number
}

function parseCarouselNote(note?: string | null): { mode: 'bg-only' | 'overlay'; fgPic: string } {
  if (!note) {
    return { mode: 'bg-only', fgPic: '' }
  }
  const fields = note
    .split(';')
    .map((item) => item.trim())
    .filter(Boolean)
  let mode: 'bg-only' | 'overlay' = 'bg-only'
  let fgPic = ''
  for (const item of fields) {
    const index = item.indexOf('=')
    if (index <= 0) continue
    const key = item.slice(0, index).trim().toLowerCase()
    const value = item.slice(index + 1).trim()
    if (!value) continue
    if (key === 'mode' && value.toLowerCase() === 'overlay') {
      mode = 'overlay'
      continue
    }
    if (key === 'fg') {
      fgPic = normalizeImageUrl(value)
    }
  }
  if (mode === 'overlay' && !fgPic) {
    return { mode: 'bg-only', fgPic: '' }
  }
  return { mode, fgPic }
}

const slides = computed<HomeSlide[]>(() => {
  const advertiseList = content.value.advertiseList ?? []
  return advertiseList
    .map((item) => {
      const id = Number(item.id ?? 0)
      const metadata = parseCarouselNote(item.note)
      const bgPic = normalizeImageUrl(item.pic)
      return {
        id: Number.isFinite(id) ? id : 0,
        name: item.name ?? '',
        bgPic,
        fgPic: metadata.fgPic,
        mode: metadata.mode,
        link: item.url ?? '',
        sort: Number(item.sort ?? 0),
      }
    })
    .filter((item) => Boolean(item.bgPic))
    .sort((a, b) => a.sort - b.sort)
})

const visibleNewProducts = computed(() => {
  const list = content.value.newProductList ?? []
  return list.slice(0, newRenderCount.value)
})

const visibleHotProducts = computed(() => {
  const list = content.value.hotProductList ?? []
  return list.slice(0, hotRenderCount.value)
})

// ── 轮播鼠标拖拽 ──
const swipeRef = ref<SwipeInstance | null>(null)
let _swipeDragX = 0
let _swipeDragging = false

function onSwipeMouseDown(e: MouseEvent) {
  _swipeDragging = true
  _swipeDragX = e.clientX
}
function onSwipeMouseMove(_e: MouseEvent) {
  // 仅阻止默认，防止拖选文字
}
function onSwipeMouseUp(e: MouseEvent) {
  if (!_swipeDragging) return
  _swipeDragging = false
  const delta = e.clientX - _swipeDragX
  if (Math.abs(delta) > 50) {
    if (delta < 0) swipeRef.value?.next()
    else swipeRef.value?.prev()
  }
}

// ── 分类数据（图标 & 分类ID & 搜索关键字备用）──
// categoryId 对应 pms_product_category.id，跳转时使用 categoryId 精确过滤
const categories = [
  { name: '手机数码', keyword: '手机', categoryId: 61, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="1.8" stroke-linecap="round"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12" y2="18.01"/></svg>' },
  { name: '电脑办公', keyword: '笔记本', categoryId: 58, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="1.8" stroke-linecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="2" y1="20" x2="22" y2="20"/></svg>' },
  { name: '智能穿戴', keyword: '智能穿戴', categoryId: 63, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#722ed1" stroke-width="1.8" stroke-linecap="round"><rect x="7" y="4" width="10" height="16" rx="4"/><line x1="9" y1="4" x2="6" y2="2"/><line x1="15" y1="4" x2="18" y2="2"/><line x1="9" y1="20" x2="6" y2="22"/><line x1="15" y1="20" x2="18" y2="22"/></svg>' },
  { name: '平板', keyword: '平板', categoryId: 60, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#13c2c2" stroke-width="1.8" stroke-linecap="round"><rect x="4" y="2" width="16" height="20" rx="2"/><line x1="12" y1="18" x2="12" y2="18.01"/></svg>' },
  { name: '家用电器', keyword: '家用电器', categoryId: 65, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fa8c16" stroke-width="1.8" stroke-linecap="round"><rect x="2" y="3" width="20" height="8" rx="2"/><path d="M7 11v10M12 11v10M17 11v10"/></svg>' },
  { name: '服装鞋帽', keyword: '服饰', categoryId: 59, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#eb2f96" stroke-width="1.8" stroke-linecap="round"><path d="M20.38 3.46L16 2a4 4 0 01-8 0L3.62 3.46a2 2 0 00-1.34 2.23l.58 3.57a1 1 0 00.99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.84l.58-3.57a2 2 0 00-1.34-2.23z"/></svg>' },
  { name: '运动户外', keyword: '运动鞋', categoryId: 62, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#52c41a" stroke-width="1.8" stroke-linecap="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>' },
  { name: '食品饮料', keyword: '食品', categoryId: 67, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f5222d" stroke-width="1.8" stroke-linecap="round"><path d="M18 8h1a4 4 0 010 8h-1"/><path d="M2 8h16v9a4 4 0 01-4 4H6a4 4 0 01-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/></svg>' },
  { name: '美妆个护', keyword: '美妆', categoryId: 66, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ff4d9e" stroke-width="1.8" stroke-linecap="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>' },
  { name: '图书文娱', keyword: '图书', categoryId: 64, icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="1.8" stroke-linecap="round"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>' },
]

function goSearch() {
  router.push('/search')
}

function goCategory(cat: { keyword: string; name: string; categoryId?: number }) {
  activeCat.value = cat.name
  if (cat.categoryId) {
    // 按分类ID精确过滤，不附带keyword避免ES关键字过滤干扰
    router.push(`/search?categoryId=${cat.categoryId}`)
  } else {
    router.push(`/search?keyword=${encodeURIComponent(cat.keyword)}`)
  }
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

function goSlide(link?: string) {
  if (!link) return
  if (link.startsWith('/')) {
    router.push(link)
    return
  }
}

// ── 图片 URL 标准化：仅内部 API 路径可用，禁止本地文件与外链 ──
function normalizeImageUrl(url?: string | null): string {
  if (!url) return ''
  const clean = url.trim()
  if (clean.startsWith('/api/asset/image/')) return clean
  if (clean.startsWith('data:image/')) return clean
  return ''
}

function toThumbUrl(url?: string | null): string {
  const normalized = normalizeImageUrl(url)
  if (!normalized || !normalized.startsWith('/api/asset/image/')) {
    return normalized
  }
  const [path, query = ''] = normalized.split('?', 2)
  const params = new URLSearchParams(query)
  params.set('variant', 'thumb')
  const serialized = params.toString()
  return serialized ? `${path}?${serialized}` : path
}

const placeholderSvg = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" fill="%23f2ede6"><rect width="400" height="400"/><text x="200" y="200" text-anchor="middle" dominant-baseline="central" font-size="13" fill="%23a39890" font-family="sans-serif">暂无图片</text></svg>'
)

function onImageError(e: Event) {
  (e.target as HTMLImageElement).src = placeholderSvg
}

function onBannerError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = 'data:image/svg+xml,' + encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="400" fill="%23efe9e0"><rect width="1200" height="400"/><text x="600" y="200" text-anchor="middle" dominant-baseline="central" font-size="16" fill="%23a39890" font-family="sans-serif">Banner</text></svg>'
  )
}

function onBannerFgError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

function formatSold(sold: number): string {
  const n = Number(sold) || 0
  if (n >= 1000000) return Math.floor(n / 1000000) + '00万+'
  if (n >= 100000) return Math.floor(n / 10000) + '万+'
  if (n >= 10000) return Math.floor(n / 1000) / 10 + 'w+'
  return n.toString()
}

function handlePanelLogout() {
  userStore.logout()
  showToast('已退出登录')
}

// ── 搜索热搜词 ──
const hotTerms = ['平板电脑', '爆款耳机', '手机', '女装', '游戏本', '运动鞋']

function syncHotFillProducts(payload?: Partial<HomeContent>) {
  const hotList = payload?.hotProductList ?? []
  const newList = payload?.newProductList ?? []
  const source = hotList.length ? hotList : newList
  hotFillProducts.value = source
    .slice(0, 10)
    .map((item) => ({
      id: Number(item.id),
      name: item.name ?? '',
      pic: item.pic ?? '',
      price: Number(item.price ?? 0),
    }))
    .filter((item) => Number.isFinite(item.id) && item.id > 0)
}

function toNumber(value: unknown, fallback = 0): number {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

function toUpcomingCard(item: Record<string, unknown>) {
  const id = toNumber(item.id, NaN)
  if (!Number.isFinite(id)) return null

  const pic = normalizeImageUrl(String(item.pic ?? '').trim())
  if (!pic) return null

  const price = toNumber(item.price, 0)
  const originalPrice = toNumber(item.originalPrice ?? item.price, price)

  return {
    id,
    name: String(item.name ?? '未命名商品'),
    pic,
    price: `${price}`,
    origPrice: `${originalPrice > 0 ? originalPrice : price}`,
    sold: toNumber(item.sale, 0),
  }
}

function syncUpcomingProducts(source: Array<Record<string, unknown>>) {
  upcomingProducts.value = source
    .map((item) => toUpcomingCard(item))
    .filter((item): item is { id: number; name: string; pic: string; price: string; origPrice: string; sold: number } => item != null)
    .slice(0, 12)
}

function updateRenderWindows(payload: Partial<HomeContent>) {
  const newTotal = payload.newProductList?.length ?? 0
  const hotTotal = payload.hotProductList?.length ?? 0
  newRenderCount.value = Math.min(INITIAL_RENDER_COUNT, newTotal)
  hotRenderCount.value = Math.min(INITIAL_RENDER_COUNT, hotTotal)
}

function warmupLiteImages(payload: Partial<HomeContent>) {
  const liteBannerUrls = (payload.advertiseList ?? [])
    .slice(0, 3)
    .flatMap((item) => {
      const urls: string[] = [normalizeImageUrl(item.pic)]
      const parsed = parseCarouselNote(item.note)
      if (parsed.fgPic) urls.push(parsed.fgPic)
      return urls
    })
  warmupImages(liteBannerUrls, {
    scopeId: HOME_WARMUP_SCOPE,
    priority: 'immediate',
    maxItems: 6,
  })
}

function warmupDeferredProductImages(payload: Partial<HomeContent>) {
  const ordered = [
    ...(payload.newProductList ?? []),
    ...(payload.hotProductList ?? []),
  ]
    .map((item) => toThumbUrl(item.pic))
    .filter(Boolean)

  warmupImages(ordered.slice(0, 8), {
    scopeId: HOME_WARMUP_SCOPE,
    priority: 'immediate',
    maxItems: 8,
  })
  warmupImages(ordered.slice(8, 20), {
    scopeId: HOME_WARMUP_SCOPE,
    priority: 'idle',
    maxItems: 12,
  })
}

function expandRenderWindow(target: 'new' | 'hot') {
  if (typeof window !== 'undefined' && 'requestIdleCallback' in window) {
    const idle = window as typeof window & {
      requestIdleCallback: (cb: IdleRequestCallback, options?: IdleRequestOptions) => number
    }
    idle.requestIdleCallback(() => doExpandRenderWindow(target), { timeout: 300 })
  } else {
    setTimeout(() => doExpandRenderWindow(target), 16)
  }
}

function doExpandRenderWindow(target: 'new' | 'hot') {
  if (target === 'new') {
    const total = content.value.newProductList?.length ?? 0
    newRenderCount.value = Math.min(total, newRenderCount.value + RENDER_STEP)
    return
  }
  const total = content.value.hotProductList?.length ?? 0
  hotRenderCount.value = Math.min(total, hotRenderCount.value + RENDER_STEP)
}

function bindSectionObserver() {
  if (typeof window === 'undefined' || typeof IntersectionObserver === 'undefined') {
    return
  }
  sectionObserver?.disconnect()
  sectionObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue
      if (entry.target === newListSentinel.value) {
        expandRenderWindow('new')
      } else if (entry.target === hotListSentinel.value) {
        expandRenderWindow('hot')
      }
    }
  }, { root: null, rootMargin: '480px 0px', threshold: 0 })

  if (newListSentinel.value) sectionObserver.observe(newListSentinel.value)
  if (hotListSentinel.value) sectionObserver.observe(hotListSentinel.value)
}

async function loadHomeContent() {
  try {
    const { data } = await homeApi.content()
    const payload = (data?.data ?? {}) as Partial<HomeContent>
    content.value = payload
    syncHotFillProducts(payload)
    updateRenderWindows(payload)
    warmupLiteImages(payload)
    warmupDeferredProductImages(payload)

    const homeSource = [
      ...(payload.newProductList ?? []),
      ...(payload.hotProductList ?? []),
    ] as unknown as Array<Record<string, unknown>>
    if (homeSource.length > 0) {
      syncUpcomingProducts(homeSource)
    }
  } finally {
    deferredSectionsReady.value = true
    await nextTick()
    bindSectionObserver()
  }
}

onMounted(() => {
  // 用户信息与首页数据互不依赖，避免串行等待放大首屏耗时。
  void userStore.fetchUser()
  void loadHomeContent()
})

onBeforeUnmount(() => {
  sectionObserver?.disconnect()
  sectionObserver = null
  cancelWarmup(HOME_WARMUP_SCOPE)
})
</script>

<style scoped>
/* ============================================================
   强网格布局系统 — CSS Grid + CSS Variables
   所有列宽、间距使用统一变量，保证 header/body/sections 对齐
   ============================================================ */

/* ── CSS Grid 变量 ── */
.home {
  --grid-sidebar-w: 200px;
  --grid-panel-w: 190px;
  --grid-gap: 16px;
  --grid-padding: 16px;
  --grid-max-w: var(--app-content-max-width, 1200px);

  min-height: 100vh;
  background: var(--t-bg, #f4f4f4);
}

/* ── 搜索框（已移入全局 MainHeader）── */
.search-bar-wrap { display: none; }
.search-hot-terms {
  display: flex; gap: 0; flex-wrap: wrap; margin-top: 4px; padding-left: 2px;
}
.hot-term {
  font-size: 12px; color: #888; cursor: pointer;
  padding: 2px 10px 2px 0; line-height: 1.8;
  transition: color 150ms; white-space: nowrap;
}
.hot-term:not(:last-child)::after { content: ' |'; color: #ddd; margin-left: 10px; }
.hot-term:hover { color: #e3000b; }
.search-inner {
  width: min(calc(100% - 0px), var(--grid-max-w));
  margin: 0 auto; padding: 0 var(--grid-padding); box-sizing: border-box;
}
.search-box {
  display: flex; align-items: center; background: #fff;
  border: 1.5px solid #e0e0e0; border-radius: 6px;
  padding: 0 6px 0 14px; height: 40px; cursor: pointer;
  transition: border-color 180ms, box-shadow 180ms;
}
.search-box:hover { border-color: #e3000b; box-shadow: 0 0 0 3px rgba(227,0,11,.07); }
.search-icon { flex-shrink: 0; margin-right: 8px; color: #bbb; }
.search-placeholder {
  flex: 1; color: #aaa; font-size: 14px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; user-select: none;
}
.search-btn {
  flex-shrink: 0; background: #e3000b; color: #fff; border: none;
  border-radius: 4px; padding: 0 18px; height: 30px;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: background 150ms;
}
.search-btn:hover { background: #c00; }

/* ═══════════════════════════════════════════
   Body 容器 — 与 header 共享 max-width + padding
   ═══════════════════════════════════════════ */
.home-body {
  display: block;
  width: 100%;
  max-width: var(--grid-max-w);
  margin: 0 auto;
  padding: 0 var(--grid-padding);
  box-sizing: border-box;
}

/* ═══════════════════════════════════════════
   首行三栏 — 移动端 block，桌面端 CSS Grid
   ═══════════════════════════════════════════ */
.home-row-top { display: block; }

/* ── 左侧分类侧边栏（桌面端） ── */
.category-sidebar { display: none; }
.cat-title {
  font-size: 13px; font-weight: 700; color: #333;
  background: #f8f8f8; padding: 10px 16px;
  letter-spacing: 0.04em; border-bottom: 1px solid #eee;
  border-left: 3px solid #e3000b;
}
.cat-list { list-style: none; margin: 0; padding: 0; }
.cat-item {
  display: flex; align-items: center; gap: 10px;
  padding: 11px 14px; cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 150ms; background: #fff;
  border-bottom: 1px solid #f5f5f5;
}
.cat-item:hover, .cat-item.active {
  background: #fff5f5; border-left-color: #e3000b; color: #e3000b;
}
.cat-icon { flex-shrink: 0; display: flex; align-items: center; }
.cat-label { flex: 1; font-size: 13px; font-weight: 500; color: inherit; }
.cat-arrow { color: #ccc; font-size: 16px; line-height: 1; }
.cat-item:hover .cat-arrow, .cat-item.active .cat-arrow { color: #e3000b; }

/* ── 移动端横向分类条 ── */
.mobile-cat-strip {
  display: flex; gap: 0; overflow-x: auto;
  background: #fff; padding: 10px 8px;
  -webkit-overflow-scrolling: touch; scrollbar-width: none;
  border-bottom: 1px solid #eee;
}
.mobile-cat-strip::-webkit-scrollbar { display: none; }
.mobile-cat-item {
  flex-shrink: 0; display: flex; flex-direction: column;
  align-items: center; gap: 5px; padding: 4px 10px;
  cursor: pointer; min-width: 62px;
}
.mobile-cat-item:active { background: #fff5f5; border-radius: 8px; }
.mobile-cat-icon {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 50%; background: #fff5f5;
}
.mobile-cat-label { font-size: 11px; color: #555; text-align: center; white-space: nowrap; }

/* ── 中列容器 ── */
.jd-center-col { display: flex; flex-direction: column; min-width: 0; }
.home-container { padding: 0; box-sizing: border-box; }

/* ── Banner / Swipe ── */
.swipe-wrap {
  overflow: hidden;
  box-shadow: var(--t-shadow-md, 0 2px 8px rgba(0,0,0,.1));
  aspect-ratio: 16 / 5;
  cursor: grab; user-select: none;
}
.swipe-wrap:active { cursor: grabbing; }

/* ── 紧凑推荐面板（仿京东秒杀）— 移动端隐藏 ── */
.hot-fill-section {
  display: none;
  flex-direction: column; background: #fff;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
  overflow: hidden;
}
.hot-fill-tabs {
  display: flex; align-items: center; gap: 0;
  border-bottom: 1px solid #f0f0f0; padding: 0 12px; background: #fff;
}
.hft-tab {
  display: inline-flex; align-items: center;
  padding: 8px 16px; font-size: 13px; font-weight: 600;
  color: #666; cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 150ms, border-color 150ms; white-space: nowrap;
}
.hft-tab:hover { color: #e3000b; }
.hft-active { color: #e3000b; border-bottom-color: #e3000b; }
.hft-more {
  margin-left: auto; font-size: 12px; color: #999;
  cursor: pointer; transition: color 150ms;
}
.hft-more:hover { color: #e3000b; }
.hot-fill-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 0; flex: 1;
}
.hot-fill-card {
  display: flex; flex-direction: column; align-items: center;
  padding: 10px 8px 8px; cursor: pointer;
  transition: background 150ms;
  border-right: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5; text-align: center;
}
.hot-fill-card:nth-child(5n) { border-right: none; }
.hot-fill-card:nth-last-child(-n+5) { border-bottom: none; }
.hot-fill-card:hover { background: #fff8f8; }
.hot-fill-img-wrap {
  width: 80px; height: 80px; overflow: hidden;
  border-radius: 4px; margin-bottom: 6px; flex-shrink: 0;
}
.hot-fill-img-wrap img { width: 100%; height: 100%; object-fit: contain; }
.hot-fill-name {
  font-size: 11px; color: #333; line-height: 1.3; margin: 0 0 3px;
  display: -webkit-box; -webkit-line-clamp: 1;
  -webkit-box-orient: vertical; overflow: hidden; max-width: 100%;
}
.hot-fill-price { font-size: 14px; font-weight: 700; color: #e3000b; margin: 0; }
.hot-fill-price em { font-style: normal; font-size: 11px; }

/* ── 服务保障降级区 — 移动端隐藏 ── */
.jd-promise-section {
  display: none;
  background: #fff; border-radius: 8px;
  padding: 12px 12px 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
  flex-direction: column; justify-content: space-between; gap: 0;
}
.promise-header { display: flex; align-items: center; }
.promise-title { font-size: 14px; font-weight: 700; color: #111; }
.promise-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.promise-item {
  display: flex; align-items: center; gap: 8px;
  background: #fafafa; border-radius: 6px; padding: 8px 10px;
  cursor: pointer; transition: box-shadow 150ms;
}
.promise-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,.1); }
.promise-icon {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 900; flex-shrink: 0;
}
.pi-multi { background: #fff0f0; color: #e3000b; }
.pi-fast  { background: #fff7e6; color: #fa8c16; }
.pi-good  { background: #f0fff0; color: #52c41a; }
.pi-save  { background: #eff8ff; color: #1890ff; }
.promise-text { display: flex; flex-direction: column; gap: 1px; }
.promise-text strong { font-size: 12px; font-weight: 700; color: #222; line-height: 1.3; }
.promise-text span { font-size: 11px; color: #999; line-height: 1.3; }
.promise-banner-row { display: flex; gap: 8px; }
.pb-card {
  flex: 1; display: flex; align-items: center; justify-content: center;
  gap: 6px; padding: 8px 0; border-radius: 6px;
  font-size: 12px; font-weight: 600; cursor: pointer; transition: opacity 150ms;
}
.pb-card:hover { opacity: 0.85; }
.pb-new    { background: #fff0f0; color: #e3000b; }
.pb-hot    { background: #fff7e6; color: #fa8c16; }
.pb-coupon { background: #eff8ff; color: #1890ff; }
.pb-flash  { background: #f6ffed; color: #389e0d; }
.promise-cat-row { display: flex; gap: 0; align-items: stretch; }
.promise-cat-item {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 3px;
  padding: 6px 2px; border-radius: 6px; cursor: pointer;
  font-size: 11px; color: #444; transition: background 150ms;
}
.promise-cat-item:hover { background: #f5f5f5; }
.pci-icon {
  width: 28px; height: 28px; border-radius: 50%; background: #f0f2f5;
  display: flex; align-items: center; justify-content: center;
}
.promise-rights-row { display: flex; gap: 6px; flex-wrap: nowrap; }
.promise-right-tag {
  flex: 1; display: flex; align-items: center; justify-content: center;
  gap: 3px; padding: 5px 4px; border-radius: 6px;
  font-size: 11px; font-weight: 500; white-space: nowrap;
  cursor: pointer; transition: opacity 150ms;
}
.promise-right-tag:hover { opacity: 0.8; }
.prt-genuine  { background: #fff2e8; color: #d4380d; }
.prt-return   { background: #e6f7ff; color: #096dd9; }
.prt-refund   { background: #f6ffed; color: #389e0d; }
.prt-discount { background: #fff0f6; color: #c41d7f; }
.prt-free     { background: #e8f5e9; color: #2e7d32; }
.prt-vip      { background: #fffbe6; color: #ad6800; }

:deep(.van-swipe),
:deep(.van-swipe-item) { width: 100%; height: 100%; }
.slide-inner {
  position: relative; width: 100%; height: 100%;
  overflow: hidden;
}
.slide-bg {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.slide-fg {
  position: absolute; inset: 0; z-index: 2; width: 100%; height: 100%;
  object-fit: cover; object-position: center; pointer-events: none;
}
.slide-text-overlay {
  position: absolute; top: 50%; left: 6%; transform: translateY(-50%);
  display: flex; flex-direction: column; gap: 8px; pointer-events: none;
}
.slide-badge {
  display: inline-block; width: fit-content;
  font-size: 11px; font-weight: 600; color: rgba(255,255,255,0.95);
  background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.45);
  border-radius: 20px; padding: 2px 12px; letter-spacing: 0.5px;
  backdrop-filter: blur(6px);
}
.slide-title {
  margin: 0; font-size: clamp(22px, 3.5vw, 34px); font-weight: 900;
  color: #fff; text-shadow: 0 2px 14px rgba(0,0,0,0.3);
  letter-spacing: -0.02em; line-height: 1.1;
}
.slide-sub {
  margin: 0; font-size: clamp(12px, 1.4vw, 15px);
  color: rgba(255,255,255,0.88); letter-spacing: 0.06em;
  text-shadow: 0 1px 6px rgba(0,0,0,0.2);
}
.slide-cta-btn {
  align-self: flex-start; margin-top: 4px; padding: 8px 20px;
  background: #e3000b; color: #fff; border: none; border-radius: 4px;
  font-size: 13px; font-weight: 700; cursor: pointer;
  letter-spacing: 0.04em;
  transition: background 150ms, transform 150ms;
  box-shadow: 0 2px 8px rgba(227,0,11,.3);
}
.slide-cta-btn:hover { background: #c00; transform: translateY(-1px); }

/* ═══════════════════════════════════════════
   Section 通用样式
   ═══════════════════════════════════════════ */
.section {
  margin-top: var(--grid-gap);
  background: #fff;
  padding: 14px 14px 16px;
  /* 性能：折叠颛WW内部子元素不影响外层排版 */
  contain: layout style;
}
.section-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 14px; border-left: 4px solid #e3000b; padding-left: 10px;
}
.section-title {
  margin: 0; color: #222; font-size: 16px; font-weight: 800;
  letter-spacing: -0.01em;
}
.section-more {
  color: #aaa; font-size: 12px; cursor: pointer;
  padding: 3px 0; transition: color 180ms;
}
.section-more:hover { color: #e3000b; }
.upcoming-browse-btn {
  color: #e3000b !important; border-color: #e3000b !important; font-weight: 600;
}
.upcoming-browse-btn:hover { background: #e3000b !important; color: #fff !important; }

/* ── Bento Grid ── */
.bento-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.bento-grid .card-hero { grid-column: 1 / -1; }
.bento-grid .card-hero .card-img-wrap { aspect-ratio: 21 / 9; }
.bento-grid .card-hero .card-name { font-size: 15px; -webkit-line-clamp: 1; }
.bento-grid .card-hero .card-price { font-size: 18px; }

/* ── Standard Product Grid ── */
.product-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.list-sentinel { width: 100%; height: 1px; }

/* ── Product Card ── */
.product-card {
  overflow: hidden; border-radius: 10px; background: #fff;
  cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,.06);
  transition: transform 180ms, box-shadow 180ms;
  border: 1px solid #f0f0f0;
  /* 性能：限制重排/重绘影响范围 */
  contain: layout style;
}
.product-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,.10); }
.product-card:active { transform: scale(0.97); }
.card-img-wrap {
  position: relative; overflow: hidden; background: #f5f5f5; aspect-ratio: 1;
  /* 性能：图片包装区独立的绘制层 */
  contain: layout paint;
}
.card-img {
  display: block; width: 100%; height: 100%; object-fit: cover;
  transition: transform 350ms ease;
  /* 提示浏览器图片会做 transform，提前提升到合成层 */
  will-change: transform;
}
.product-card:hover .card-img { transform: scale(1.05); }
.card-badge {
  position: absolute; top: 6px; left: 6px; padding: 2px 7px;
  font-size: 10px; font-weight: 700; letter-spacing: 0.06em;
  border-radius: 20px; line-height: 1.6;
}
.badge-new { background: #f0f5ff; color: #1890ff; }
.badge-hot { background: #fff1f0; color: #e3000b; }
.card-body { padding: 8px 10px 10px; }
.card-name {
  overflow: hidden; display: -webkit-box; -webkit-box-orient: vertical;
  -webkit-line-clamp: 2; line-height: 1.45; color: #333;
  font-size: 12px; font-weight: 500;
}
.card-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 6px; }
.card-price { color: #e3000b; font-size: 15px; font-weight: 800; font-variant-numeric: tabular-nums; }
.card-sale { color: #999; font-size: 11px; }

/* ── Footer ── */
.home-footer {
  padding: 28px 0 20px; text-align: center;
  color: #bbb; font-size: 12px; background: #fff;
  margin-top: var(--grid-gap);
}

/* ── 即将上架 ── */
.upcoming-meta { display: flex; align-items: center; gap: 10px; }
.countdown-label {
  font-size: 12px; color: #e3000b; background: #fff0f0;
  border: 1px solid #ffa8a8; border-radius: 20px;
  padding: 2px 10px; font-weight: 600;
}
.upcoming-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.upcoming-card {
  overflow: hidden; border-radius: 10px; background: #fff;
  cursor: pointer; border: 1px solid #f0f0f0;
  transition: transform 180ms, box-shadow 180ms;
  /* 性能：限制卡片重排/重绘影响范围 */
  contain: layout style;
}
.upcoming-card:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,.10); }
.upcoming-img-wrap {
  position: relative; background: #f8f8f8; aspect-ratio: 1; overflow: hidden;
  contain: layout paint;
}
.upcoming-img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform 350ms ease;
  will-change: transform;
}
.upcoming-card:hover .upcoming-img { transform: scale(1.06); }
.upcoming-badge {
  position: absolute; top: 6px; right: 6px;
  background: linear-gradient(135deg, #e3000b, #ff6b35);
  color: #fff; font-size: 10px; font-weight: 700;
  letter-spacing: 0.04em; padding: 2px 7px; border-radius: 2px;
}
.upcoming-body { padding: 8px 10px 10px; }
.upcoming-name {
  overflow: hidden; display: -webkit-box; -webkit-box-orient: vertical;
  -webkit-line-clamp: 2; line-height: 1.45; color: #333;
  font-size: 12px; font-weight: 500; min-height: 2.9em;
}
.upcoming-footer {
  display: flex; align-items: baseline; gap: 5px; margin-top: 6px; flex-wrap: wrap;
}
.upcoming-price { color: #e3000b; font-size: 15px; font-weight: 800; }
.upcoming-orig { color: #bbb; font-size: 11px; text-decoration: line-through; }
.upcoming-sold { color: #999; font-size: 11px; margin-left: auto; }

/* ── terra-animate ── */
.terra-animate { animation: terraFadeUp 0.4s ease both; }
@keyframes terraFadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ═══════════════════════════════════════════
   JD 频道广场 — 与三栏共享同一容器宽度
   ═══════════════════════════════════════════ */
.channel-plaza {
  background: #fff; margin-top: var(--grid-gap);
  padding: 14px 14px 16px;
}
.channel-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.channel-card {
  display: flex; flex-direction: column; align-items: center;
  gap: 8px; padding: 14px 8px; border-radius: 10px;
  background: #fafafa; border: 1px solid #f0f0f0;
  cursor: pointer; transition: all 200ms;
}
.channel-card:hover {
  border-color: #e8c8c8; background: #fffafa;
  transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.07);
}
.channel-icon {
  width: 52px; height: 52px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.channel-name { font-size: 12px; font-weight: 600; color: #333; text-align: center; }

/* ── JD 标题样式 ── */
.jd-title {
  color: #1a1a1a !important; font-size: 16px !important;
  display: flex; align-items: center;
}
.price-unit { font-style: normal; font-size: 12px; font-weight: 700; }

/* ═══════════════════════════════════════════
   右侧信息面板 — 移动端隐藏
   ═══════════════════════════════════════════ */
.jd-right-panel { display: none; }
.jrp-greeting {
  background: #fff; border-bottom: 1px solid #f0f0f0; padding: 14px;
}
.jrp-hi { font-size: 13px; color: #333; font-weight: 600; margin-bottom: 10px; }
.jrp-btns { display: flex; gap: 8px; margin-bottom: 10px; }
.jrp-login-btn {
  flex: 1; height: 32px; background: #e3000b; color: #fff;
  border: none; border-radius: 4px; font-size: 13px;
  font-weight: 600; cursor: pointer; transition: background 150ms;
}
.jrp-login-btn:hover { background: #c00; }
.jrp-reg-btn {
  flex: 1; height: 32px; background: #fff; color: #555;
  border: 1px solid #ddd; border-radius: 4px; font-size: 13px;
  cursor: pointer; transition: all 150ms;
}
.jrp-reg-btn:hover { border-color: #e3000b; color: #e3000b; background: #fff5f5; }
.jrp-links { display: flex; gap: 0; justify-content: space-between; }
.jrp-links span {
  font-size: 12px; color: #666; cursor: pointer; padding: 2px 0;
  border-bottom: 1px dashed transparent; transition: all 150ms;
}
.jrp-links span:hover { color: #e3000b; border-bottom-color: #e3000b; }
.jrp-links-loggedin { margin-top: 6px; }
.jrp-user-row {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 10px; padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}
.jrp-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  overflow: hidden; flex-shrink: 0; background: #e3000b;
  border: 2px solid #fff; box-shadow: 0 2px 8px rgba(227,0,11,.25);
}
.jrp-avatar-img { width: 100%; height: 100%; object-fit: cover; }
.jrp-avatar-ring {
  width: 100%; height: 100%; display: flex;
  align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; color: #fff;
}
.jrp-user-meta { min-width: 0; }
.jrp-username {
  font-size: 14px; font-weight: 700; color: #111;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 116px;
}
.jrp-plus {
  font-size: 11px; color: #b8720a;
  background: linear-gradient(90deg, #fff8e6, #fff0c0);
  border: 1px solid #f3c46a; border-radius: 20px;
  padding: 1px 7px; display: inline-block;
  margin-top: 3px; white-space: nowrap;
}
.jrp-logout-btn {
  width: 100%; margin-top: 8px; padding: 6px 0;
  background: transparent; border: 1px solid #ddd;
  color: #888; border-radius: 4px; font-size: 12px;
  cursor: pointer; transition: all 150ms;
}
.jrp-logout-btn:hover {
  border-color: #e3000b; color: #e3000b; background: #fff5f5;
}
.jrp-news {
  background: #fff; margin-top: 8px; padding: 12px 14px;
  border-bottom: 1px solid #f0f0f0;
  flex: 1; overflow-y: auto;
}
.jrp-news-title {
  font-size: 13px; font-weight: 700; color: #222;
  margin-bottom: 10px; padding-left: 8px; border-left: 3px solid #e3000b;
}
.jrp-news-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.jrp-news-list li {
  font-size: 12px; color: #555; cursor: pointer; line-height: 1.5;
  padding-bottom: 8px; border-bottom: 1px dotted #eee;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  transition: color 150ms;
}
.jrp-news-list li:last-child { border-bottom: none; padding-bottom: 0; }
.jrp-news-list li:hover { color: #e3000b; }
.jrp-pay-title {
  font-size: 13px; font-weight: 700; color: #222;
  margin-bottom: 10px; padding-left: 8px; border-left: 3px solid #e3000b;
}
.jrp-quick-svc {
  background: #fff; margin-top: 8px; padding: 12px 14px;
  border-bottom: 1px solid #f0f0f0;
}
.jrp-svc-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px; margin-top: 6px; }
.svc-item {
  display: flex; flex-direction: column; align-items: center;
  gap: 5px; padding: 10px 4px; cursor: pointer; border-radius: 6px;
  transition: background 150ms; min-height: 44px; justify-content: center;
}
.svc-item:hover { background: #f5f5f5; }
.svc-icon-wrap {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border-radius: 50%; background: #f8f8f8;
}
.svc-name { font-size: 11px; color: #555; text-align: center; line-height: 1.3; }
.jrp-service {
  background: #fff; margin-top: auto;
  padding: 10px 14px; font-size: 12px; color: #666;
  display: flex; align-items: center; gap: 6px;
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Tablet ( ≥ 768px)
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
@media (min-width: 768px) {
  .mobile-cat-strip { justify-content: flex-start; flex-wrap: nowrap; }
  .bento-grid { grid-template-columns: repeat(3, 1fr); }
  .bento-grid .card-hero { grid-column: 1 / 3; grid-row: 1 / 3; }
  .bento-grid .card-hero .card-img-wrap { aspect-ratio: auto; height: 100%; }
  .product-grid { grid-template-columns: repeat(3, 1fr); }
  .upcoming-grid { grid-template-columns: repeat(3, 1fr); }
  .jd-recommend-grid { grid-template-columns: repeat(3, 1fr); }
  .channel-grid { grid-template-columns: repeat(6, 1fr); }
  .section { border-radius: 8px; }
  .channel-plaza { border-radius: 8px; }
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Desktop ( ≥ 1024px) — 强网格三栏布局
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
@media (min-width: 1024px) {
  .search-bar-wrap { display: none !important; }

  .home-body {
    display: flex;
    flex-direction: column;
    gap: var(--grid-gap);
  }

  /* ── 首行三栏：CSS Grid (与 header grid-template-columns 完全一致) ── */
  .home-row-top {
    display: grid;
    grid-template-columns: var(--grid-sidebar-w) 1fr var(--grid-panel-w);
    gap: var(--grid-gap);
    align-items: stretch; /* 三列等高 */
  }
  .home-row-top.home-row-top--focus {
    grid-template-columns: 1fr;
  }

  /* 左侧侧边栏 — grid 列 1 */
  .category-sidebar {
    display: block;
    background: #fff;
    border-radius: 0 0 8px 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,.06);
    overflow-y: auto;
    align-self: start; /* 内容不够高时不拉伸 */
  }
  .mobile-cat-strip { display: none; }

  /* 中列 — grid 列 2 */
  .jd-center-col {
    display: flex;
    flex-direction: column;
    gap: 0;
    min-width: 0; /* 防止内容撑破 grid */
  }

  .swipe-wrap {
    border-radius: 8px;
    aspect-ratio: 16 / 5;
    height: auto;
    min-height: unset;
    margin-top: 0;
  }

  /* 桌面端显示 hot-fill / promise 填充区 */
  .hot-fill-section { display: flex; flex: 1; }
  .hot-fill-grid { align-content: flex-start; }
  .jd-promise-section { display: flex; flex: 1; }

  /* 右侧面板 — grid 列 3，自然占满行高 */
  .jd-right-panel {
    display: flex;
    flex-direction: column;
    background: #f4f4f4;
    border-radius: 8px;
    overflow-y: auto;
    box-shadow: 0 2px 8px rgba(0,0,0,.06);
    /* 不需要绝对定位或 width 声明，grid 列宽已固定 */
  }
  .jd-right-panel::-webkit-scrollbar { display: none; }

  /* ── 下方 sections 与频道广场全部使用 grid 容器宽度 ── */
  .section { border-radius: 8px; }
  .channel-plaza { border-radius: 8px; }
  .channel-grid { grid-template-columns: repeat(6, 1fr); }

  .bento-grid { grid-template-columns: repeat(4, 1fr); }
  .bento-grid .card-hero { grid-column: 1 / 3; grid-row: 1 / 3; }
  .product-grid { grid-template-columns: repeat(4, 1fr); }
  .upcoming-grid { grid-template-columns: repeat(4, 1fr); }
  .jd-recommend-grid { grid-template-columns: repeat(4, 1fr); }
  .card-img-wrap { aspect-ratio: 1; }
  .card-name { font-size: 13px; }
  .card-price { font-size: 15px; }
}

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   Large Desktop ( ≥ 1440px) — 加宽侧边栏 + 面板
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
@media (min-width: 1440px) {
  .home {
    --grid-sidebar-w: 220px;
    --grid-panel-w: 208px;
  }
  .bento-grid { grid-template-columns: repeat(5, 1fr); }
  .product-grid { grid-template-columns: repeat(5, 1fr); }
  .upcoming-grid { grid-template-columns: repeat(6, 1fr); }
  .jd-recommend-grid { grid-template-columns: repeat(5, 1fr); }
  .channel-grid { grid-template-columns: repeat(6, 1fr); }
}
</style>
