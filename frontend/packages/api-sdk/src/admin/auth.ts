import type { ApiResult } from '../core/http'
import { adminRequest } from '../core/http'

export interface LoginParam {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenHead: string
}

export interface AdminInfo {
  username: string
  roles: string[]
  menus: unknown[]
  icon?: string
}

export const adminAuthApi = {
  login: (data: LoginParam) =>
    adminRequest.post<ApiResult<LoginResult>>('/admin/login', data),

  info: () =>
    adminRequest.get<ApiResult<AdminInfo>>('/admin/info'),

  logout: () =>
    adminRequest.post<ApiResult<void>>('/admin/logout'),

  refreshToken: () =>
    adminRequest.get<ApiResult<string>>('/admin/refreshToken'),
}

