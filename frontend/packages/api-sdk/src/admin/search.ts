import type { ApiResult, PageResult } from '../core/http'
import { adminRequest } from '../core/http'

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
  attrValueList?: EsProductAttributeValue[]
}

export interface EsProductAttributeValue {
  id?: number
  productAttributeId?: number
  value?: string
  type?: number
  name?: string
}

export const esProductApi = {
  importAll: () =>
    adminRequest.post<ApiResult<number>>('/esProduct/importAll'),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/esProduct/delete/${id}`),

  create: (id: number) =>
    adminRequest.post<ApiResult<EsProduct>>(`/esProduct/create/${id}`),

  search: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<EsProduct>>>('/esProduct/search/simple', { params }),
}

