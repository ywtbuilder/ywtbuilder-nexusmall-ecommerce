<template>
  <div class="login-scene noise-overlay">
    <!-- Ambient grid background -->
    <div class="grid-bg" aria-hidden="true"></div>

    <div class="login-card animate-fade-in-up">
      <!-- Brand mark -->
      <div class="brand">
        <div class="brand-icon">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect x="2" y="2" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.9"/>
            <rect x="18" y="2" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.5"/>
            <rect x="2" y="18" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.5"/>
            <rect x="18" y="18" width="12" height="12" rx="3" fill="var(--accent-gold)" opacity="0.25"/>
          </svg>
        </div>
        <h1 class="brand-name">Mall V3</h1>
        <p class="brand-desc">管理后台控制面板</p>
      </div>

      <!-- Login form -->
      <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
        <div class="field-group">
          <label class="field-label">账户</label>
          <el-input
            v-model="form.username"
            placeholder="请输入管理员账户"
            size="large"
            :prefix-icon="UserIcon"
          />
        </div>
        <div class="field-group">
          <label class="field-label">密码</label>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="LockIcon"
          />
        </div>
        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          size="large"
          class="login-btn"
        >
          {{ loading ? '验证中...' : '进入控制台' }}
        </el-button>
      </el-form>

      <div class="login-footer">
        <span>Mall V3 电商管理系统</span>
      </div>
    </div>

    <!-- Decorative corner accent -->
    <div class="corner-accent" aria-hidden="true"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminAuthApi } from '@mall/api-sdk'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

// Simple SVG icon render functions
const UserIcon = () => h('svg', { width: 16, height: 16, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2, 'stroke-linecap': 'round' }, [
  h('circle', { cx: 12, cy: 8, r: 4 }),
  h('path', { d: 'M20 21a8 8 0 10-16 0' }),
])
const LockIcon = () => h('svg', { width: 16, height: 16, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2, 'stroke-linecap': 'round' }, [
  h('rect', { x: 3, y: 11, width: 18, height: 11, rx: 2 }),
  h('path', { d: 'M7 11V7a5 5 0 0110 0v4' }),
])

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账户和密码')
    return
  }
  loading.value = true
  try {
    const { data } = await adminAuthApi.login(form.value)
    localStorage.setItem('token', data.data.token)
    router.push('/')
  } catch {
    ElMessage.error('登录失败，请检查账户密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-scene {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: var(--bg-root);
  overflow: hidden;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.02) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
}

.corner-accent {
  position: absolute;
  top: -120px;
  right: -120px;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212,168,83,.06) 0%, transparent 70%);
  pointer-events: none;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  max-width: calc(100vw - 48px);
  background: var(--bg-surface);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-xl);
  padding: 48px 40px 36px;
  box-shadow:
    0 4px 12px rgba(0,0,0,.3),
    0 16px 48px rgba(0,0,0,.35),
    0 0 0 1px rgba(255,255,255,.03) inset;
}

.brand {
  text-align: center;
  margin-bottom: 40px;
}
.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: rgba(212,168,83,.08);
  border: 1px solid rgba(212,168,83,.15);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}
.brand-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  margin-bottom: 6px;
}
.brand-desc {
  font-size: 13px;
  color: var(--text-muted);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.field-group {
  margin-bottom: 20px;
}
.field-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
  letter-spacing: 0.01em;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-size: 14px;
  letter-spacing: 0.02em;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.03em;
}
</style>
