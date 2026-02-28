import { defineConfig, devices } from '@playwright/test';

/**
 * Mall V3 — E2E 端到端测试配置
 *
 * 前置条件：
 *   1. 后端 API 已启动（App BFF: 18080, Admin BFF: 18081）
 *   2. 前端开发服务器已启动（App: 8091, Admin: 8090）
 *   或直接使用 build 产物 + preview 服务
 *
 * 注意：下方 baseURL 默认值（3100/3000）为占位，本地运行建议覆盖：
 *   $env:APP_BASE_URL='http://localhost:8091'
 *   $env:ADMIN_BASE_URL='http://localhost:8090'
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,                    // 购物流程有顺序依赖，串行执行
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,                              // 单 worker 保证串行
  reporter: [
    ['html', { open: 'never' }],
    ['list'],
  ],
  timeout: 30_000,
  expect: { timeout: 10_000 },

  use: {
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'app-chromium',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.APP_BASE_URL || 'http://localhost:3100',
      },
    },
    {
      name: 'admin-chromium',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.ADMIN_BASE_URL || 'http://localhost:3000',
      },
    },
  ],
});
