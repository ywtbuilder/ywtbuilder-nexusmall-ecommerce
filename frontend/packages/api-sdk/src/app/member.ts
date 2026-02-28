import type { ApiResult, PageResult } from '../core/http'
import { appRequest } from '../core/http'

/* =================== Address =================== */

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

export const memberAddressApi = {
  list: () =>
    appRequest.get<ApiResult<MemberAddress[]>>('/member/address/list'),

  getItem: (id: number) =>
    appRequest.get<ApiResult<MemberAddress>>(`/member/address/${id}`),

  add: (data: MemberAddress) =>
    appRequest.post<ApiResult<number>>('/member/address/add', data),

  update: (id: number, data: MemberAddress) =>
    appRequest.post<ApiResult<number>>(`/member/address/update/${id}`, data),

  delete: (id: number) =>
    appRequest.post<ApiResult<number>>(`/member/address/delete/${id}`),
}

/* =================== Read History =================== */

export interface MemberReadHistory {
  id?: string
  memberId?: number
  memberNickname?: string
  memberIcon?: string
  productId?: number
  productName?: string
  productPic?: string
  productSubTitle?: string
  productPrice?: string
  createTime?: string
}

export const memberReadHistoryApi = {
  list: (pageNum: number, pageSize: number) =>
    appRequest.get<ApiResult<PageResult<MemberReadHistory> | MemberReadHistory[]>>('/member/readHistory/list', { params: { pageNum, pageSize } }),

  create: (data: MemberReadHistory) =>
    appRequest.post<ApiResult<number>>('/member/readHistory/create', data),

  delete: (ids: string[]) =>
    appRequest.post<ApiResult<number>>('/member/readHistory/delete', null, { params: { ids: ids.join(',') } }),

  clear: () =>
    appRequest.post<ApiResult<null>>('/member/readHistory/clear'),
}

/* =================== Product Collection =================== */

export interface MemberProductCollection {
  id?: string
  memberId?: number
  memberNickname?: string
  memberIcon?: string
  productId?: number
  productName?: string
  productPic?: string
  productSubTitle?: string
  productPrice?: string
  createTime?: string
}

export const memberCollectionApi = {
  list: (pageNum: number, pageSize: number) =>
    appRequest.get<ApiResult<PageResult<MemberProductCollection> | MemberProductCollection[]>>('/member/productCollection/list', { params: { pageNum, pageSize } }),

  add: (data: MemberProductCollection) =>
    appRequest.post<ApiResult<number>>('/member/productCollection/add', data),

  delete: (productId: number) =>
    appRequest.post<ApiResult<number>>('/member/productCollection/delete', null, { params: { productId } }),

  detail: (productId: number) =>
    appRequest.get<ApiResult<MemberProductCollection>>('/member/productCollection/detail', { params: { productId } }),
}

/* =================== Brand Attention =================== */

export interface MemberBrandAttention {
  id?: string
  memberId?: number
  memberNickname?: string
  memberIcon?: string
  brandId?: number
  brandName?: string
  brandLogo?: string
  createTime?: string
}

export const memberAttentionApi = {
  list: (pageNum: number, pageSize: number) =>
    appRequest.get<ApiResult<PageResult<MemberBrandAttention> | MemberBrandAttention[]>>('/member/attention/list', { params: { pageNum, pageSize } }),

  add: (data: MemberBrandAttention) =>
    appRequest.post<ApiResult<number>>('/member/attention/add', data),

  delete: (brandId: number) =>
    appRequest.post<ApiResult<number>>('/member/attention/delete', null, { params: { brandId } }),

  detail: (brandId: number) =>
    appRequest.get<ApiResult<MemberBrandAttention>>('/member/attention/detail', { params: { brandId } }),
}

/* =================== Member Coupon =================== */

export interface MemberCoupon {
  id: number
  couponId?: number
  memberId?: number
  couponCode?: string
  memberNickname?: string
  getType?: number
  useStatus?: number
  useTime?: string
  orderSn?: string
  createTime?: string
  couponName?: string
  couponAmount?: number
  couponMinPoint?: number
}

export const memberCouponApi = {
  list: (useStatus: number) =>
    appRequest.get<ApiResult<MemberCoupon[]>>('/member/coupon/list', { params: { useStatus } }),

  listCart: (type: number) =>
    appRequest.get<ApiResult<MemberCoupon[]>>(`/member/coupon/list/cart/${type}`),

  add: (couponId: number) =>
    appRequest.post<ApiResult<null>>(`/member/coupon/add/${couponId}`),

  listByProduct: (productId: number) =>
    appRequest.get<ApiResult<MemberCoupon[]>>(`/member/coupon/listByProduct/${productId}`),
}

/* =================== Return Apply =================== */

export interface ReturnApplyParam {
  orderId: number
  productId?: number
  orderSn?: string
  memberUsername?: string
  productPic?: string
  productName?: string
  productBrand?: string
  productPrice?: number
  productCount?: number
  productRealPrice?: number
  productAttr?: string
  reason?: string
  description?: string
  proofPics?: string
  returnName?: string
  returnPhone?: string
}

export const returnApplyApi = {
  create: (data: ReturnApplyParam) =>
    appRequest.post<ApiResult<number>>('/returnApply/create', data),
}

/* =================== User Center Summary =================== */

export interface UserCenterStats {
  couponCount: number
  followCount: number
  favCount: number
  footprintCount: number
}

export interface UserCenterOrderCounts {
  pendingPay: number
  pendingShip: number
  pendingReceive: number
  pendingReview: number
  afterSale: number
}

export interface UserCenterSummary {
  user: {
    id: number
    nickname: string
    icon: string
    phone: string
    memberLevel: string
  }
  stats: UserCenterStats
  orderCounts: UserCenterOrderCounts
  cartCount: number
}

export const userCenterApi = {
  summary: () =>
    appRequest.get<ApiResult<UserCenterSummary>>('/user/center/summary'),
}


