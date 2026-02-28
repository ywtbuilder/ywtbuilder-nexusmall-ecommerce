import { test, expect } from '@playwright/test';

/**
 * Admin 前端 Golden Loop E2E 测试
 *
 * 黄金路径：登录 → 查看仪表盘 → 浏览商品列表 → 浏览订单列表
 *
 * 前置条件：
 *   - Admin 前端开发服务器运行在 http://localhost:3000
 *   - Admin BFF API 运行在 http://localhost:8086
 *   - 数据库已初始化（包含默认管理员）
 */

const ADMIN_USER = 'admin';
const ADMIN_PASS = 'admin123';

test.describe('Admin 黄金路径', () => {

  test('1. 登录页可正常加载', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[placeholder*="用户名"], input[name="username"]')).toBeVisible({ timeout: 10_000 });
  });

  test('2. 管理员登录', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', ADMIN_USER);
    await page.fill('input[placeholder*="密码"], input[type="password"]', ADMIN_PASS);
    await page.click('button:has-text("登录"), button[type="submit"]');
    // 登录后应跳转到控制台/首页
    await expect(page).not.toHaveURL(/login/, { timeout: 10_000 });
  });

  test('3. 仪表盘/首页可加载', async ({ page }) => {
    await page.goto('/');
    const dashboard = page.locator('.dashboard, .home, main, .el-container');
    await expect(dashboard).toBeVisible({ timeout: 10_000 });
  });

  test('4. 商品管理页可加载', async ({ page }) => {
    await page.goto('/pms/product');
    const productList = page.locator('.product-list, table, .el-table, main');
    await expect(productList).toBeVisible({ timeout: 10_000 });
  });

  test('5. 订单管理页可加载', async ({ page }) => {
    await page.goto('/oms/order');
    const orderList = page.locator('.order-list, table, .el-table, main');
    await expect(orderList).toBeVisible({ timeout: 10_000 });
  });

  test('6. 品牌管理页可加载', async ({ page }) => {
    await page.goto('/pms/brand');
    const brandList = page.locator('.brand-list, table, .el-table, main');
    await expect(brandList).toBeVisible({ timeout: 10_000 });
  });
});
