<template>
  <div class="jd-item-wrapper">
    <!-- 顶部导航条 -->
    <div class="jd-nav-bar">
      <button class="back-btn" @click="$router.back()">
        <span class="back-icon">←</span> 返回
      </button>
      <span class="nav-title">商品详情（离线预览）</span>
      <span class="nav-sku">SKU: {{ sku }}</span>
    </div>

    <!-- 加载提示 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner" />
      <p>正在加载离线镜像页面…</p>
    </div>

    <!-- JD 页面 iframe -->
    <iframe
      v-show="!loading"
      ref="iframeRef"
      :src="iframeSrc"
      class="jd-iframe"
      sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
      @load="onLoad"
      @error="onError"
    />

    <!-- 错误提示 -->
    <div v-if="error" class="error-overlay">
      <p>⚠️ 页面加载失败：{{ error }}</p>
      <p class="error-hint">
        请确认 <code>item.jd.com_v2/item.jd.com/{{ sku }}.html</code> 文件存在，
        且 Vite 开发服务器正在运行。
      </p>
      <button @click="reload">重试</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 默认 SKU 为华为 WATCH GT 6 雅丹黑 46mm
const sku = computed(() => (route.params.sku as string) || '100274451580')

// 离线镜像 HTML 路径（由 vite.config.ts 中的 jd-mirror-static 插件代理）
const iframeSrc = computed(() => `/item.jd.com/${sku.value}.html`)

const iframeRef = ref<HTMLIFrameElement | null>(null)
const loading = ref(true)
const error = ref('')

function onLoad() {
  loading.value = false
  error.value = ''
}

function onError() {
  loading.value = false
  error.value = `无法加载 ${iframeSrc.value}`
}

function reload() {
  error.value = ''
  loading.value = true
  if (iframeRef.value) {
    // 重新设置 src 触发重载
    iframeRef.value.src = iframeSrc.value
  }
}

onMounted(() => {
  // 5 秒超时保底
  setTimeout(() => {
    if (loading.value) {
      loading.value = false
    }
  }, 5000)
})
</script>

<style scoped>
.jd-item-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background: #f4f4f4;
}

/* ── 顶部导航条 ── */
.jd-nav-bar {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  background: #e1251b; /* 京东红 */
  color: #fff;
  flex-shrink: 0;
  gap: 12px;
  z-index: 10;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(255,255,255,0.15);
  border: none;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 4px;
  transition: background 0.15s;
}

.back-btn:hover {
  background: rgba(255,255,255,0.3);
}

.back-icon {
  font-size: 16px;
}

.nav-title {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
}

.nav-sku {
  font-size: 11px;
  opacity: 0.75;
}

/* ── iframe 主体 ── */
.jd-iframe {
  flex: 1;
  width: 100%;
  border: none;
  background: #fff;
}

/* ── 加载蒙层 ── */
.loading-overlay {
  position: absolute;
  inset: 44px 0 0 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.9);
  gap: 16px;
  z-index: 5;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e1251b33;
  border-top-color: #e1251b;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ── 错误蒙层 ── */
.error-overlay {
  position: absolute;
  inset: 44px 0 0 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #fff;
  gap: 12px;
  color: #333;
  z-index: 5;
}

.error-hint {
  font-size: 12px;
  color: #888;
  text-align: center;
}

.error-hint code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
}

.error-overlay button {
  margin-top: 8px;
  padding: 8px 24px;
  border-radius: 4px;
  border: none;
  background: #e1251b;
  color: #fff;
  cursor: pointer;
}
</style>
