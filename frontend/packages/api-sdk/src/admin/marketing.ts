import type { ApiResult, PageResult } from '../core/http'
import { adminRequest } from '../core/http'

/* =================== Coupon =================== */

export interface Coupon {
  id: number
  type?: number
  name?: string
  platform?: number
  count?: number
  amount?: number
  perLimit?: number
  minPoint?: number
  startTime?: string
  endTime?: string
  useType?: number
  note?: string
  publishCount?: number
  useCount?: number
  receiveCount?: number
  enableTime?: string
  code?: string
  memberLevel?: number
}

export interface CouponParam extends Coupon {
  productRelationList?: CouponProductRelation[]
  productCategoryRelationList?: CouponProductCategoryRelation[]
}

export interface CouponProductRelation {
  id?: number
  couponId?: number
  productId?: number
  productName?: string
  productSn?: string
}

export interface CouponProductCategoryRelation {
  id?: number
  couponId?: number
  productCategoryId?: number
  productCategoryName?: string
  parentCategoryName?: string
}

export interface CouponHistory {
  id: number
  couponId?: number
  memberId?: number
  orderId?: number
  couponCode?: string
  memberNickname?: string
  getType?: number
  useStatus?: number
  useTime?: string
  createTime?: string
}

export const couponApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<Coupon>>>('/coupon/list', { params }),

  create: (data: CouponParam) =>
    adminRequest.post<ApiResult<number>>('/coupon/create', data),

  update: (id: number, data: CouponParam) =>
    adminRequest.post<ApiResult<number>>(`/coupon/update/${id}`, data),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/coupon/delete/${id}`),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<CouponParam>>(`/coupon/${id}`),
}

export const couponHistoryApi = {
  list: (couponId: number, params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<CouponHistory>>>(`/couponHistory/list/${couponId}`, { params }),
}

/* =================== Flash =================== */

export interface FlashPromotion {
  id: number
  title?: string
  startDate?: string
  endDate?: string
  status?: number
  createTime?: string
}

export interface FlashPromotionSession {
  id: number
  name?: string
  startTime?: string
  endTime?: string
  status?: number
  createTime?: string
  productCount?: number
}

export interface FlashPromotionProductRelation {
  id: number
  flashPromotionId?: number
  flashPromotionSessionId?: number
  productId?: number
  flashPromotionPrice?: number
  flashPromotionCount?: number
  flashPromotionLimit?: number
  sort?: number
}

export const flashApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<FlashPromotion>>>('/flash/list', { params }),

  create: (data: FlashPromotion) =>
    adminRequest.post<ApiResult<number>>('/flash/create', data),

  update: (id: number, data: FlashPromotion) =>
    adminRequest.post<ApiResult<number>>(`/flash/update/${id}`, data),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/flash/delete/${id}`),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<FlashPromotion>>(`/flash/${id}`),

  updateStatus: (id: number, status: number) =>
    adminRequest.post<ApiResult<number>>('/flash/update/status', null, { params: { id, status } }),
}

export const flashSessionApi = {
  list: () =>
    adminRequest.get<ApiResult<FlashPromotionSession[]>>('/flashSession/list'),

  selectList: (flashPromotionId: number) =>
    adminRequest.get<ApiResult<FlashPromotionSession[]>>(`/flashSession/selectList/${flashPromotionId}`),

  create: (data: FlashPromotionSession) =>
    adminRequest.post<ApiResult<number>>('/flashSession/create', data),

  update: (id: number, data: FlashPromotionSession) =>
    adminRequest.post<ApiResult<number>>(`/flashSession/update/${id}`, data),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/flashSession/delete/${id}`),

  updateStatus: (id: number, status: number) =>
    adminRequest.post<ApiResult<number>>('/flashSession/update/status', null, { params: { id, status } }),
}

export const flashProductRelationApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<FlashPromotionProductRelation>>>('/flashProductRelation/list', { params }),

  create: (data: FlashPromotionProductRelation[]) =>
    adminRequest.post<ApiResult<number>>('/flashProductRelation/create', data),

  update: (id: number, data: FlashPromotionProductRelation) =>
    adminRequest.post<ApiResult<number>>(`/flashProductRelation/update/${id}`, data),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/flashProductRelation/delete/${id}`),
}

/* =================== Advertise =================== */

