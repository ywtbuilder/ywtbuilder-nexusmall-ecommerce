import type { ApiResult } from '../core/http'
import { appRequest } from '../core/http'

export interface ProductDetail {
  product?: ProductInfo
  brand?: BrandInfo
  productAttributeList?: ProductAttrInfo[]
  productAttributeValueList?: ProductAttrValueInfo[]
  skuStockList?: SkuStockInfo[]
  specList?: ProductSpecInfo[]
  introImageUrls?: string[]
  detailImageUrls?: string[]
  couponList?: CouponInfo[]
}

export interface ProductInfo {
  id: number
  brandId?: number
  productCategoryId?: number
  name?: string
  pic?: string
  productSn?: string
  subTitle?: string
  description?: string
  originalPrice?: number
  price?: number
  sale?: number
  stock?: number
  unit?: string
  weight?: number
  albumPics?: string
  detailTitle?: string
  detailDesc?: string
  detailHtml?: string
  detailMobileHtml?: string
  introImageUrls?: string[]
  detailImageUrls?: string[]
  specList?: ProductSpecInfo[]
}

export interface BrandInfo {
  id: number
  name?: string
  logo?: string
}

export interface ProductAttrInfo {
  id: number
  name?: string
  type?: number
  inputType?: number
  inputList?: string
  selectType?: number
  filterType?: number
}

export interface ProductAttrValueInfo {
  id: number
  productId?: number
  productAttributeId?: number
  value?: string
}

export interface ProductSpecInfo {
  id?: number
  productId?: number
  specGroup?: string
  specName?: string
  specValue?: string
  sortOrder?: number
}

export interface SkuStockInfo {
  id: number
  productId?: number
  skuCode?: string
  price?: number
  stock?: number
  sp1?: string
  sp2?: string
  sp3?: string
  spData?: string
  pic?: string
}

export interface CouponInfo {
  id: number
  name?: string
  amount?: number
  minPoint?: number
  startTime?: string
  endTime?: string
}

export const productApi = {
  detail: (id: number) =>
    appRequest.get<ApiResult<ProductDetail>>(`/product/detail/${id}`),

  categoryTreeList: () =>
    appRequest.get<ApiResult<unknown[]>>('/product/categoryTreeList'),

  search: (params: Record<string, unknown>) =>
    appRequest.get<ApiResult<unknown>>('/product/search', { params }),
}

