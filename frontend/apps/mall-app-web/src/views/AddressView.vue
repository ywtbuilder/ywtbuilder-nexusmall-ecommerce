<template>
  <div class="address-page">
    <van-nav-bar title="收货地址" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="plus" size="20" @click="openForm()" />
      </template>
    </van-nav-bar>
    <van-address-list v-if="list.length" :list="vanList" default-tag-text="默认"
      @edit="onEdit" @select="onSelect" />
    <van-empty v-else description="暂无收货地址" />

    <!-- 编辑弹出 -->
    <van-popup v-model:show="showForm" position="bottom" round style="height:80%">
      <van-nav-bar :title="editId ? '编辑地址' : '新增地址'" left-text="取消" right-text="保存"
        @click-left="showForm = false" @click-right="saveAddress" />
      <van-cell-group inset style="margin-top:12px">
        <van-field v-model="form.name" label="姓名" placeholder="请输入姓名" />
        <van-field v-model="form.phoneNumber" label="手机号" placeholder="请输入手机号" />
        <van-field v-model="form.province" label="省" placeholder="省" />
        <van-field v-model="form.city" label="市" placeholder="市" />
        <van-field v-model="form.region" label="区" placeholder="区" />
        <van-field v-model="form.detailAddress" label="详细地址" placeholder="请输入详细地址" />
        <van-cell title="默认地址">
          <template #right-icon>
            <van-switch v-model="isDefault" />
          </template>
        </van-cell>
      </van-cell-group>
      <div style="padding:16px">
        <van-button v-if="editId" type="danger" block plain @click="deleteAddress">删除地址</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { NavBar as VanNavBar, AddressList as VanAddressList, Popup as VanPopup,
  CellGroup as VanCellGroup, Cell as VanCell, Field as VanField, Switch as VanSwitch,
  Button as VanButton, Icon as VanIcon, Empty as VanEmpty, showToast, showConfirmDialog
} from 'vant'
import { memberAddressApi } from '@mall/api-sdk/app'
import type { MemberAddress } from '@mall/api-sdk/app/member'

const list = ref<MemberAddress[]>([])
const showForm = ref(false)
const editId = ref<number | null>(null)
const isDefault = ref(false)
const form = reactive({ name: '', phoneNumber: '', province: '', city: '', region: '', detailAddress: '' })

const vanList = computed(() => list.value.map(a => ({
  id: a.id,
  name: a.name || '',
  tel: a.phoneNumber || '',
  address: `${a.province || ''}${a.city || ''}${a.region || ''}${a.detailAddress || ''}`,
  isDefault: a.defaultStatus === 1
})))

async function load() {
  const res = await memberAddressApi.list()
  list.value = res.data.data || []
}

function openForm(addr?: MemberAddress) {
  if (addr) {
    editId.value = addr.id!
    Object.assign(form, { name: addr.name, phoneNumber: addr.phoneNumber, province: addr.province, city: addr.city, region: addr.region, detailAddress: addr.detailAddress })
    isDefault.value = addr.defaultStatus === 1
  } else {
    editId.value = null
    Object.assign(form, { name: '', phoneNumber: '', province: '', city: '', region: '', detailAddress: '' })
    isDefault.value = false
  }
  showForm.value = true
}

function onEdit(item: { id: number }) {
  const addr = list.value.find(a => a.id === item.id)
  if (addr) openForm(addr)
}

function onSelect(item: { id: number }) {
  onEdit(item)
}

async function saveAddress() {
  const data = { ...form, defaultStatus: isDefault.value ? 1 : 0 } as unknown as MemberAddress
  if (editId.value) {
    await memberAddressApi.update(editId.value, data)
  } else {
    await memberAddressApi.add(data)
  }
  showToast('保存成功')
  showForm.value = false
  load()
}

async function deleteAddress() {
  await showConfirmDialog({ title: '确认删除该地址?' })
  await memberAddressApi.delete(editId.value!)
  showToast('已删除')
  showForm.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.address-page { min-height: 100vh; background: var(--t-bg); }
</style>