export interface HomeAdvertise {
  id: number
  name?: string
  type?: number
  pic?: string
  startTime?: string
  endTime?: string
  status?: number
  clickCount?: number
  orderCount?: number
  url?: string
  note?: string
  sort?: number
}

export const advertiseApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<HomeAdvertise>>>('/home/advertise/list', { params }),

  create: (data: HomeAdvertise) =>
    adminRequest.post<ApiResult<number>>('/home/advertise/create', data),

  update: (id: number, data: HomeAdvertise) =>
    adminRequest.post<ApiResult<number>>(`/home/advertise/update/${id}`, data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/home/advertise/delete', null, { params: { ids: ids.join(',') } }),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<HomeAdvertise>>(`/home/advertise/${id}`),

  updateStatus: (id: number, status: number) =>
    adminRequest.post<ApiResult<number>>('/home/advertise/update/status', null, { params: { id, status } }),
}

/* =================== Home Recommend =================== */

export interface HomeBrand {
  id: number
  brandId?: number
  brandName?: string
  recommendStatus?: number
  sort?: number
}

export interface HomeNewProduct {
  id: number
  productId?: number
  productName?: string
  recommendStatus?: number
  sort?: number
}

export interface HomeRecommendProduct {
  id: number
  productId?: number
  productName?: string
  recommendStatus?: number
  sort?: number
}

export interface HomeRecommendSubject {
  id: number
  subjectId?: number
  subjectName?: string
  recommendStatus?: number
  sort?: number
}

export const homeBrandApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<HomeBrand>>>('/home/brand/list', { params }),

  create: (data: HomeBrand[]) =>
    adminRequest.post<ApiResult<number>>('/home/brand/create', data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/home/brand/delete', null, { params: { ids: ids.join(',') } }),

  updateRecommendStatus: (ids: number[], recommendStatus: number) =>
    adminRequest.post<ApiResult<number>>('/home/brand/update/recommendStatus', null, { params: { ids: ids.join(','), recommendStatus } }),

  updateSort: (id: number, sort: number) =>
    adminRequest.post<ApiResult<number>>('/home/brand/update/sort', null, { params: { id, sort } }),
}

export const homeNewProductApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<HomeNewProduct>>>('/home/newProduct/list', { params }),

  create: (data: HomeNewProduct[]) =>
    adminRequest.post<ApiResult<number>>('/home/newProduct/create', data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/home/newProduct/delete', null, { params: { ids: ids.join(',') } }),

  updateRecommendStatus: (ids: number[], recommendStatus: number) =>
    adminRequest.post<ApiResult<number>>('/home/newProduct/update/recommendStatus', null, { params: { ids: ids.join(','), recommendStatus } }),

  updateSort: (id: number, sort: number) =>
    adminRequest.post<ApiResult<number>>('/home/newProduct/update/sort', null, { params: { id, sort } }),
}

export const homeRecommendProductApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<HomeRecommendProduct>>>('/home/recommendProduct/list', { params }),

  create: (data: HomeRecommendProduct[]) =>
    adminRequest.post<ApiResult<number>>('/home/recommendProduct/create', data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/home/recommendProduct/delete', null, { params: { ids: ids.join(',') } }),

  updateRecommendStatus: (ids: number[], recommendStatus: number) =>
    adminRequest.post<ApiResult<number>>('/home/recommendProduct/update/recommendStatus', null, { params: { ids: ids.join(','), recommendStatus } }),

  updateSort: (id: number, sort: number) =>
    adminRequest.post<ApiResult<number>>('/home/recommendProduct/update/sort', null, { params: { id, sort } }),
}

export const homeRecommendSubjectApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<HomeRecommendSubject>>>('/home/recommendSubject/list', { params }),

  create: (data: HomeRecommendSubject[]) =>
    adminRequest.post<ApiResult<number>>('/home/recommendSubject/create', data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/home/recommendSubject/delete', null, { params: { ids: ids.join(',') } }),

  updateRecommendStatus: (ids: number[], recommendStatus: number) =>
    adminRequest.post<ApiResult<number>>('/home/recommendSubject/update/recommendStatus', null, { params: { ids: ids.join(','), recommendStatus } }),

  updateSort: (id: number, sort: number) =>
    adminRequest.post<ApiResult<number>>('/home/recommendSubject/update/sort', null, { params: { id, sort } }),
}

