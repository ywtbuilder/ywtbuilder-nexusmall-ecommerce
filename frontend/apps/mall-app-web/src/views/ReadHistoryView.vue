<template>
  <div class="history-page">
    <van-nav-bar title="浏览记录" left-arrow @click-left="$router.back()">
      <template #right>
        <span style="color:#ee0a24;font-size:14px" @click="clearAll">清空</span>
      </template>
    </van-nav-bar>
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <van-card v-for="item in list" :key="item.id"
        :title="item.productName" :thumb="item.productPic"
        :price="String(item.productPrice ?? '')"
        @click="$router.push(`/product/${item.productId}`)"
      >
        <template #footer>
          <van-button size="mini" @click.stop="remove(item.id!)">删除</van-button>
        </template>
      </van-card>
    </van-list>
    <van-empty v-if="!loading && list.length === 0" description="暂无浏览记录" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NavBar as VanNavBar, List as VanList, Card as VanCard, Button as VanButton, Empty as VanEmpty, showToast, showConfirmDialog } from 'vant'
import { memberReadHistoryApi } from '@mall/api-sdk/app'
import type { MemberReadHistory } from '@mall/api-sdk/app/member'

const list = ref<MemberReadHistory[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadMore() {
  try {
    const res = await memberReadHistoryApi.list(page.value, 10)
    const data = res.data.data
    const items = Array.isArray(data) ? data : (data?.list ?? [])
    list.value.push(...items)
    finished.value = Array.isArray(data) || items.length < 10
    page.value++
  } finally {
    loading.value = false
  }
}

async function remove(id: string) {
  await memberReadHistoryApi.delete([id])
  list.value = list.value.filter(i => i.id !== id)
  showToast('已删除')
}

async function clearAll() {
  await showConfirmDialog({ title: '确认清空所有浏览记录?' })
  await memberReadHistoryApi.clear()
  list.value = []
  showToast('已清空')
}
</script>

<style scoped>
.history-page { min-height: 100vh; background: var(--t-bg); }
</style>

