import type { ApiResult, PageResult } from '../core/http'
import { appRequest } from '../core/http'

export interface ConfirmOrder {
  cartPromotionItemList?: CartPromotionItem[]
  memberReceiveAddressList?: MemberAddress[]
  couponHistoryDetailList?: CouponHistoryDetail[]
  integrationConsumeSetting?: IntegrationConsumeSetting
  memberIntegration?: number
  calcAmount?: CalcAmount
}

export interface CartPromotionItem {
  id: number
  productId?: number
  productName?: string
  productPic?: string
  quantity?: number
  price?: number
  productAttr?: string
  reduceAmount?: number
}

export interface MemberAddress {
  id: number
  memberId?: number
  name?: string
  phoneNumber?: string
  defaultStatus?: number
  postCode?: string
  province?: string
  city?: string
  region?: string
  detailAddress?: string
}

export interface CouponHistoryDetail {
  id: number
  couponId?: number
  couponName?: string
  couponAmount?: number
}

export interface IntegrationConsumeSetting {
  useUnit?: number
  couponStatus?: number
}

export interface CalcAmount {
  totalAmount?: number
  freightAmount?: number
  promotionAmount?: number
  payAmount?: number
}

export interface OrderParam {
  memberReceiveAddressId?: number
  receiverName?: string
  receiverPhone?: string
  receiverDetailAddress?: string
  note?: string
  couponId?: number
  useIntegration?: number
  payType?: number
  cartIds?: number[]
}

export interface OrderDetail {
  id: number
  orderSn?: string
  memberId?: number
  totalAmount?: number
  payAmount?: number
  freightAmount?: number
  payType?: number
  status?: number
  orderType?: number
  deliveryCompany?: string
  deliverySn?: string
  receiverName?: string
  receiverPhone?: string
  receiverDetailAddress?: string
  note?: string
  createTime?: string
  paymentTime?: string
  deliveryTime?: string
  receiveTime?: string
  orderItemList?: OrderItem[]
}

export interface OrderItem {
  id: number
  productId?: number
  productName?: string
  productPic?: string
  productPrice?: number
  productQuantity?: number
  productAttr?: string
}

export const portalOrderApi = {
  /** 确认下单页数据（支持可选 cartIds 过滤） */
  generateConfirmOrder: (cartIds?: number[]) =>
    appRequest.get<ApiResult<ConfirmOrder>>('/order/confirm', {
      params: cartIds?.length ? { cartIds: cartIds.join(',') } : undefined,
    }),

  generateOrder: (data: OrderParam) =>
    appRequest.post<ApiResult<OrderDetail>>('/order/generateOrder', data),

  paySuccess: (orderId: number, payType: number) =>
    appRequest.post<ApiResult<number>>('/order/paySuccess', null, { params: { orderId, payType } }),

  cancelUserOrder: (orderId: number) =>
    appRequest.post<ApiResult<null>>('/order/cancelUserOrder', null, { params: { orderId } }),

  list: (params: Record<string, unknown>) =>
    appRequest.get<ApiResult<PageResult<OrderDetail>>>('/order/list', { params }),

  detail: (orderId: number) =>
    appRequest.get<ApiResult<OrderDetail>>(`/order/detail/${orderId}`),

  confirmReceiveOrder: (orderId: number) =>
    appRequest.post<ApiResult<null>>('/order/confirmReceiveOrder', null, { params: { orderId } }),

  deleteOrder: (orderId: number) =>
    appRequest.post<ApiResult<null>>('/order/deleteOrder', null, { params: { orderId } }),
}

