import type { ApiResult, PageResult } from '../core/http'
import { appRequest } from '../core/http'

export interface HomeContent {
  advertiseList?: HomeAdvertise[]
  brandList?: HomeBrand[]
  newProductList?: HomeProduct[]
  hotProductList?: HomeProduct[]
  subjectList?: HomeSubject[]
}

export interface HomeContentLite {
  advertiseList?: HomeAdvertise[]
}

export interface HomeAdvertise {
  id: number
  name?: string
  type?: number
  status?: number
  pic?: string
  url?: string
  note?: string
  sort?: number
}

export interface HomeBrand {
  id: number
  name?: string
  logo?: string
  bigPic?: string
}

export interface HomeProduct {
  id: number
  name?: string
  pic?: string
  price?: number
  subTitle?: string
  sale?: number
}

export interface HomeSubject {
  id: number
  title?: string
  pic?: string
  categoryName?: string
}

export interface ProductCategory {
  id: number
  name?: string
  parentId?: number
  level?: number
  icon?: string
  children?: ProductCategory[]
}

export const homeApi = {
  content: () =>
    appRequest.get<ApiResult<HomeContent>>('/home/content'),

  contentLite: () =>
    appRequest.get<ApiResult<HomeContentLite>>('/home/content-lite'),

  productCateList: (parentId: number) =>
    appRequest.get<ApiResult<PageResult<ProductCategory> | ProductCategory[]>>(`/home/productCateList/${parentId}`),

  recommendProductList: (params: Record<string, unknown>) =>
    appRequest.get<ApiResult<HomeProduct[]>>('/home/recommendProductList', { params }),

  hotProductList: (params: Record<string, unknown>) =>
    appRequest.get<ApiResult<HomeProduct[]>>('/home/hotProductList', { params }),

  newProductList: (params: Record<string, unknown>) =>
    appRequest.get<ApiResult<HomeProduct[]>>('/home/newProductList', { params }),

  subjectList: (params: Record<string, unknown>) =>
    appRequest.get<ApiResult<HomeSubject[]>>('/home/subjectList', { params }),
}

