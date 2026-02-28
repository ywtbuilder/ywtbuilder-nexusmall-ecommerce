export type { ApiResult, PageResult } from './core/http'
export { createRequest, appRequest, adminRequest } from './core/http'

// ─── Admin API (only api objects, types via sub-path) ───
export {
  adminAuthApi,
  productApi,
  brandApi,
  productCategoryApi,
  productAttributeApi,
  productAttrCategoryApi,
  skuStockApi,
  orderApi,
  adminReturnApplyApi,
  returnReasonApi,
  orderSettingApi,
  companyAddressApi,
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
  esProductApi,
} from './admin'

// ─── App API (only api objects, types via sub-path) ─────
export {
  appAuthApi,
  homeApi,
  appProductApi,
  cartApi,
  portalOrderApi,
  memberAddressApi,
  memberReadHistoryApi,
  memberCollectionApi,
  memberAttentionApi,
  memberCouponApi,
  appReturnApplyApi,
  userCenterApi,
  paymentApi,
  searchApi,
} from './app'
