<template>
  <div class="register-page">
    <div class="reg-top">
      <button class="back-btn" @click="$router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
    </div>

    <div class="reg-hero terra-animate">
      <h1 class="hero-title">创建账号</h1>
      <p class="hero-subtitle">加入 Mall V3，发现更多好物</p>
    </div>

    <div class="reg-form terra-animate" style="animation-delay:80ms">
      <div class="field-group">
        <label class="field-label">用户名</label>
        <input v-model="form.username" class="field-input" placeholder="请输入用户名" />
      </div>
      <div class="field-group">
        <label class="field-label">密码</label>
        <input v-model="form.password" class="field-input" type="password" placeholder="请输入密码" />
      </div>
      <div class="field-group">
        <label class="field-label">手机号</label>
        <input v-model="form.telephone" class="field-input" placeholder="请输入手机号" />
      </div>
      <div class="field-group">
        <label class="field-label">验证码</label>
        <div class="code-row">
          <input v-model="form.authCode" class="field-input" placeholder="请输入验证码" style="flex:1" />
          <button class="code-btn" :disabled="countdown > 0" @click="getCode">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </div>
      </div>
      <button class="submit-btn" :disabled="loading" @click="handleRegister">注册</button>
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
const countdown = ref(0)
const form = reactive({ username: '', password: '', telephone: '', authCode: '' })

async function getCode() {
  if (!form.telephone) { showToast('请输入手机号'); return }
  try {
    const res = await appAuthApi.getAuthCode(form.telephone)
    form.authCode = res.data.data
    showToast(`验证码已发送: ${res.data.data}`)
    countdown.value = 60
    const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch (e: unknown) {
    showToast((e as Error).message || '获取验证码失败')
  }
}

async function handleRegister() {
  if (!form.username || !form.password || !form.telephone || !form.authCode) {
    showToast('请填写完整信息'); return
  }
  loading.value = true
  try {
    await appAuthApi.register(form)
    showToast('注册成功')
    router.replace('/login')
  } catch (e: unknown) {
    showToast((e as Error).message || '注册失败')
  } finally { loading.value = false }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--t-bg);
  padding: 0 24px;
}
.reg-top { padding: 12px 0; }
.back-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  background: var(--t-bg-card); border: 1px solid var(--t-border);
  border-radius: var(--t-radius-full); color: var(--t-text); cursor: pointer;
}
.reg-hero { padding: 24px 0 20px; }
.hero-title { font-size: 24px; font-weight: 700; color: var(--t-text); margin: 0; letter-spacing: -0.02em; }
.hero-subtitle { font-size: 14px; color: var(--t-text-3); margin: 4px 0 0; }
.reg-form { max-width: 360px; }
.field-group { margin-bottom: 14px; }
.field-label { display: block; font-size: 13px; font-weight: 600; color: var(--t-text-2); margin-bottom: 6px; }
.field-input {
  width: 100%; padding: 12px 14px;
  border: 1px solid var(--t-border); border-radius: var(--t-radius-md);
  background: var(--t-bg-card); font-size: 15px; font-family: var(--t-font);
  color: var(--t-text); outline: none;
  transition: border-color var(--t-dur) var(--t-ease);
}
.field-input:focus { border-color: var(--t-primary); }
.field-input::placeholder { color: var(--t-text-3); }
.code-row { display: flex; gap: 8px; }
.code-btn {
  flex-shrink: 0; padding: 0 14px;
  background: var(--t-primary-bg); color: var(--t-primary);
  border: 1px solid var(--t-primary); border-radius: var(--t-radius-md);
  font-size: 13px; font-weight: 600; font-family: var(--t-font); cursor: pointer;
  white-space: nowrap;
}
.code-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.submit-btn {
  width: 100%; padding: 14px;
  background: var(--t-primary); color: #fff; border: none;
  border-radius: var(--t-radius-full); font-size: 15px; font-weight: 600;
  font-family: var(--t-font); cursor: pointer; margin-top: 8px;
}
.submit-btn:active { background: var(--t-primary-hover); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>

