type RumStage = 'navigation' | 'vital' | 'route' | 'api' | 'app'

export interface RumEventPayload {
  stage: RumStage
  name: string
  durationMs?: number
  value?: number
  route?: string
  status?: number
  success?: boolean
  requestId?: string
  traceId?: string
  sessionId?: string
  extra?: Record<string, unknown>
}

const SESSION_KEY = 'mall_rum_session_id'
const TRACE_KEY = 'mall_rum_trace_id'
const ROUTE_MARK_PREFIX = 'mall_route_start:'

function getEnvValue(key: string): string | undefined {
  if (typeof import.meta !== 'undefined' && (import.meta as ImportMeta).env) {
    const value = (import.meta as ImportMeta).env[key]
    if (typeof value === 'string' && value.trim()) return value.trim()
  }
  return undefined
}

function getRumEndpoint(): string {
  return getEnvValue('VITE_RUM_ENDPOINT') || '/api/rum'
}

function randomId(prefix: string): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}-${crypto.randomUUID()}`
  }
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

function getOrCreateSessionId(): string {
  if (typeof window === 'undefined') return randomId('sess')
  const exists = window.sessionStorage.getItem(SESSION_KEY)
  if (exists) return exists
  const created = randomId('sess')
  window.sessionStorage.setItem(SESSION_KEY, created)
  return created
}

export function getOrCreateTraceId(): string {
  if (typeof window === 'undefined') return randomId('trace')
  const exists = window.sessionStorage.getItem(TRACE_KEY)
  if (exists) {
    ;(window as unknown as Record<string, unknown>).__MALL_RUM_TRACE_ID = exists
    return exists
  }
  const created = randomId('trace')
  window.sessionStorage.setItem(TRACE_KEY, created)
  ;(window as unknown as Record<string, unknown>).__MALL_RUM_TRACE_ID = created
  return created
}

export function rotateTraceId(): string {
  if (typeof window === 'undefined') return randomId('trace')
  const created = randomId('trace')
  window.sessionStorage.setItem(TRACE_KEY, created)
  ;(window as unknown as Record<string, unknown>).__MALL_RUM_TRACE_ID = created
  return created
}

function postRum(payload: RumEventPayload): void {
  if (typeof window === 'undefined') return
  const body = JSON.stringify({
    timestamp: new Date().toISOString(),
    sessionId: payload.sessionId || getOrCreateSessionId(),
    traceId: payload.traceId || getOrCreateTraceId(),
    ...payload,
  })

  const endpoint = getRumEndpoint()
  const sent = typeof navigator !== 'undefined'
    && typeof navigator.sendBeacon === 'function'
    && navigator.sendBeacon(endpoint, body)

  if (!sent) {
    void fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body,
      keepalive: true,
    }).catch(() => {
      // ignore rum transport errors
    })
  }
}

export function reportRumEvent(event: RumEventPayload): void {
  postRum(event)
}

export function markRouteStart(route: string): void {
  if (typeof performance === 'undefined') return
  performance.mark(`${ROUTE_MARK_PREFIX}${route}`)
}

export function markRouteEnd(route: string): void {
  if (typeof performance === 'undefined') return
  const startMark = `${ROUTE_MARK_PREFIX}${route}`
  const endMark = `${startMark}:end`
  performance.mark(endMark)
  try {
    performance.measure(`mall_route:${route}`, startMark, endMark)
    const measure = performance.getEntriesByName(`mall_route:${route}`).at(-1)
    if (measure) {
      reportRumEvent({
        stage: 'route',
        name: 'route_change',
        durationMs: Math.round(measure.duration),
        route,
      })
    }
  } finally {
    performance.clearMarks(startMark)
    performance.clearMarks(endMark)
    performance.clearMeasures(`mall_route:${route}`)
  }
}

function setupNavigationTiming() {
  window.addEventListener('load', () => {
    const nav = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming | undefined
    if (!nav) return

    reportRumEvent({
      stage: 'navigation',
      name: 'page_load',
      durationMs: Math.round(nav.loadEventEnd - nav.startTime),
      value: Math.round(nav.loadEventEnd - nav.startTime),
      extra: {
        ttfbMs: Math.round(nav.responseStart - nav.startTime),
        downloadMs: Math.round(nav.responseEnd - nav.responseStart),
        domParseMs: Math.round(nav.domInteractive - nav.responseEnd),
        domContentLoadedMs: Math.round(nav.domContentLoadedEventEnd - nav.startTime),
      },
    })
  })
}

function setupVitalObservers() {
  if (typeof PerformanceObserver === 'undefined') return

  try {
    const lcpObserver = new PerformanceObserver((entryList) => {
      const entries = entryList.getEntries()
      const lastEntry = entries.at(-1)
      if (!lastEntry) return
      reportRumEvent({
        stage: 'vital',
        name: 'LCP',
        value: Math.round(lastEntry.startTime),
      })
    })
    lcpObserver.observe({ type: 'largest-contentful-paint', buffered: true })
  } catch {
    // ignore unsupported browsers
  }

  try {
    let cls = 0
    const clsObserver = new PerformanceObserver((entryList) => {
      for (const entry of entryList.getEntries()) {
        const shifted = entry as PerformanceEntry & { value?: number; hadRecentInput?: boolean }
        if (!shifted.hadRecentInput && typeof shifted.value === 'number') {
          cls += shifted.value
        }
      }
      reportRumEvent({
        stage: 'vital',
        name: 'CLS',
        value: Number(cls.toFixed(4)),
      })
    })
    clsObserver.observe({ type: 'layout-shift', buffered: true })
  } catch {
    // ignore unsupported browsers
  }

  try {
    let inp = 0
    const inpObserver = new PerformanceObserver((entryList) => {
      for (const entry of entryList.getEntries()) {
        const eventTiming = entry as PerformanceEntry & { duration?: number; interactionId?: number }
        if (eventTiming.interactionId && typeof eventTiming.duration === 'number') {
          inp = Math.max(inp, eventTiming.duration)
        }
      }
      if (inp > 0) {
        reportRumEvent({
          stage: 'vital',
          name: 'INP',
          value: Math.round(inp),
        })
      }
    })
    inpObserver.observe({ type: 'event', durationThreshold: 40, buffered: true } as PerformanceObserverInit)
  } catch {
    // ignore unsupported browsers
  }
}

function setupApiMetricBridge() {
  window.addEventListener('mall:api-metric', (evt) => {
    const customEvt = evt as CustomEvent<RumEventPayload>
    if (!customEvt.detail) return
    reportRumEvent({
      ...customEvt.detail,
      stage: 'api',
    })
  })
}

export function initRum(): void {
  if (typeof window === 'undefined') return
  const sessionId = getOrCreateSessionId()
  const traceId = getOrCreateTraceId()

  reportRumEvent({
    stage: 'app',
    name: 'app_bootstrap',
    sessionId,
    traceId,
  })

  setupNavigationTiming()
  setupVitalObservers()
  setupApiMetricBridge()
}
