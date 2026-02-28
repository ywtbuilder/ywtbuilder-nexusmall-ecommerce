type WarmupPriority = 'immediate' | 'idle'

export interface WarmupOptions {
  scopeId?: string
  maxItems?: number
  priority?: WarmupPriority
  variant?: string
}

interface WarmupTask {
  key: string
  url: string
}

interface ScopeState {
  queue: WarmupTask[]
  idleHandles: number[]
}

const DEFAULT_SCOPE = '__global__'
const DEFAULT_BUDGET = 20
const MOBILE_CONCURRENCY = 2
const DESKTOP_CONCURRENCY = 4
const ASSET_PATH_RE = /^\/api\/asset\/image\/[0-9a-f]{64}(?:\?.*)?$/i
const DATA_IMAGE_RE = /^data:image\//i

const scopes = new Map<string, ScopeState>()
const scheduledKeys = new Set<string>()
let inflightCount = 0

function isMobileDevice(): boolean {
  if (typeof window === 'undefined') return false
  if (window.matchMedia?.('(pointer: coarse)').matches) return true
  return /android|iphone|ipad|ipod|mobile/i.test(window.navigator.userAgent)
}

function maxConcurrency(): number {
  return isMobileDevice() ? MOBILE_CONCURRENCY : DESKTOP_CONCURRENCY
}

function normalizeCandidate(raw?: string | null): string {
  if (!raw) return ''
  const trimmed = raw.trim()
  if (!trimmed) return ''
  if (DATA_IMAGE_RE.test(trimmed)) return trimmed
  if (ASSET_PATH_RE.test(trimmed)) return trimmed
  return ''
}

function withVariant(url: string, variant?: string): string {
  if (!variant || !url.startsWith('/api/asset/image/')) return url
  const [path, query = ''] = url.split('?', 2)
  const params = new URLSearchParams(query)
  params.set('variant', variant)
  const serialized = params.toString()
  return serialized ? `${path}?${serialized}` : path
}

function normalizeForWarmup(raw: string, variant?: string): string {
  const normalized = normalizeCandidate(raw)
  if (!normalized) return ''
  return withVariant(normalized, variant)
}

function ensureScope(scopeId: string): ScopeState {
  const existing = scopes.get(scopeId)
  if (existing) return existing
  const created: ScopeState = { queue: [], idleHandles: [] }
  scopes.set(scopeId, created)
  return created
}

function releaseScope(scopeId: string): void {
  const state = scopes.get(scopeId)
  if (!state) return
  if (state.queue.length > 0 || state.idleHandles.length > 0) return
  scopes.delete(scopeId)
}

function consumeNextTask(): WarmupTask | null {
  for (const [scopeId, state] of scopes.entries()) {
    const task = state.queue.shift()
    if (task) {
      if (state.queue.length === 0 && state.idleHandles.length === 0) {
        releaseScope(scopeId)
      }
      return task
    }
    if (state.idleHandles.length === 0) {
      releaseScope(scopeId)
    }
  }
  return null
}

function preloadImage(url: string): Promise<void> {
  return new Promise((resolve) => {
    const image = new Image()
    image.decoding = 'async'
    image.onload = () => resolve()
    image.onerror = () => resolve()
    image.src = url
  })
}

function pumpQueue(): void {
  const max = maxConcurrency()
  while (inflightCount < max) {
    const next = consumeNextTask()
    if (!next) break
    inflightCount++
    void preloadImage(next.url).finally(() => {
      inflightCount = Math.max(0, inflightCount - 1)
      pumpQueue()
    })
  }
}

function scheduleIdle(scopeId: string, callback: () => void): void {
  if (typeof window === 'undefined') {
    callback()
    return
  }
  const win = window as Window & typeof globalThis & {
    requestIdleCallback?: (cb: IdleRequestCallback, options?: IdleRequestOptions) => number
  }
  const state = ensureScope(scopeId)
  const releaseHandle = (handle: number) => {
    const idx = state.idleHandles.indexOf(handle)
    if (idx >= 0) state.idleHandles.splice(idx, 1)
    releaseScope(scopeId)
  }

  if (typeof win.requestIdleCallback === 'function') {
    const handle = win.requestIdleCallback(() => {
      releaseHandle(handle)
      callback()
    }, { timeout: 500 })
    state.idleHandles.push(handle)
    return
  }

  const handle = win.setTimeout(() => {
    releaseHandle(handle)
    callback()
  }, 16)
  state.idleHandles.push(handle)
}

export function warmupImages(urls: Array<string | null | undefined>, options: WarmupOptions = {}): void {
  if (!urls.length) return
  const scopeId = options.scopeId ?? DEFAULT_SCOPE
  const budget = Math.min(Math.max(options.maxItems ?? DEFAULT_BUDGET, 1), DEFAULT_BUDGET)
  const priority: WarmupPriority = options.priority ?? 'idle'
  const variant = options.variant

  const normalized: string[] = []
  for (const raw of urls) {
    const url = normalizeForWarmup(raw ?? '', variant)
    if (!url) continue
    normalized.push(url)
    if (normalized.length >= budget) break
  }
  if (!normalized.length) return

  const scope = ensureScope(scopeId)
  const enqueue = () => {
    for (const url of normalized) {
      const key = url
      if (scheduledKeys.has(key)) continue
      scheduledKeys.add(key)
      scope.queue.push({ key, url })
    }
    pumpQueue()
  }

  if (priority === 'immediate') {
    enqueue()
    return
  }

  scheduleIdle(scopeId, enqueue)
}

export function cancelWarmup(scopeId: string = DEFAULT_SCOPE): void {
  const scope = scopes.get(scopeId)
  if (!scope) return
  const win = typeof window !== 'undefined'
    ? window as Window & typeof globalThis & { cancelIdleCallback?: (id: number) => void }
    : null

  for (const handle of scope.idleHandles) {
    if (win && typeof win.cancelIdleCallback === 'function') {
      win.cancelIdleCallback(handle)
    } else if (win) {
      win.clearTimeout(handle)
    }
  }
  scope.idleHandles = []

  for (const task of scope.queue) {
    scheduledKeys.delete(task.key)
  }
  scope.queue = []
  releaseScope(scopeId)
}
