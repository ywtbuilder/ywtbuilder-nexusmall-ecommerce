export type { ApiResult, PageResult } from '../core/http'
export { createRequest, adminRequest } from '../core/http'

export { adminAuthApi } from './auth'
export {
  productApi,
  brandApi,
  productCategoryApi,
  productAttributeApi,
  productAttrCategoryApi,
  skuStockApi,
} from './product'
export {
  orderApi,
  returnApplyApi as adminReturnApplyApi,
  returnReasonApi,
  orderSettingApi,
  companyAddressApi,
} from './order'
export {
  couponApi,
  couponHistoryApi,
  flashApi,
  flashSessionApi,
  flashProductRelationApi,
  advertiseApi,
  homeBrandApi,
  homeNewProductApi,
  homeRecommendProductApi,
  homeRecommendSubjectApi,
} from './marketing'
export { esProductApi } from './search'
