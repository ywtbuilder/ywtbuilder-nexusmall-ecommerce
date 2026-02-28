<template>
  <div class="collection-page">
    <van-nav-bar title="我的收藏" left-arrow @click-left="$router.back()" />
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
      <van-card v-for="item in list" :key="item.id"
        :title="item.productName" :thumb="item.productPic"
        :price="String(item.productPrice ?? '')"
        @click="$router.push(`/product/${item.productId}`)"
      >
        <template #footer>
          <van-button size="mini" @click.stop="remove(item.productId!)">取消收藏</van-button>
        </template>
      </van-card>
    </van-list>
    <van-empty v-if="!loading && list.length === 0" description="暂无收藏" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NavBar as VanNavBar, List as VanList, Card as VanCard, Button as VanButton, Empty as VanEmpty, showToast } from 'vant'
import { memberCollectionApi } from '@mall/api-sdk/app'
import type { MemberProductCollection } from '@mall/api-sdk/app/member'

const list = ref<MemberProductCollection[]>([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)

async function loadMore() {
  try {
    const res = await memberCollectionApi.list(page.value, 10)
    const data = res.data.data
    const items = Array.isArray(data) ? data : (data?.list ?? [])
    list.value.push(...items)
    finished.value = Array.isArray(data) || items.length < 10
    page.value++
  } finally {
    loading.value = false
  }
}

async function remove(productId: number) {
  await memberCollectionApi.delete(productId)
  list.value = list.value.filter(i => i.productId !== productId)
  showToast('已取消收藏')
}
</script>

<style scoped>
.collection-page { min-height: 100vh; background: var(--t-bg); }
</style>

