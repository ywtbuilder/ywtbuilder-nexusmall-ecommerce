<template>
  <div class="order-confirm">
    <van-nav-bar title="确认订单" left-arrow @click-left="$router.back()" />

    <template v-if="confirmData">
      <!-- 收货地址 -->
      <van-contact-card
        v-if="selectedAddress"
        type="edit"
        :name="selectedAddress.name"
        :tel="selectedAddress.phoneNumber"
        @click="showAddressPicker = true"
      />
      <van-contact-card v-else type="add" add-text="选择收货地址" @click="showAddressPicker = true" />

      <!-- 商品列表 -->
      <van-card
        v-for="item in confirmData.cartPromotionItemList"
        :key="item.id"
        :num="item.quantity"
        :price="String(item.price ?? 0)"
        :title="item.productName"
        :thumb="item.productPic"
      />

      <!-- 金额明细 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-cell title="商品合计" :value="`¥${confirmData.calcAmount?.totalAmount ?? 0}`" />
        <van-cell title="运费" :value="`¥${confirmData.calcAmount?.freightAmount ?? 0}`" />
        <van-cell title="优惠" :value="`-¥${confirmData.calcAmount?.promotionAmount ?? 0}`" />
      </van-cell-group>

      <!-- 备注 -->
      <van-cell-group inset style="margin-top: 12px">
        <van-field v-model="note" label="备注" placeholder="选填" />
      </van-cell-group>
    </template>

    <van-skeleton v-else :row="6" style="padding: 16px" />

    <van-submit-bar
      :price="(confirmData?.calcAmount?.payAmount ?? 0) * 100"
      button-text="提交订单"
      @submit="submitOrder"
      :loading="submitting"
    />

    <!-- 地址选择弹窗 -->
    <van-popup v-model:show="showAddressPicker" round position="bottom" style="max-height: 60vh; padding: 16px;">
      <div v-for="addr in confirmData?.memberReceiveAddressList" :key="addr.id"
        :class="['address-item', { active: selectedAddress?.id === addr.id }]"
        @click="selectedAddress = addr; showAddressPicker = false"
      >
        <div><strong>{{ addr.name }}</strong> {{ addr.phoneNumber }}</div>
        <div style="color: #666; font-size: 13px">{{ addr.province }}{{ addr.city }}{{ addr.region }}{{ addr.detailAddress }}</div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NavBar as VanNavBar, ContactCard as VanContactCard, Card as VanCard,
  CellGroup as VanCellGroup, Cell as VanCell, Field as VanField,
  SubmitBar as VanSubmitBar, Skeleton as VanSkeleton, Popup as VanPopup, showToast,
} from 'vant'
import { portalOrderApi } from '@mall/api-sdk/app'
import type { ConfirmOrder, MemberAddress } from '@mall/api-sdk/app/order'

const route = useRoute()
const router = useRouter()
const confirmData = ref<ConfirmOrder | null>(null)
const selectedAddress = ref<MemberAddress | null>(null)
const note = ref('')
const submitting = ref(false)
const showAddressPicker = ref(false)

onMounted(async () => {
  const cartIds = (route.query.cartIds as string)?.split(',').map(Number) ?? []
  if (!cartIds.length) { showToast('无商品信息'); router.back(); return }
  try {
    const { data } = await portalOrderApi.generateConfirmOrder(cartIds)
    confirmData.value = data.data
    // 默认选中第一个地址
    const defaultAddr = data.data.memberReceiveAddressList?.find(a => a.defaultStatus === 1)
    selectedAddress.value = defaultAddr ?? data.data.memberReceiveAddressList?.[0] ?? null
  } catch { showToast('加载失败') }
})

async function submitOrder() {
  if (!selectedAddress.value) { showToast('请选择收货地址'); return }
  submitting.value = true
  try {
    const cartIds = (route.query.cartIds as string)?.split(',').map(Number) ?? []
    await portalOrderApi.generateOrder({
      memberReceiveAddressId: selectedAddress.value.id,
      payType: 0,
      cartIds,
      receiverName: selectedAddress.value.name,
      receiverPhone: selectedAddress.value.phoneNumber,
      receiverDetailAddress: `${selectedAddress.value.province ?? ''}${selectedAddress.value.city ?? ''}${selectedAddress.value.region ?? ''}${selectedAddress.value.detailAddress ?? ''}`,
      note: note.value,
    })
    showToast('下单成功')
    router.replace('/order/list')
  } catch (e: unknown) {
    showToast((e as Error).message || '下单失败')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.order-confirm { padding-bottom: 60px; min-height: 100vh; background: var(--t-bg); }
.address-item {
  padding: 14px;
  border-bottom: 1px solid var(--t-border-light);
  cursor: pointer;
  transition: background var(--t-dur) var(--t-ease);
}
.address-item.active {
  background: var(--t-primary-bg);
}
</style>

