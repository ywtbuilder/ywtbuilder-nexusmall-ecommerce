<template>
  <div class="attention-page">
    <van-nav-bar title="关注品牌" left-arrow @click-left="$router.back()" />

    <div class="list-wrap">
      <!-- 骨架屏 -->
      <template v-if="loading && list.length === 0">
        <div v-for="i in 4" :key="i" class="brand-card skeleton">
          <van-skeleton avatar :row="2" row-width="['50%','35%']" />
        </div>
      </template>

      <!-- 品牌卡片 -->
      <div v-for="item in list" :key="item.id" class="brand-card">
        <div class="brand-logo-wrap">
          <van-image :src="item.brandLogo" width="52" height="52" radius="8"
            fit="contain" style="background:#f5f5f5"
            @error="(e:any) => onLogoError(e)" />
        </div>
        <div class="brand-info">
          <div class="brand-name">{{ item.brandName || '品牌' }}</div>
          <div class="brand-time" v-if="item.createTime">关注于 {{ formatDate(item.createTime) }}</div>
        </div>
        <button class="follow-btn followed" @click="remove(item.brandId!)">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M20 6L9 17l-5-5"/></svg>
          已关注
        </button>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && list.length === 0" class="empty-wrap">
        <svg width="88" height="88" viewBox="0 0 88 88" fill="none">
          <circle cx="44" cy="44" r="36" fill="#fff0f0"/>
          <path d="M44 58c-8-6-18-14-18-22a10 10 0 0120 0 10 10 0 0120 0c0 8-10 16-22 22z" fill="#e3000b" opacity=".15"/>
          <path d="M44 56c-7-5-16-12-16-20a9 9 0 0118 0 9 9 0 0118 0c0 8-9 14-20 20z" stroke="#e3000b" stroke-width="1.5" fill="none" stroke-dasharray="none"/>
        </svg>
        <p class="empty-title">暂无关注品牌</p>
        <p class="empty-desc">关注品牌后，可及时获取新品和优惠信息</p>
        <button class="go-shop-btn" @click="$router.push('/')">去逛逛</button>
      </div>

      <!-- 错误 -->
      <div v-if="errorMsg" class="error-tip">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><circle cx="12" cy="16" r="0.5" fill="#f59e0b"/></svg>
        {{ errorMsg }}
        <button class="retry-btn" @click="retry">重试</button>
      </div>

      <div v-if="!loading && !errorMsg && list.length > 0 && finished" class="bottom-tip">到底了</div>
      <div v-if="!finished && list.length > 0" class="load-more-btn" @click="loadMore">加载更多</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NavBar as VanNavBar, Image as VanImage, Skeleton as VanSkeleton, showToast, showDialog } from 'vant'
import { memberAttentionApi } from '@mall/api-sdk/app'
import type { MemberBrandAttention } from '@mall/api-sdk/app/member'

const list = ref<MemberBrandAttention[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const errorMsg = ref('')

function formatDate(s: string) { return s.slice(0, 10) }

const fallbackLogo = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="52" height="52" fill="#f5f5f5"><rect width="52" height="52" rx="8"/><text x="26" y="30" text-anchor="middle" font-size="10" fill="#ccc" font-family="sans-serif">LOGO</text></svg>'
)
function onLogoError(e: Event) { (e.target as HTMLImageElement).src = fallbackLogo }

async function loadMore() {
  if (loading.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await memberAttentionApi.list(page.value, 10)
    const data = res.data?.data
    const items: MemberBrandAttention[] = Array.isArray(data) ? data : ((data as any)?.list ?? [])
    list.value.push(...items)
    if (Array.isArray(data) || items.length < 10) finished.value = true
    else page.value++
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '加载失败，请稍后重试'
    finished.value = true
  } finally {
    loading.value = false
  }
}

function retry() {
  finished.value = false
  errorMsg.value = ''
  loadMore()
}

async function remove(brandId: number) {
  await showDialog({ title: '取消关注', message: '确定取消关注该品牌？', confirmButtonColor: '#e3000b' })
  try {
    await memberAttentionApi.delete(brandId)
    list.value = list.value.filter(i => i.brandId !== brandId)
    showToast('已取消关注')
  } catch {
    showToast('操作失败')
  }
}

loadMore()
</script>

<style scoped>
.attention-page { min-height: 100vh; background: #f5f5f5; }

.list-wrap { padding: 12px; display: flex; flex-direction: column; gap: 10px; }

.brand-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.05);
}
.brand-card.skeleton { padding: 16px; }

.brand-logo-wrap {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-info { flex: 1; min-width: 0; }
.brand-name { font-size: 15px; font-weight: 700; color: #222; }
.brand-time { font-size: 11px; color: #bbb; margin-top: 3px; }

.follow-btn {
  flex-shrink: 0;
  border-radius: 18px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 180ms;
  border: 1px solid #ddd;
  background: #f5f5f5;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}
.follow-btn.followed { background: #fff0f0; border-color: #ffcccc; color: #e3000b; }
.follow-btn:hover { opacity: 0.8; }

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0 40px;
  gap: 8px;
}
.empty-title { font-size: 15px; font-weight: 600; color: #444; margin: 0; }
.empty-desc { font-size: 12px; color: #aaa; margin: 0; text-align: center; }
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
.retry-btn {
  border: none;
  background: none;
  color: #e3000b;
  font-size: 13px;
  text-decoration: underline;
  cursor: pointer;
  padding: 0 4px;
}

.bottom-tip {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 12px 0 4px;
}

.load-more-btn {
  text-align: center;
  color: #e3000b;
  font-size: 13px;
  padding: 14px 0;
  cursor: pointer;
}
</style>

