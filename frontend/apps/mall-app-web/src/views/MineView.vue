<template>
  <div class="mine-page">
    <div class="mine-layout">

      <!-- ====== 左侧导航栏 (桌面端显示) ====== -->
      <nav class="mine-nav">
        <div class="mine-nav-header">我的京东</div>

        <div class="mine-nav-group">
          <div class="mine-nav-group-label">订单中心</div>
          <ul class="mine-nav-list">
            <li class="mine-nav-item mine-nav-item--active" @click="$router.push('/order/list')">我的订单</li>
            <li class="mine-nav-item" @click="$router.push('/order/list')">评价晒单</li>
          </ul>
        </div>

        <div class="mine-nav-single" @click="$router.push('/coupon')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M20 12v6a2 2 0 01-2 2H6a2 2 0 01-2-2v-6"/><path d="M2 7h20v5H2z"/><path d="M12 22V7"/></svg>
          我的钱包
        </div>
        <div class="mine-nav-single" @click="$router.push('/address')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>
          账户设置
        </div>

        <div class="mine-nav-group">
          <div class="mine-nav-group-label">足迹收藏</div>
          <ul class="mine-nav-list">
            <li class="mine-nav-item" @click="$router.push('/readHistory')">我的足迹</li>
            <li class="mine-nav-item" @click="$router.push('/collection')">商品收藏</li>
            <li class="mine-nav-item" @click="$router.push('/attention')">店铺关注</li>
          </ul>
        </div>

        <div class="mine-nav-single">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.63A2 2 0 012 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.2 9.68a16 16 0 006.12 6.12l1.05-1.05a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
          客户服务
        </div>
        <div class="mine-nav-single">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          我的活动
        </div>
        <div class="mine-nav-single">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2"/></svg>
          特色业务
        </div>
        <div class="mine-nav-single">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          生活服务
        </div>
        <div class="mine-nav-single">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          其他业务
        </div>

        <div v-if="userInfo" class="mine-nav-logout" @click="handleLogout">退出登录</div>
      </nav>

      <!-- ====== 右侧主内容区 ====== -->
      <div class="mine-main">

        <!-- ── Profile Card ── -->
        <div class="profile-card">
          <!-- 顶部操作栏 -->
          <div class="profile-brand-bar">
            <span class="brand-bar-text">个人中心</span>
            <div class="header-setting" @click="$router.push('/address')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="1.8" stroke-linecap="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>
            </div>
          </div>

          <!-- 用户信息行 -->
          <div class="profile-info-row">
            <!-- 头像 -->
            <div class="avatar-area">
              <div class="avatar-wrap" @click="userInfo ? showAvatarPreview() : $router.push('/login')">
                <van-image v-if="avatarSrc" round width="72" height="72" :src="avatarSrc" />
                <div v-else class="avatar-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.6" stroke-linecap="round">
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <div v-if="!userInfo" class="avatar-unlogged-tip">点击登录</div>
              </div>
              <div v-if="userInfo" class="change-avatar-btn" @click.stop="triggerUpload">
                <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                换头像
              </div>
              <input ref="fileInputRef" type="file" accept="image/*" style="display:none" @change="onFileChange" />
            </div>

            <!-- 文字信息 -->
            <div class="user-info" v-if="userInfo">
              <div class="username">{{ userInfo.nickname || userInfo.username }}</div>
              <div class="phone">{{ userInfo.phone }}</div>
              <div class="vip-badge">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M2 8.5l4 7 6-11 6 11 4-7"/></svg>
                PLUS会员
              </div>
            </div>
            <div class="user-info" v-else>
              <div class="login-tip" @click="$router.push('/login')">登录 / 注册</div>
              <div class="phone">登录后解锁全部功能</div>
            </div>

            <!-- 右侧统计（桌面端） -->
            <div class="stats-row-desktop">
              <div class="stat-item" @click="$router.push('/coupon')">
                <div class="stat-num">{{ couponCount }}</div>
                <div class="stat-label">优惠券</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item" @click="$router.push('/attention')">
                <div class="stat-num">{{ attentionCount }}</div>
                <div class="stat-label">关注</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item" @click="$router.push('/collection')">
                <div class="stat-num">{{ collectionCount }}</div>
                <div class="stat-label">收藏</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item" @click="$router.push('/readHistory')">
                <div class="stat-num">{{ historyCount }}</div>
                <div class="stat-label">足迹</div>
              </div>
            </div>
          </div>

          <!-- 数据统计栏（移动端） -->
          <div class="stats-row-mobile">
            <div class="stat-item" @click="$router.push('/coupon')">
              <div class="stat-num">{{ couponCount }}</div>
              <div class="stat-label">优惠券</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="$router.push('/attention')">
              <div class="stat-num">{{ attentionCount }}</div>
              <div class="stat-label">关注</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="$router.push('/collection')">
              <div class="stat-num">{{ collectionCount }}</div>
              <div class="stat-label">收藏</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="$router.push('/readHistory')">
              <div class="stat-num">{{ historyCount }}</div>
              <div class="stat-label">足迹</div>
            </div>
          </div>
        </div>

        <!-- ── 我的订单 ── -->
        <div class="section-card">
          <div class="section-header" @click="$router.push('/order/list')">
            <span class="section-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="3"/><path d="M3 9h18M9 21V9"/></svg>
              我的订单
            </span>
            <span class="section-more">全部订单
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="9 18 15 12 9 6"/></svg>
            </span>
          </div>
          <div class="order-icons">
            <div class="order-icon-item" @click="$router.push('/order/list')">
              <div class="order-icon-wrap" style="background:#fff5e8">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#f5a623" stroke-width="1.8" stroke-linecap="round"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg>
              </div>
              <span>待付款</span>
            </div>
            <div class="order-icon-item" @click="$router.push('/order/list')">
              <div class="order-icon-wrap" style="background:#fff0f5">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="1.8" stroke-linecap="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
              </div>
              <span>待发货</span>
            </div>
            <div class="order-icon-item" @click="$router.push('/order/list')">
              <div class="order-icon-wrap" style="background:#f0f7ff">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="1.8" stroke-linecap="round"><rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 3v5h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
              </div>
              <span>待收货</span>
            </div>
            <div class="order-icon-item" @click="$router.push('/order/list')">
              <div class="order-icon-wrap" style="background:#f0fff4">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#52c41a" stroke-width="1.8" stroke-linecap="round"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
              </div>
              <span>待评价</span>
            </div>
            <div class="order-icon-item" @click="$router.push('/order/list')">
              <div class="order-icon-wrap" style="background:#f9f0ff">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#722ed1" stroke-width="1.8" stroke-linecap="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/></svg>
              </div>
              <span>退换售后</span>
            </div>
          </div>
        </div>

        <!-- ── 常用服务 ── -->
        <div class="section-card">
          <div class="section-header">
            <span class="section-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
              常用服务
            </span>
          </div>
          <div class="service-grid">
            <div class="service-item" @click="$router.push('/address')">
              <div class="service-icon" style="background:#fff5e8">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#f5a623" stroke-width="1.8" stroke-linecap="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </div>
              <span>收货地址</span>
            </div>
            <div class="service-item" @click="$router.push('/collection')">
              <div class="service-icon" style="background:#fff0f5">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#e3000b" stroke-width="1.8" stroke-linecap="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
              </div>
              <span>我的收藏</span>
            </div>
            <div class="service-item" @click="$router.push('/readHistory')">
              <div class="service-icon" style="background:#f0f7ff">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#1890ff" stroke-width="1.8" stroke-linecap="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              </div>
              <span>浏览足迹</span>
            </div>
            <div class="service-item" @click="$router.push('/attention')">
              <div class="service-icon" style="background:#f0fff4">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#52c41a" stroke-width="1.8" stroke-linecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
              </div>
              <span>我的关注</span>
            </div>
            <div class="service-item" @click="$router.push('/coupon')">
              <div class="service-icon" style="background:#f9f0ff">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#722ed1" stroke-width="1.8" stroke-linecap="round"><path d="M20 12v6a2 2 0 01-2 2H6a2 2 0 01-2-2v-6"/><path d="M2 7h20v5H2z"/><path d="M12 22V7"/></svg>
              </div>
              <span>优惠券</span>
            </div>
            <div class="service-item" @click="$router.push('/')">
              <div class="service-icon" style="background:#f5f5f5">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#666" stroke-width="1.8" stroke-linecap="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg>
              </div>
              <span>去购物</span>
            </div>
          </div>
        </div>

        <!-- ── 退出登录（移动端 / 无左栏时显示） ── -->
        <div v-if="userInfo" class="logout-section">
          <van-button block round type="danger" @click="handleLogout" class="logout-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" style="margin-right:6px"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            退出登录
          </van-button>
        </div>
        <div v-else class="logout-section">
          <van-button block round type="danger" @click="$router.push('/login')" class="logout-btn">
            立即登录
          </van-button>
        </div>

      </div><!-- /mine-main -->
    </div><!-- /mine-layout -->
  </div><!-- /mine-page -->

  <!-- ====== 头像预览灯箱 ====== -->
  <teleport to="body">
    <div v-if="showPreview" class="avatar-preview-overlay" @click="showPreview = false">
      <div class="avatar-preview-close">✕</div>
      <img :src="avatarSrc" class="avatar-preview-img" @click.stop />
    </div>
  </teleport>

  <!-- ====== 头像裁剪弹窗 ====== -->
  <teleport to="body">
    <div v-if="showCrop" class="crop-overlay" @click.self="showCrop = false">
      <div class="crop-modal">
        <div class="crop-header">
          <span class="crop-title">裁剪头像</span>
          <button class="crop-close" @click="showCrop = false">✕</button>
        </div>
        <div class="crop-canvas-wrap"
          @mousedown.prevent="onDragStart" @mousemove.prevent="onDragMove" @mouseup="onDragEnd"
          @touchstart.prevent="onTouchStart" @touchmove.prevent="onTouchMove" @touchend="onDragEnd"
          @wheel.prevent="onWheel">
          <canvas ref="cropCanvasRef" width="280" height="280"></canvas>
        </div>
        <div class="crop-controls">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/></svg>
          <input class="crop-range" type="range" min="0.5" max="4" step="0.02" v-model.number="cropScale" @input="renderCrop" />
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/></svg>
        </div>
        <div class="crop-actions">
          <button class="crop-cancel-btn" @click="showCrop = false">取消</button>
          <button class="crop-confirm-btn" @click="confirmCrop">确认使用</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Image as VanImage, Button as VanButton, showToast } from 'vant'
