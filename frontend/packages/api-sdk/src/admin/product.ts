import type { ApiResult, PageResult } from '../core/http'
import { adminRequest } from '../core/http'

export interface Product {
  id: number
  brandId?: number
  productCategoryId?: number
  name: string
  pic?: string
  productSn?: string
  publishStatus?: number
  newStatus?: number
  recommandStatus?: number
  verifyStatus?: number
  sort?: number
  price?: number
  subTitle?: string
  description?: string
  originalPrice?: number
  stock?: number
  unit?: string
  weight?: number
  keywords?: string
  note?: string
  brandName?: string
  productCategoryName?: string
  sale?: number
}

export interface ProductParam extends Product {
  productLadderList?: unknown[]
  productFullReductionList?: unknown[]
  memberPriceList?: unknown[]
  skuStockList?: unknown[]
  productAttributeValueList?: unknown[]
}

export interface ProductCategory {
  id: number
  parentId?: number
  name: string
  level?: number
  productCount?: number
  productUnit?: string
  navStatus?: number
  showStatus?: number
  sort?: number
  icon?: string
  keywords?: string
  description?: string
  children?: ProductCategory[]
}

export interface ProductAttribute {
  id: number
  productAttributeCategoryId?: number
  name: string
  selectType?: number
  inputType?: number
  inputList?: string
  sort?: number
  filterType?: number
  searchType?: number
  relatedStatus?: number
  handAddStatus?: number
  type?: number
}

export interface ProductAttributeCategory {
  id: number
  name: string
  attributeCount?: number
  paramCount?: number
  productAttributeList?: ProductAttribute[]
}

export interface Brand {
  id: number
  name: string
  firstLetter?: string
  sort?: number
  factoryStatus?: number
  showStatus?: number
  productCount?: number
  productCommentCount?: number
  logo?: string
  bigPic?: string
  brandStory?: string
}

export interface SkuStock {
  id?: number
  productId?: number
  skuCode?: string
  price?: number
  stock?: number
  lowStock?: number
  pic?: string
  sale?: number
  spData?: string
}

export const productApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<Product>>>('/product/list', { params }),

  create: (data: ProductParam) =>
    adminRequest.post<ApiResult<number>>('/product/create', data),

  update: (id: number, data: ProductParam) =>
    adminRequest.post<ApiResult<number>>(`/product/update/${id}`, data),

  getUpdateInfo: (id: number) =>
    adminRequest.get<ApiResult<ProductParam>>(`/product/updateInfo/${id}`),

  updatePublishStatus: (ids: number[], publishStatus: number) =>
    adminRequest.post<ApiResult<number>>('/product/update/publishStatus', null, { params: { ids: ids.join(','), publishStatus } }),

  updateRecommendStatus: (ids: number[], recommendStatus: number) =>
    adminRequest.post<ApiResult<number>>('/product/update/recommendStatus', null, { params: { ids: ids.join(','), recommendStatus } }),

  updateNewStatus: (ids: number[], newStatus: number) =>
    adminRequest.post<ApiResult<number>>('/product/update/newStatus', null, { params: { ids: ids.join(','), newStatus } }),

  updateDeleteStatus: (ids: number[], deleteStatus: number) =>
    adminRequest.post<ApiResult<number>>('/product/update/deleteStatus', null, { params: { ids: ids.join(','), deleteStatus } }),

  updateVerifyStatus: (ids: number[], verifyStatus: number, detail: string) =>
    adminRequest.post<ApiResult<number>>('/product/update/verifyStatus', null, { params: { ids: ids.join(','), verifyStatus, detail } }),

  simpleList: (keyword?: string) =>
    adminRequest.get<ApiResult<Product[]>>('/product/simpleList', { params: { keyword } }),
}

export const brandApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<Brand>>>('/brand/list', { params }),

  listAll: () =>
    adminRequest.get<ApiResult<Brand[]>>('/brand/listAll'),

  create: (data: Brand) =>
    adminRequest.post<ApiResult<number>>('/brand/create', data),

  update: (id: number, data: Brand) =>
    adminRequest.post<ApiResult<number>>(`/brand/update/${id}`, data),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/brand/delete/${id}`),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<Brand>>(`/brand/${id}`),

  updateShowStatus: (ids: number[], showStatus: number) =>
    adminRequest.post<ApiResult<number>>('/brand/update/showStatus', null, { params: { ids: ids.join(','), showStatus } }),

  updateFactoryStatus: (ids: number[], factoryStatus: number) =>
    adminRequest.post<ApiResult<number>>('/brand/update/factoryStatus', null, { params: { ids: ids.join(','), factoryStatus } }),
}

export const productCategoryApi = {
  list: (parentId: number, params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<ProductCategory>>>(`/productCategory/list/${parentId}`, { params }),

  listWithChildren: () =>
    adminRequest.get<ApiResult<ProductCategory[]>>('/productCategory/list/withChildren'),

  create: (data: Record<string, unknown>) =>
    adminRequest.post<ApiResult<number>>('/productCategory/create', data),

  update: (id: number, data: Record<string, unknown>) =>
    adminRequest.post<ApiResult<number>>(`/productCategory/update/${id}`, data),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/productCategory/delete/${id}`),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<ProductCategory>>(`/productCategory/${id}`),

  updateNavStatus: (ids: number[], navStatus: number) =>
    adminRequest.post<ApiResult<number>>('/productCategory/update/navStatus', null, { params: { ids: ids.join(','), navStatus } }),

  updateShowStatus: (ids: number[], showStatus: number) =>
    adminRequest.post<ApiResult<number>>('/productCategory/update/showStatus', null, { params: { ids: ids.join(','), showStatus } }),
}

export const productAttributeApi = {
  list: (cid: number, params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<ProductAttribute>>>(`/productAttribute/list/${cid}`, { params }),

  create: (data: ProductAttribute) =>
    adminRequest.post<ApiResult<number>>('/productAttribute/create', data),

  update: (id: number, data: ProductAttribute) =>
    adminRequest.post<ApiResult<number>>(`/productAttribute/update/${id}`, data),

  delete: (ids: number[]) =>
    adminRequest.post<ApiResult<number>>('/productAttribute/delete', null, { params: { ids: ids.join(',') } }),

  getItem: (id: number) =>
    adminRequest.get<ApiResult<ProductAttribute>>(`/productAttribute/${id}`),

  getAttrInfo: (productCategoryId: number) =>
    adminRequest.get<ApiResult<ProductAttribute[]>>(`/productAttribute/attrInfo/${productCategoryId}`),
}

export const productAttrCategoryApi = {
  list: (params: Record<string, unknown>) =>
    adminRequest.get<ApiResult<PageResult<ProductAttributeCategory>>>('/productAttribute/category/list', { params }),

  listWithAttr: () =>
    adminRequest.get<ApiResult<ProductAttributeCategory[]>>('/productAttribute/category/list/withAttr'),

  create: (name: string) =>
    adminRequest.post<ApiResult<number>>('/productAttribute/category/create', null, { params: { name } }),

  update: (id: number, name: string) =>
    adminRequest.post<ApiResult<number>>(`/productAttribute/category/update/${id}`, null, { params: { name } }),

  delete: (id: number) =>
    adminRequest.post<ApiResult<number>>(`/productAttribute/category/delete/${id}`),
}

export const skuStockApi = {
  getList: (pid: number, keyword?: string) =>
    adminRequest.get<ApiResult<SkuStock[]>>(`/sku/${pid}`, { params: { keyword } }),

  update: (pid: number, data: SkuStock[]) =>
    adminRequest.post<ApiResult<number>>(`/sku/update/${pid}`, data),
}

