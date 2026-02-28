import type { ApiResult } from '../core/http'
import { appRequest } from '../core/http'

export interface LoginParam {
  username: string
  password: string
}

export interface RegisterParam {
  username: string
  password: string
  telephone: string
  authCode: string
}

export interface LoginResult {
  token: string
  tokenHead: string
}

export interface MemberInfo {
  id: number
  username?: string
  nickname?: string
  phone?: string
  icon?: string
  gender?: number
  birthday?: string
  city?: string
  integration?: number
  growth?: number
  luckyCount?: number
  historyIntegration?: number
}

export const appAuthApi = {
  login: (data: LoginParam) =>
    appRequest.post<ApiResult<LoginResult>>('/sso/login', data),

  register: (data: RegisterParam) =>
    appRequest.post<ApiResult<null>>('/sso/register', data),

  getAuthCode: (telephone: string) =>
    appRequest.get<ApiResult<string>>('/sso/getAuthCode', { params: { telephone } }),

  updatePassword: (data: { telephone: string; password: string; authCode: string }) =>
    appRequest.post<ApiResult<null>>('/sso/updatePassword', data),

  info: () =>
    appRequest.get<ApiResult<MemberInfo>>('/sso/info'),

  refreshToken: () =>
    appRequest.get<ApiResult<string>>('/sso/refreshToken'),
}

