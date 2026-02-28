<template>
  <div class="login-page">
    <div class="login-top">
      <button class="back-btn" @click="$router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
    </div>

    <div class="login-hero terra-animate">
      <div class="brand-mark">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--t-primary)" stroke-width="1.6" stroke-linecap="round">
          <path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/>
        </svg>
      </div>
      <h1 class="hero-title">Mall V3</h1>
      <p class="hero-subtitle">精选好物，品质生活</p>
    </div>

    <div class="login-form terra-animate" style="animation-delay:80ms">
      <div class="field-group">
        <label class="field-label">用户名</label>
        <input v-model="form.username" class="field-input" placeholder="请输入用户名" autocomplete="username" />
      </div>
      <div class="field-group">
        <label class="field-label">密码</label>
        <input v-model="form.password" class="field-input" type="password" placeholder="请输入密码" autocomplete="current-password" />
      </div>
      <button class="login-btn" :disabled="loading" @click="handleLogin">
        <span v-if="!loading">登录</span>
        <span v-else class="loading-dots">登录中</span>
      </button>
      <button class="register-link" @click="goRegister">还没有账号？ 立即注册</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { appAuthApi } from '@mall/api-sdk/app'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) { showToast('请输入用户名和密码'); return }
  loading.value = true
  try {
    const { data } = await appAuthApi.login(form)
    localStorage.setItem('token', data.data.token)
    router.replace('/')
  } catch (e: unknown) {
    showToast((e as Error).message || '登录失败')
  } finally {
    loading.value = false
  }
}

function goRegister() { router.push('/register') }
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--t-bg);
  padding: 0 24px;
}
.login-top {
  padding: 12px 0;
}
.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--t-bg-card);
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius-full);
  color: var(--t-text);
  cursor: pointer;
}

.login-hero {
  text-align: center;
  padding: 40px 0 32px;
}
.brand-mark {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--t-primary-bg);
  border-radius: var(--t-radius-lg);
  margin: 0 auto 16px;
}
.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--t-text);
  margin: 0;
  letter-spacing: -0.03em;
}
.hero-subtitle {
  font-size: 14px;
  color: var(--t-text-3);
  margin: 6px 0 0;
}

.login-form {
  max-width: 360px;
  margin: 0 auto;
}
.field-group {
  margin-bottom: 16px;
}
.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text-2);
  margin-bottom: 6px;
}
.field-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--t-border);
  border-radius: var(--t-radius-md);
  background: var(--t-bg-card);
  font-size: 15px;
  font-family: var(--t-font);
  color: var(--t-text);
  outline: none;
  transition: border-color var(--t-dur) var(--t-ease);
}
.field-input:focus {
  border-color: var(--t-primary);
}
.field-input::placeholder {
  color: var(--t-text-3);
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: var(--t-primary);
  color: #fff;
  border: none;
  border-radius: var(--t-radius-full);
  font-size: 15px;
  font-weight: 600;
  font-family: var(--t-font);
  cursor: pointer;
  margin-top: 8px;
  transition: background var(--t-dur) var(--t-ease);
}
.login-btn:active {
  background: var(--t-primary-hover);
}
.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-link {
  display: block;
  width: 100%;
  text-align: center;
  background: none;
  border: none;
  color: var(--t-primary);
  font-size: 13px;
  font-family: var(--t-font);
  padding: 16px 0;
  cursor: pointer;
}
</style>