import { appAuthApi, userCenterApi } from '@mall/api-sdk/app'
import type { MemberInfo } from '@mall/api-sdk/app/auth'

const router = useRouter()
const userInfo = ref<MemberInfo | null>(null)

// 状态统计
const couponCount = ref(0)
const attentionCount = ref(0)
const collectionCount = ref(0)
const historyCount = ref(0)

// ── 头像（本地优先） ──
const localAvatar = ref<string>('')
const avatarSrc = computed(() => localAvatar.value || userInfo.value?.icon || '')
const fileInputRef = ref<HTMLInputElement | null>(null)

function getAvatarKey() {
  return `avatar_${userInfo.value?.username || userInfo.value?.id || 'guest'}`
}

function loadLocalAvatar() {
  const key = getAvatarKey()
  const saved = localStorage.getItem(key)
  if (saved) localAvatar.value = saved
}

// ── 头像预览 ──
const showPreview = ref(false)
function showAvatarPreview() {
  if (avatarSrc.value) showPreview.value = true
}

function triggerUpload() {
  fileInputRef.value?.click()
}

// ── 裁剪状态 ──
const showCrop = ref(false)
const cropCanvasRef = ref<HTMLCanvasElement | null>(null)
const cropScale = ref(1)
const cropImage = new Image()
let offsetX = 0
let offsetY = 0
let dragging = false
let lastX = 0
let lastY = 0
const CANVAS_SIZE = 280

