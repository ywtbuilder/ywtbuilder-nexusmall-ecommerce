import type { ApiResult, PageResult } from '../core/http'
import { adminRequest } from '../core/http'

export interface Order {
  id: number
  memberId?: number
  orderSn?: string
  memberUsername?: string
  totalAmount?: number
  payAmount?: number
  freightAmount?: number
  payType?: number
  sourceType?: number
  status?: number
  orderType?: number
  deliveryCompany?: string
  deliverySn?: string
  receiverName?: string
  receiverPhone?: string
  receiverProvince?: string
  receiverCity?: string
  receiverRegion?: string
  receiverDetailAddress?: string
  note?: string
  confirmStatus?: number
  deleteStatus?: number
  createTime?: string
  paymentTime?: string
  deliveryTime?: string
  receiveTime?: string
  modifyTime?: string
}

export interface OrderItem {
  id: number
  orderId?: number
  productId?: number
  productName?: string
  productPic?: string
  productPrice?: number
  productQuantity?: number
  productSkuCode?: string
  productAttr?: string
}

export interface OrderDetail extends Order {
  orderItemList?: OrderItem[]
}

export interface OrderDeliveryParam {
  orderId: number
  deliveryCompany: string
  deliverySn: string
}

export interface ReturnApply {
  id: number
  orderId?: number
  memberUsername?: string
  productPic?: string
  productName?: string
  productPrice?: number
  productCount?: number
  reason?: string
  description?: string
  status?: number
  handleNote?: string
  handleMan?: string
  createTime?: string
  handleTime?: string
}

export interface OrderReturnReason {
  id: number
  name?: string
  sort?: number
  status?: number
  createTime?: string
}

export interface OrderSetting {
  id: number
  flashOrderOvertime?: number
  normalOrderOvertime?: number
  confirmOvertime?: number
  finishOvertime?: number
  commentOvertime?: number
}

export interface CompanyAddress {
  id: number
  addressName?: string
  sendStatus?: number
  receiveStatus?: number
  name?: string
  phone?: string
  province?: string
  city?: string
  region?: string
  detailAddress?: string
}

export const orderApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<Order>>>('/order/list', { params }),

  detail: (id: number) =>
    adminRequest.get<ApiResult<OrderDetail>>(`/order/${id}`),

  delivery: (data: OrderDeliveryParam[]) =>
    adminRequest.post<ApiResult<number>>('/order/update/delivery', data),

  close: (ids: number[], note: string) =>
    adminRequest.post<ApiResult<number>>('/order/update/close', null, { params: { ids: ids.join(','), note } }),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/order/delete', null, { params: { ids: ids.join(',') } }),

  updateReceiverInfo: (data: Record<string, unknown>) =>
    adminRequest.post<ApiResult<number>>('/order/update/receiverInfo', data),

  updateMoneyInfo: (data: Record<string, unknown>) =>
    adminRequest.post<ApiResult<number>>('/order/update/moneyInfo', data),

  updateNote: (id: number, note: string, status: number) =>
    adminRequest.post<ApiResult<number>>('/order/update/note', null, { params: { id, note, status } }),
}

export const returnApplyApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<ReturnApply>>>('/returnApply/list', { params }),

  detail: (id: number) =>
    adminRequest.get<ApiResult<ReturnApply>>(`/returnApply/${id}`),

  updateStatus: (id: number, data: Record<string, unknown>) =>
    adminRequest.post<ApiResult<number>>(`/returnApply/update/status/${id}`, data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/returnApply/delete', null, { params: { ids: ids.join(',') } }),
}

export const returnReasonApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<OrderReturnReason>>>('/returnReason/list', { params }),

  create: (data: OrderReturnReason) =>
    adminRequest.post<ApiResult<number>>('/returnReason/create', data),

  update: (id: number, data: OrderReturnReason) =>
    adminRequest.post<ApiResult<number>>(`/returnReason/update/${id}`, data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/returnReason/delete', null, { params: { ids: ids.join(',') } }),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<OrderReturnReason>>(`/returnReason/${id}`),

  updateStatus: (ids: number[], status: number) =>
    adminRequest.post<ApiResult<number>>('/returnReason/update/status', null, { params: { ids: ids.join(','), status } }),
}

export const orderSettingApi = {
  getItem: (id: number) =>
    adminRequest.get<ApiResult<OrderSetting>>(`/orderSetting/${id}`),

  update: (id: number, data: OrderSetting) =>
    adminRequest.post<ApiResult<number>>(`/orderSetting/update/${id}`, data),
}

export const companyAddressApi = {
  list: () =>
    adminRequest.get<ApiResult<CompanyAddress[]>>('/companyAddress/list'),
}

