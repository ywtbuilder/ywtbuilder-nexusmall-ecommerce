import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosRequestConfig } from 'axios'

/** 统一 API 响应结构 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页响应结构 */
export interface PageResult<T> {
  pageNum: number
  pageSize: number
  totalPage: number
  total: number
  list: T[]
}

export type RequestConfig = Pick<AxiosRequestConfig, 'signal' | 'headers' | 'timeout'>

interface RequestMetadata {
  startedAt: number
  requestId: string
  traceId: string
  sessionId: string
}

interface RequestConfigWithMetadata extends InternalAxiosRequestConfig {
  metadata?: RequestMetadata
}

function getEnvValue(key: string): string | undefined {
  const meta = import.meta as unknown as { env?: Record<string, unknown> }
  if (typeof import.meta !== 'undefined' && meta.env) {
    const value = meta.env[key]
    if (typeof value === 'string' && value.trim()) return value.trim()
  }
  return undefined
}

function resolveBaseUrl(kind: 'app' | 'admin'): string {
  const envKey = kind === 'app' ? 'VITE_APP_API_BASE' : 'VITE_ADMIN_API_BASE'
  const envValue = getEnvValue(envKey)
  if (envValue) return envValue

  // Browser default: same-origin reverse proxy path.
  if (typeof window !== 'undefined') {
    return kind === 'app' ? '/api' : '/admin-api'
  }

  // Non-browser fallback (tests/SSR).
  return kind === 'app' ? 'http://localhost:18080' : 'http://localhost:18081'
}

function randomId(prefix: string): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}-${crypto.randomUUID()}`
  }
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

function getSessionId(): string {
  if (typeof window === 'undefined') return randomId('sess')
  const key = 'mall_rum_session_id'
  const exists = window.sessionStorage.getItem(key)
  if (exists) return exists
  const created = randomId('sess')
  window.sessionStorage.setItem(key, created)
  return created
}

function getTraceId(): string {
  if (typeof window === 'undefined') return randomId('trace')
  const globalTraceId = (window as unknown as Record<string, unknown>).__MALL_RUM_TRACE_ID
  if (typeof globalTraceId === 'string' && globalTraceId.trim()) return globalTraceId
  const key = 'mall_rum_trace_id'
  const exists = window.sessionStorage.getItem(key)
  if (exists) return exists
  const created = randomId('trace')
  window.sessionStorage.setItem(key, created)
  ;(window as unknown as Record<string, unknown>).__MALL_RUM_TRACE_ID = created
  return created
}

function emitApiMetric(detail: Record<string, unknown>) {
  if (typeof window === 'undefined' || typeof window.dispatchEvent !== 'function') return
  window.dispatchEvent(new CustomEvent('mall:api-metric', { detail }))
}

export function createRequest(baseURL: string): AxiosInstance {
  const instance = axios.create({
    baseURL,
    timeout: 15000,
  })

  instance.interceptors.request.use((rawConfig: InternalAxiosRequestConfig) => {
    const config = rawConfig as RequestConfigWithMetadata
    const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

    const metadata: RequestMetadata = {
      startedAt: Date.now(),
      requestId: randomId('req'),
      traceId: getTraceId(),
      sessionId: getSessionId(),
    }
    config.metadata = metadata

    if (config.headers) {
      config.headers['X-Request-Id'] = metadata.requestId
      config.headers['X-Trace-Id'] = metadata.traceId
      config.headers['X-Session-Id'] = metadata.sessionId
    }

    return config
  })

  instance.interceptors.response.use(
    (response: AxiosResponse<ApiResult>) => {
      const config = response.config as RequestConfigWithMetadata
      const durationMs = config.metadata ? Date.now() - config.metadata.startedAt : undefined
      emitApiMetric({
        name: 'api_request',
        durationMs,
        status: response.status,
        success: true,
        traceId: config.metadata?.traceId,
        requestId: config.metadata?.requestId,
        sessionId: config.metadata?.sessionId,
        route: response.config.url,
        extra: {
          method: response.config.method,
        },
      })

      const res = response.data
      if (res.code !== 200) {
        if (res.code === 401) {
          if (typeof window !== 'undefined') {
            localStorage.removeItem('token')
            window.location.href = '/login'
          }
        }
        return Promise.reject(new Error(res.message || 'Error'))
      }
      return response
    },
    (error) => {
      const config = (error?.config || {}) as RequestConfigWithMetadata
      const durationMs = config.metadata ? Date.now() - config.metadata.startedAt : undefined
      emitApiMetric({
        name: 'api_request',
        durationMs,
        status: error?.response?.status,
        success: false,
        traceId: config.metadata?.traceId,
        requestId: config.metadata?.requestId,
        sessionId: config.metadata?.sessionId,
        route: config.url,
        extra: {
          method: config.method,
          message: error?.message,
        },
      })
      return Promise.reject(error)
    },
  )

  return instance
}

export const appRequest = createRequest(resolveBaseUrl('app'))
export const adminRequest = createRequest(resolveBaseUrl('admin'))