function renderCrop() {
  const canvas = cropCanvasRef.value
  if (!canvas || !cropImage.src) return
  const ctx = canvas.getContext('2d')!
  const cx = CANVAS_SIZE / 2, cy = CANVAS_SIZE / 2, r = CANVAS_SIZE / 2 - 10

  ctx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)

  // 1. 绘制图片
  const w = cropImage.naturalWidth * cropScale.value
  const h = cropImage.naturalHeight * cropScale.value
  ctx.drawImage(cropImage, offsetX, offsetY, w, h)

  // 2. 圆形外暗色遮罩（even-odd挖孔）
  ctx.save()
  ctx.fillStyle = 'rgba(0,0,0,0.5)'
  ctx.beginPath()
  ctx.rect(0, 0, CANVAS_SIZE, CANVAS_SIZE)  // 外框顺时针
  ctx.arc(cx, cy, r, 0, Math.PI * 2, true) // 圆形逆时针（挖孔）
  ctx.fill('evenodd')
  ctx.restore()

  // 3. 圆形边框
  ctx.save()
  ctx.strokeStyle = 'rgba(255,255,255,0.85)'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.arc(cx, cy, r, 0, Math.PI * 2)
  ctx.stroke()
  ctx.restore()
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  input.value = '' // reset so same file can be re-selected
  const url = URL.createObjectURL(file)
  cropImage.onload = () => {
    // 计算初始缩放使图片铺满圆形区域
    const minSide = Math.min(cropImage.naturalWidth, cropImage.naturalHeight)
    cropScale.value = Math.max(1, (CANVAS_SIZE / minSide) * 1.1)
    // 居中
    const w = cropImage.naturalWidth * cropScale.value
    const h = cropImage.naturalHeight * cropScale.value
    offsetX = (CANVAS_SIZE - w) / 2
    offsetY = (CANVAS_SIZE - h) / 2
    showCrop.value = true
    nextTick(renderCrop)
  }
  cropImage.src = url
}

