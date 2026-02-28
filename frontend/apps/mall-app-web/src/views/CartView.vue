<template>
  <div class="cart-page">
    <!-- 顶部工具栏（京东风格） -->
    <div class="cart-header">
      <span class="cart-title">购物车</span>
      <span class="cart-clear" @click="confirmClear" v-if="list.length">清理购物车</span>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <van-loading size="24px" vertical>加载中...</van-loading>
    </div>

    <!-- 空购物车 -->
    <van-empty v-else-if="!list.length" description="购物车空空如也">
      <template #image>
        <svg width="160" height="160" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="100" cy="100" r="90" fill="#f5f5f5"/>
          <path d="M40 60h12l18 70a8 8 0 008 6h60a8 8 0 008-6l10-46H60" stroke="#e0e0e0" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="82" cy="152" r="10" fill="#e0e0e0"/>
          <circle cx="138" cy="152" r="10" fill="#e0e0e0"/>
          <path d="M75 95l14 14 26-28" stroke="#e3000b" stroke-width="6" stroke-linecap="round" stroke-linejoin="round" opacity="0.3"/>
        </svg>
      </template>
      <van-button round type="primary" style="width: 160px; margin-top: 16px" @click="$router.push('/')">去逛逛</van-button>
    </van-empty>

    <!-- 商品列表 -->
    <template v-else>
      <van-checkbox-group v-model="checkedIds">
        <div v-for="item in list" :key="item.id" class="cart-item">
          <!-- 勾选框 -->
          <van-checkbox class="item-check" :name="item.id" />

          <!-- 商品图 -->
          <img
            class="item-pic"
            :src="item.productPic || ''"
            :alt="item.productName"
            @error="($event.target as HTMLImageElement).style.display='none'"
          />

          <!-- 中间信息区 -->
          <div class="item-mid">
            <div class="item-name">{{ item.productName }}</div>
            <div class="item-attr" v-if="item.productAttr && !isRawJson(item.productAttr)">
              {{ item.productAttr }}
            </div>
            <div class="item-price-row">
              <span class="item-price">¥{{ Number(item.price ?? 0).toFixed(2) }}</span>
            </div>
          </div>

          <!-- 右侧：数量 + 删除 -->
          <div class="item-right">
            <!-- 京东式数量调节：— 数字 + -->
            <div class="qty-bar">
              <button
                class="qty-btn"
                :disabled="updatingId === item.id"
                @click="onMinus(item)"
              >—</button>
              <span class="qty-num">{{ item.quantity ?? 1 }}</span>
              <button
                class="qty-btn"
                :disabled="updatingId === item.id"
                @click="onPlus(item)"
              >+</button>
            </div>
            <!-- 京东式删除文字按钮 -->
            <button
              class="btn-delete"
              :disabled="deletingId === item.id"
              @click="removeItem(item)"
            >
              <span v-if="deletingId === item.id">...</span>
              <span v-else>删除</span>
            </button>
          </div>
        </div>
      </van-checkbox-group>
    </template>

    <!-- 底部结算栏 -->
    <div v-if="list.length" class="cart-bar">
      <div class="bar-left">
        <van-checkbox v-model="allChecked" @click="toggleAll" />
        <span class="bar-all">全选</span>
      </div>
      <div class="bar-right">
        <div class="bar-total">
          合计：<span class="bar-price">¥{{ (totalPrice / 100).toFixed(2) }}</span>
        </div>
        <button class="bar-btn" @click="goCheckout">去结算({{ checkedIds.length }})</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckboxGroup as VanCheckboxGroup,
  Checkbox as VanCheckbox,
  Empty as VanEmpty,
  Button as VanButton,
  Loading as VanLoading,
  showToast,
  showConfirmDialog,
} from 'vant'
import { cartApi } from '@mall/api-sdk/app'
import type { CartItem } from '@mall/api-sdk/app/cart'

const router = useRouter()
const loading = ref(false)
const list = ref<CartItem[]>([])
const checkedIds = ref<number[]>([])
const updatingId = ref<number | null>(null)
const deletingId = ref<number | null>(null)

/** productAttr 有时是原始 JSON 字符串，不应直接显示 */
function isRawJson(s: string) {
  return s.trimStart().startsWith('[') || s.trimStart().startsWith('{')
}

const allChecked = computed({
  get: () => list.value.length > 0 && checkedIds.value.length === list.value.length,
  set: (v: boolean) => {
    checkedIds.value = v ? list.value.map(i => i.id) : []
  },
})

const totalPrice = computed(() => {
  return list.value
    .filter(i => checkedIds.value.includes(i.id))
    .reduce((sum, i) => sum + (i.price ?? 0) * (i.quantity ?? 1), 0) * 100
})

