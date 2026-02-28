import type { ApiResult } from '../core/http'
import { appRequest } from '../core/http'

export interface PaymentParam {
  orderId: number
  payType: number
}

export interface PaymentResult {
  payUrl?: string
  paySign?: string
  outTradeNo?: string
}

export const paymentApi = {
  create: (data: PaymentParam) =>
    appRequest.post<ApiResult<PaymentResult>>('/payment/create', null, { params: data }),

  notify: (data: Record<string, unknown>) =>
    appRequest.post<ApiResult<null>>('/payment/notify', data),

  query: (orderId: number) =>
    appRequest.get<ApiResult<Record<string, unknown>>>('/payment/query', { params: { orderId } }),
}

