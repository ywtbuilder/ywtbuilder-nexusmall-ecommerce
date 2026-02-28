<template>
  <div class="admin-layout noise-overlay">
    <!-- ═══ Sidebar ═══ -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- Brand -->
      <div class="sidebar-brand" @click="$router.push('/')">
        <div class="brand-logo">
          <svg width="22" height="22" viewBox="0 0 32 32" fill="none">
            <rect x="2" y="2" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.9"/>
            <rect x="18" y="2" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.5"/>
            <rect x="2" y="18" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.5"/>
            <rect x="18" y="18" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.25"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!sidebarCollapsed" class="brand-text">Mall V3</span>
        </transition>
      </div>

      <!-- Navigation -->
      <nav class="sidebar-nav">
        <div class="nav-section">
          <router-link to="/dashboard" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">仪表盘</span>
          </router-link>
        </div>

        <div class="nav-section">
          <div class="nav-section-title" v-if="!sidebarCollapsed">商品</div>
          <router-link to="/pms/product" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">商品列表</span>
          </router-link>
          <router-link to="/pms/brand" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">品牌管理</span>
          </router-link>
          <router-link to="/pms/productCategory" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">商品分类</span>
          </router-link>
          <router-link to="/pms/productAttrCategory" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">属性分类</span>
          </router-link>
        </div>

        <div class="nav-section">
          <div class="nav-section-title" v-if="!sidebarCollapsed">订单</div>
          <router-link to="/oms/order" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">订单管理</span>
          </router-link>
          <router-link to="/oms/returnApply" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">退货管理</span>
          </router-link>
        </div>

        <div class="nav-section">
          <div class="nav-section-title" v-if="!sidebarCollapsed">营销</div>
          <router-link to="/sms/coupon" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M20 12V8H6a2 2 0 01-2-2c0-1.1.9-2 2-2h12v4"/><path d="M4 6v12c0 1.1.9 2 2 2h14v-4"/><path d="M18 12a2 2 0 000 4h4v-4h-4z"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">优惠券</span>
          </router-link>
          <router-link to="/sms/flash" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">秒杀活动</span>
          </router-link>
          <router-link to="/sms/advertise" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">广告管理</span>
          </router-link>
        </div>

        <div class="nav-section">
          <div class="nav-section-title" v-if="!sidebarCollapsed">权限</div>
          <router-link to="/ums/admin" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">用户管理</span>
          </router-link>
          <router-link to="/ums/role" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">角色管理</span>
          </router-link>
          <router-link to="/ums/menu" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">菜单管理</span>
          </router-link>
          <router-link to="/ums/resource" class="nav-item" active-class="active">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
            <span v-if="!sidebarCollapsed" class="nav-label">资源管理</span>
          </router-link>
        </div>
      </nav>

      <!-- Sidebar toggle -->
      <button class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          :style="{ transform: sidebarCollapsed ? 'rotate(180deg)' : 'none', transition: 'transform 0.3s var(--ease-out)' }">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
    </aside>

    <!-- ═══ Main Content ═══ -->
    <div class="main-wrapper">
      <!-- Header bar -->
      <header class="top-bar">
        <div class="top-bar-left">
          <h2 class="page-title">{{ currentPageTitle }}</h2>
        </div>
        <div class="top-bar-right">
          <div class="admin-badge">
            <div class="admin-avatar">
              {{ (adminName || 'A').charAt(0).toUpperCase() }}
            </div>
            <span class="admin-name">{{ adminName || '管理员' }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout" title="退出登录">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </div>
      </header>

      <!-- Page content -->
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { adminAuthApi } from '@mall/api-sdk'

const router = useRouter()
const route = useRoute()
const adminName = ref('')
const sidebarCollapsed = ref(false)

const pageTitleMap: Record<string, string> = {
  '/dashboard': '仪表盘',
  '/pms/product': '商品列表',
  '/pms/brand': '品牌管理',
  '/pms/productCategory': '商品分类',
  '/pms/productAttrCategory': '属性分类',
  '/oms/order': '订单管理',
  '/oms/returnApply': '退货管理',
  '/sms/coupon': '优惠券管理',
  '/sms/flash': '秒杀活动',
  '/sms/advertise': '广告管理',
  '/ums/admin': '用户管理',
  '/ums/role': '角色管理',
  '/ums/menu': '菜单管理',
  '/ums/resource': '资源管理',
}

const currentPageTitle = computed(() => pageTitleMap[route.path] ?? '控制台')

onMounted(async () => {
  try {
    const { data } = await adminAuthApi.info()
    adminName.value = data.data.username ?? ''
  } catch { /* ignore */ }
})

function handleLogout() {
  adminAuthApi.logout().catch(() => {})
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ── Sidebar ── */
.sidebar {
  width: 240px;
  min-width: 240px;
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  transition: width var(--duration-normal) var(--ease-out),
              min-width var(--duration-normal) var(--ease-out);
  position: relative;
  z-index: 10;
}
.sidebar.collapsed {
  width: 64px;
  min-width: 64px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-subtle);
  min-height: 64px;
}
.sidebar.collapsed .sidebar-brand {
  justify-content: center;
  padding: 20px 0;
}
.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-text {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 8px;
}

.nav-section {
  margin-bottom: 8px;
}
.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 12px 12px 6px;
  white-space: nowrap;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 13px;
  font-weight: 450;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
  position: relative;
}
.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 10px;
}
.nav-item:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}
.nav-item.active {
  color: var(--accent-gold);
  background: rgba(212,168,83,.08);
}
.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 3px;
  background: var(--accent-gold);
  border-radius: 0 2px 2px 0;
}

.nav-label {
  overflow: hidden;
  text-overflow: ellipsis;
}
.sidebar.collapsed .nav-label {
  display: none;
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px auto 12px;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--bg-elevated);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.sidebar-toggle:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* ── Main wrapper ── */
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

/* ── Top bar ── */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}
.top-bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.top-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.admin-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: 100px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
}
.admin-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-gold), var(--accent-orange));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #0c0c10;
}
.admin-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}
.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.logout-btn:hover {
  background: rgba(192,85,85,.08);
  border-color: rgba(192,85,85,.2);
  color: var(--accent-red);
}

/* ── Content area ── */
.content-area {
  flex: 1;
  overflow: auto;
  padding: 24px;
  background: var(--bg-root);
}

/* ── Page transitions ── */
.page-enter-active {
  animation: fadeInUp var(--duration-normal) var(--ease-out);
}
.page-leave-active {
  animation: fadeIn var(--duration-fast) var(--ease-out) reverse;
}

/* ── Fade transition helper ── */
.fade-enter-active, .fade-leave-active {
  transition: opacity var(--duration-fast) var(--ease-out);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