// 拖拽
function onDragStart(e: MouseEvent) { dragging = true; lastX = e.clientX; lastY = e.clientY }
function onTouchStart(e: TouchEvent) { dragging = true; lastX = e.touches[0].clientX; lastY = e.touches[0].clientY }
function onDragMove(e: MouseEvent) {
  if (!dragging) return
  offsetX += e.clientX - lastX
  offsetY += e.clientY - lastY
  lastX = e.clientX; lastY = e.clientY
  renderCrop()
}
function onTouchMove(e: TouchEvent) {
  if (!dragging) return
  offsetX += e.touches[0].clientX - lastX
  offsetY += e.touches[0].clientY - lastY
  lastX = e.touches[0].clientX; lastY = e.touches[0].clientY
  renderCrop()
}
function onDragEnd() { dragging = false }

// 滚轮缩放
function onWheel(e: WheelEvent) {
  const delta = e.deltaY > 0 ? -0.08 : 0.08
  const newScale = Math.max(0.5, Math.min(4, cropScale.value + delta))
  // 以画布中心为锚点缩放图片位置
  const cx = CANVAS_SIZE / 2, cy = CANVAS_SIZE / 2
  const ratio = newScale / cropScale.value
  offsetX = cx + (offsetX - cx) * ratio
  offsetY = cy + (offsetY - cy) * ratio
  cropScale.value = newScale
  renderCrop()
}

function confirmCrop() {
  if (!cropImage.src) return
  const r = CANVAS_SIZE / 2 - 10
  const size = r * 2
  const out = document.createElement('canvas')
  out.width = size
  out.height = size
  const ctx = out.getContext('2d')!
  ctx.beginPath()
  ctx.arc(r, r, r, 0, Math.PI * 2)
  ctx.clip()
  // 直接从原始图片绘制（不带遮罩）
  const w = cropImage.naturalWidth * cropScale.value
  const h = cropImage.naturalHeight * cropScale.value
  ctx.drawImage(cropImage, offsetX - (CANVAS_SIZE / 2 - r), offsetY - (CANVAS_SIZE / 2 - r), w, h)
  const base64 = out.toDataURL('image/jpeg', 0.92)
  localAvatar.value = base64
  localStorage.setItem(getAvatarKey(), base64)
  showCrop.value = false
  showToast('头像已更新')
}

onMounted(async () => {
  if (!localStorage.getItem('token')) return
  try {
    // 先并行拉取用户基本信息 + 中心统计数据
    const [infoRes, summaryRes] = await Promise.allSettled([
      appAuthApi.info(),
      userCenterApi.summary(),
    ])
    if (infoRes.status === 'fulfilled') {
      userInfo.value = infoRes.value.data.data
      loadLocalAvatar()
    }
    if (summaryRes.status === 'fulfilled') {
      const s = summaryRes.value.data.data
      couponCount.value = s.stats?.couponCount ?? 0
      attentionCount.value = s.stats?.followCount ?? 0
      collectionCount.value = s.stats?.favCount ?? 0
      historyCount.value = s.stats?.footprintCount ?? 0
    } else {
      // 后端未启动时回退默认值，避免页面空白
      couponCount.value = 3
      attentionCount.value = 12
      collectionCount.value = 8
      historyCount.value = 24
    }
  } catch { /* ignore */ }
})

function handleLogout() {
  localStorage.removeItem('token')
  userInfo.value = null
  localAvatar.value = ''
  couponCount.value = 0
  attentionCount.value = 0
  collectionCount.value = 0
  historyCount.value = 0
  showToast('已退出登录')
  router.push('/')
}
</script>

<style scoped>
/* ============================================================
   「我的」页面 — JD 风格双栏布局
   ============================================================ */
.mine-page {
  min-height: 100vh;
  background: #f4f5f7;
  padding-bottom: 40px;
}

