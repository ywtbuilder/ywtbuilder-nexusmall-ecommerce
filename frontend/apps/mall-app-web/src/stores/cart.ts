import { defineStore } from 'pinia'
import { ref } from 'vue'
import { cartApi } from '@mall/api-sdk/app'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  async function fetchCount() {
    if (!localStorage.getItem('token')) {
      count.value = 0
      return
    }
    try {
      const { data } = await cartApi.list()
      const list = Array.isArray(data.data) ? data.data : []
      count.value = list.reduce((sum, item) => sum + (item.quantity ?? 1), 0)
    } catch {
      count.value = 0
    }
  }

  function setCount(n: number) {
    count.value = n
  }

  return { count, fetchCount, setCount }
})

