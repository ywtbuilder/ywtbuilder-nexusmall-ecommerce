import type { ApiResult } from '../core/http'
import { appRequest } from '../core/http'

export interface CartItem {
  id: number
  productId?: number
  productSkuId?: number
  memberId?: number
  quantity?: number
  price?: number
  productPic?: string
  productName?: string
  productSubTitle?: string
  productSkuCode?: string
  productCategoryId?: number
  productBrand?: string
  productSn?: string
  productAttr?: string
  deleteStatus?: number
  createDate?: string
  modifyDate?: string
}

export interface CartPromotionItem extends CartItem {
  promotionMessage?: string
  reduceAmount?: number
  realStock?: number
  integration?: number
  growth?: number
}

export const cartApi = {
  add: (data: CartItem) =>
    appRequest.post<ApiResult<number>>('/cart/add', data),

  list: () =>
    appRequest.get<ApiResult<CartItem[]>>('/cart/list'),

  listPromotion: () =>
    appRequest.get<ApiResult<CartPromotionItem[]>>('/cart/list/promotion'),

  updateQuantity: (id: number, quantity: number) =>
    appRequest.get<ApiResult<number>>('/cart/update/quantity', { params: { id, quantity } }),

  getProduct: (productId: number) =>
    appRequest.get<ApiResult<CartItem>>(`/cart/getProduct/${productId}`),

  updateAttr: (data: CartItem) =>
    appRequest.post<ApiResult<number>>('/cart/update/attr', data),

  delete: (ids: number[]) =>
    appRequest.post<ApiResult<number>>('/cart/delete', null, { params: { ids: ids.join(',') } }),

  clear: () =>
    appRequest.post<ApiResult<number>>('/cart/clear'),
}

