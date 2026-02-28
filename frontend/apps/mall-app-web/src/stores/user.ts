import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { appAuthApi } from '@mall/api-sdk/app'
import type { MemberInfo } from '@mall/api-sdk/app/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<MemberInfo | null>(null)
  const isLoggedIn = computed(() => !!userInfo.value && !!localStorage.getItem('token'))

  // 获取本地头像（MineView 中也用同一 key 保存）
  const avatarSrc = computed(() => {
    if (!userInfo.value) return ''
    const key = `avatar_${userInfo.value.username || userInfo.value.id || 'guest'}`
    return localStorage.getItem(key) || userInfo.value.icon || ''
  })

  /** 有 token 时拉取用户信息，否则清空 */
  async function fetchUser(): Promise<void> {
    if (!localStorage.getItem('token')) {
      userInfo.value = null
      return
    }
    try {
      const { data } = await appAuthApi.info()
      userInfo.value = data.data
    } catch {
      // token 失效时静默清除
      userInfo.value = null
    }
  }

  /** 退出登录 */
  function logout(): void {
    localStorage.removeItem('token')
    userInfo.value = null
  }

  return { userInfo, isLoggedIn, avatarSrc, fetchUser, logout }
})