function toggleAll() {
  if (allChecked.value) {
    checkedIds.value = []
  } else {
    checkedIds.value = list.value.map(i => i.id)
  }
}

async function fetchCart() {
  loading.value = true
  try {
    const { data } = await cartApi.list()
    list.value = (data.data ?? []).map(i => ({ ...i, quantity: i.quantity ?? 1 }))
    checkedIds.value = list.value.map(i => i.id)
  } catch (e: unknown) {
    showToast((e as Error).message || '获取购物车失败')
  } finally {
    loading.value = false
  }
}

/** 减号：数量 > 1 则 -1；数量 = 1 则直接删除（无确认框） */
async function onMinus(item: CartItem) {
  const cur = item.quantity ?? 1
  if (cur <= 1) {
    // 数量已是 1，点减号 → 直接删除
    await removeItem(item)
    return
  }
  await updateQty(item, cur - 1)
}

async function onPlus(item: CartItem) {
  await updateQty(item, (item.quantity ?? 1) + 1)
}

async function updateQty(item: CartItem, qty: number) {
  if (qty < 1) qty = 1
  if (qty > 99) qty = 99
  updatingId.value = item.id
  try {
    await cartApi.updateQuantity(item.id, qty)
    item.quantity = qty
  } catch (e: unknown) {
    showToast((e as Error).message || '修改数量失败')
    await fetchCart()   // 回滚到服务器状态
  } finally {
    updatingId.value = null
  }
}

async function removeItem(item: CartItem) {
  deletingId.value = item.id
  try {
    await cartApi.delete([item.id])
    list.value = list.value.filter(i => i.id !== item.id)
    checkedIds.value = checkedIds.value.filter(id => id !== item.id)
    showToast('已移除')
  } catch (e: unknown) {
    showToast((e as Error).message || '删除失败，请重试')
  } finally {
    deletingId.value = null
  }
}

async function confirmClear() {
  try {
    await showConfirmDialog({ title: '清理购物车', message: '确定清空购物车中所有商品？' })
    await cartApi.clear()
    list.value = []
    checkedIds.value = []
    showToast('购物车已清空')
  } catch {
    // 用户取消，不做任何操作
  }
}

function goCheckout() {
  if (!checkedIds.value.length) { showToast('请先选择商品'); return }
  router.push({ path: '/order/confirm', query: { cartIds: checkedIds.value.join(',') } })
}

onMounted(fetchCart)
</script>

<style scoped>
/* ─── 基础容器 ─────────────────────────────────── */
.cart-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 64px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ─── 顶部工具栏 ───────────────────────────────── */
.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.cart-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}
.cart-clear {
  font-size: 13px;
  color: #e4393c;
  cursor: pointer;
}

/* ─── 加载 ─────────────────────────────────────── */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

/* ─── 商品条目 ─────────────────────────────────── */
.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  margin: 8px 12px;
  border-radius: 8px;
  padding: 12px 12px 12px 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, .06);
}

.item-check {
  flex-shrink: 0;
}

.item-pic {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  background: #f0f0f0;
}

/* 中间文字区（自适应宽度） */
.item-mid {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-size: 13px;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-attr {
  font-size: 11px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-price-row {
  margin-top: auto;
}

.item-price {
  font-size: 16px;
  font-weight: 700;
  color: #e4393c;
}

/* ─── 右侧：数量 + 删除 ──────────────────────── */
.item-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

/* 京东款数量条 */
.qty-bar {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  height: 28px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
  padding: 0;
  line-height: 1;
}

.qty-btn:hover:not(:disabled) {
  background: #e8e8e8;
}

.qty-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.qty-num {
  min-width: 32px;
  text-align: center;
  font-size: 14px;
  color: #333;
  border-left: 1px solid #e0e0e0;
  border-right: 1px solid #e0e0e0;
  height: 28px;
  line-height: 28px;
  display: inline-block;
}

/* 京东式删除文字按钮 */
.btn-delete {
  border: none;
  background: transparent;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 2px 0;
  transition: color 0.15s;
}

.btn-delete:hover:not(:disabled) {
  color: #e4393c;
}

.btn-delete:disabled {
  color: #ccc;
  cursor: not-allowed;
}

/* ─── 底部结算栏 ───────────────────────────────── */
.cart-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 100;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, .06);
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar-all {
  font-size: 14px;
  color: #333;
}

.bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-total {
  font-size: 13px;
  color: #666;
}

.bar-price {
  font-size: 18px;
  font-weight: 700;
  color: #e4393c;
}

.bar-btn {
  background: #e4393c;
  color: #fff;
  border: none;
  border-radius: 20px;
  height: 38px;
  padding: 0 20px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}

.bar-btn:hover {
  background: #c7272a;
}
</style>