/* ── 版心双栏容器 ── */
.mine-layout {
  width: 100%;
  max-width: var(--app-content-max-width, 1200px);
  margin: 0 auto;
  padding: 16px 16px 0;
  box-sizing: border-box;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

/* ============================================================
   左侧导航栏 (mobile 默认隐藏，桌面端显示)
   ============================================================ */
.mine-nav {
  display: none;
}

/* ── 右侧主内容 ── */
.mine-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ============================================================
   Profile Card
   ============================================================ */
.profile-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,.05);
  overflow: hidden;
}

.profile-brand-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 0;
}
.brand-bar-text {
  font-size: 13px;
  color: #aaa;
  font-weight: 500;
}
.header-setting {
  padding: 4px;
  cursor: pointer;
  color: #bbb;
  border-radius: 50%;
  transition: background 150ms, color 150ms;
}
.header-setting:hover { background: #f5f5f5; color: #666; }

/* 用户信息行 */
.profile-info-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px 14px;
}

/* 头像 */
.avatar-area {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}
.avatar-wrap { position: relative; cursor: pointer; }
:deep(.van-image) {
  display: block;
  border: 2.5px solid #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,.12);
}
.avatar-placeholder {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: #f5f5f5;
  border: 2px solid #eee;
  display: flex; align-items: center; justify-content: center;
}
.avatar-unlogged-tip {
  position: absolute;
  bottom: -2px; left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 10px;
  background: rgba(0,0,0,.5);
  color: #fff;
  padding: 1px 6px;
  border-radius: 4px;
  pointer-events: none;
}
.change-avatar-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: #999;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 150ms, color 150ms;
  white-space: nowrap;
}
.change-avatar-btn:hover { background: #f5f5f5; color: #666; }

/* 用户文字信息 */
.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.username {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.phone { font-size: 13px; color: #999; }
.vip-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #a07800;
  background: linear-gradient(90deg, #fff8e1, #ffe082);
  border: 1px solid #ffd54f;
  border-radius: 20px;
  padding: 2px 8px;
  width: fit-content;
}
.login-tip {
  font-size: 17px;
  font-weight: 700;
  color: #e3000b;
  cursor: pointer;
}

/* ── 右侧统计（桌面端在 profile-info-row 内）── */
.stats-row-desktop {
  display: none;
}

/* ── 统计栏（移动端在 profile-card 底部）── */
.stats-row-mobile {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 10px 16px 14px;
  border-top: 1px solid #f5f5f5;
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  cursor: pointer;
}
.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}
.stat-label { font-size: 12px; color: #999; }
.stat-divider {
  width: 1px;
  height: 24px;
  background: #eee;
}

/* ============================================================
   通用 Section Card（订单 / 服务）
   ============================================================ */
.section-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,.05);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 8px;
  cursor: pointer;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
}
.section-more {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #bbb;
}

/* ── 订单图标行 ── */
.order-icons {
  display: flex;
  align-items: flex-start;
  padding: 4px 0 14px;
}
.order-icon-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  cursor: pointer;
  padding: 4px 0;
  transition: opacity 150ms;
}
.order-icon-item:hover { opacity: .75; }
.order-icon-wrap {
  width: 46px; height: 46px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.order-icon-item span { font-size: 12px; color: #444; }

/* ── 服务宫格 ── */
.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 4px 0 8px;
}
.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  padding: 12px 8px;
  cursor: pointer;
  transition: background 150ms;
}
.service-item:hover { background: #fafafa; }
.service-icon {
  width: 46px; height: 46px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.service-item span { font-size: 12px; color: #444; }

/* ── 退出按钮（移动端 / 无左栏时） ── */
.logout-section { padding: 4px 0 8px; }
.logout-btn {
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px !important;
}

/* ============================================================
   桌面端 (≥ 1024px) — 双栏布局激活
   ============================================================ */
@media (min-width: 1024px) {
  .mine-layout {
    padding-top: 24px;
  }

  /* 左侧导航栏显示 */
  .mine-nav {
    display: block;
    width: 180px;
    flex-shrink: 0;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 1px 6px rgba(0,0,0,.06);
    overflow: hidden;
    position: sticky;
    top: 108px; /* below 96px shell padding + 12px gap */
    align-self: flex-start;
  }

  .mine-nav-header {
    background: #e3000b;
    color: #fff;
    font-size: 14px;
    font-weight: 700;
    padding: 14px 16px;
    letter-spacing: 0.02em;
  }

  .mine-nav-group {
    border-bottom: 1px solid #f5f5f5;
  }
  .mine-nav-group-label {
    font-size: 12px;
    color: #999;
    font-weight: 600;
    padding: 10px 16px 4px;
    letter-spacing: 0.04em;
    text-transform: uppercase;
  }
  .mine-nav-list {
    list-style: none;
    margin: 0;
    padding: 0 0 8px;
  }
  .mine-nav-item {
    font-size: 13px;
    color: #444;
    padding: 7px 16px 7px 24px;
    cursor: pointer;
    transition: background 120ms, color 120ms;
    position: relative;
  }
  .mine-nav-item:hover { background: #fef5f5; color: #e3000b; }
  .mine-nav-item--active {
    color: #e3000b;
    font-weight: 600;
  }
  .mine-nav-item--active::before {
    content: '';
    position: absolute;
    left: 14px;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 12px;
    background: #e3000b;
    border-radius: 2px;
  }

  .mine-nav-single {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #333;
    padding: 10px 16px;
    cursor: pointer;
    transition: background 120ms, color 120ms;
    border-bottom: 1px solid #f5f5f5;
  }
  .mine-nav-single:hover { background: #fef5f5; color: #e3000b; }
  .mine-nav-single svg { color: #bbb; flex-shrink: 0; }

  .mine-nav-logout {
    font-size: 13px;
    color: #999;
    padding: 12px 16px;
    cursor: pointer;
    text-align: center;
    transition: color 150ms;
    border-top: 1px solid #f5f5f5;
  }
  .mine-nav-logout:hover { color: #e3000b; }

  /* 桌面端退出按钮（mine-main 内）隐藏 */
  .mine-main > .logout-section {
    display: none;
  }

  /* 服务宫格桌面端6列 */
  .service-grid { grid-template-columns: repeat(6, 1fr); }

  /* 桌面端：头像信息行内显示统计数字（紧凑） */
  .stats-row-desktop {
    display: flex;
    align-items: center;
    gap: 0;
    margin-left: auto;
    flex-shrink: 0;
  }
  .stats-row-mobile { display: none; }

  /* 统计数字桌面样式 */
  .stats-row-desktop .stat-item {
    flex: none;
    padding: 0 20px;
    border-right: 1px solid #eee;
  }
  .stats-row-desktop .stat-item:last-child { border-right: none; }
  .stats-row-desktop .stat-num { font-size: 22px; }
  .stats-row-desktop .stat-divider { display: none; }
}

/* ============================================================
   头像预览灯箱
   ============================================================ */
.avatar-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  cursor: zoom-out;
}
.avatar-preview-close {
  position: absolute;
  top: 20px; right: 24px;
  color: rgba(255,255,255,.7);
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
  transition: color 150ms;
}
.avatar-preview-close:hover { color: #fff; }
.avatar-preview-img {
  max-width: min(320px, 80vw);
  max-height: min(320px, 80vh);
  border-radius: 50%;
  box-shadow: 0 8px 40px rgba(0,0,0,.5);
  border: 4px solid rgba(255,255,255,.2);
  cursor: default;
}

/* ============================================================
   头像裁剪弹窗
   ============================================================ */
.crop-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.crop-modal {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  width: 320px;
  max-width: 95vw;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  box-shadow: 0 8px 32px rgba(0,0,0,.3);
}
.crop-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.crop-title { font-size: 16px; font-weight: 700; color: #333; }
.crop-close {
  background: none; border: none;
  font-size: 18px; color: #999; cursor: pointer; padding: 4px 8px; line-height: 1;
}
.crop-close:hover { color: #333; }
.crop-canvas-wrap {
  position: relative;
  cursor: grab;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 0 0 3px #e3000b;
  user-select: none;
}
.crop-canvas-wrap:active { cursor: grabbing; }
.crop-canvas-wrap canvas { display: block; }
.crop-controls {
  display: flex; align-items: center; gap: 10px; width: 100%;
}
.crop-range {
  flex: 1; accent-color: #e3000b; height: 4px; cursor: pointer;
}
.crop-actions { display: flex; gap: 12px; width: 100%; }
.crop-cancel-btn, .crop-confirm-btn {
  flex: 1; height: 42px; border-radius: 21px; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: opacity 150ms;
}
.crop-cancel-btn { background: #f5f5f5; color: #666; }
.crop-cancel-btn:hover { background: #ebebeb; }
.crop-confirm-btn { background: #e3000b; color: #fff; }
.crop-confirm-btn:hover { opacity: 0.88; }
</style>

