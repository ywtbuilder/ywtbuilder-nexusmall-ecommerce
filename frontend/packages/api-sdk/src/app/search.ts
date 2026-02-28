import type { ApiResult, PageResult, RequestConfig } from '../core/http'
import { appRequest } from '../core/http'

export interface EsProduct {
  id: number
  productSn?: string
  brandId?: number
  brandName?: string
  productCategoryId?: number
  productCategoryName?: string
  pic?: string
  name?: string
  subTitle?: string
  keywords?: string
  price?: number
  sale?: number
  newStatus?: number
  recommandStatus?: number
  stock?: number
  promotionType?: number
  sort?: number
}

export const searchApi = {
  search: (params: Record<string, unknown>, config?: RequestConfig) =>
    appRequest.get<ApiResult<PageResult<EsProduct>>>('/search/product', {
      params,
      ...config,
    }),
}

