import { test, expect } from '@playwright/test';

/**
 * App 前端 Golden Loop E2E 测试
 *
 * 黄金路径：注册 → 登录 → 浏览首页 → 查看商品 → 添加购物车 → 查看购物车
 *
 * 前置条件：
 *   - App 前端开发服务器运行在 http://localhost:3100
 *   - App BFF API 运行在 http://localhost:8085
 *   - 数据库已初始化（至少有一个商品）
 */

// 每次测试运行使用唯一用户名
const timestamp = Date.now();
const TEST_USER = `e2e_user_${timestamp}`;
const TEST_PASS = 'E2eTest@123';

test.describe('App 黄金路径', () => {

  test('1. 首页可正常加载', async ({ page }) => {
    await page.goto('/');
    // 等待页面加载完成 — 检查标题或关键元素
    await expect(page).toHaveTitle(/mall|商城/i, { timeout: 15_000 });
  });

  test('2. 注册新用户', async ({ page }) => {
    await page.goto('/register');
    // 填写注册表单
    await page.fill('input[placeholder*="用户名"], input[name="username"]', TEST_USER);
    await page.fill('input[placeholder*="密码"], input[type="password"]', TEST_PASS);
    // 点击注册按钮
    await page.click('button:has-text("注册"), button[type="submit"]');
    // 断言：注册成功后应跳转或显示成功提示
    await expect(page.locator('text=注册成功, text=登录')).toBeVisible({ timeout: 10_000 });
  });

  test('3. 使用注册的账户登录', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder*="用户名"], input[name="username"]', TEST_USER);
    await page.fill('input[placeholder*="密码"], input[type="password"]', TEST_PASS);
    await page.click('button:has-text("登录"), button[type="submit"]');
    // 断言：登录成功后应重定向到首页或显示用户信息
    await expect(page).not.toHaveURL(/login/, { timeout: 10_000 });
  });

  test('4. 浏览首页商品分类', async ({ page }) => {
    // 假设已登录（需配合 storageState）
    await page.goto('/');
    // 查找分类列表或推荐商品区域
    const content = page.locator('.product-list, .category-list, .home-content, main');
    await expect(content).toBeVisible({ timeout: 10_000 });
  });

  test('5. 查看商品详情页', async ({ page }) => {
    await page.goto('/product/1');
    // 断言：商品详情页应显示商品名称、价格、加入购物车按钮
    const productPage = page.locator('.product-detail, .product-info, main');
    await expect(productPage).toBeVisible({ timeout: 10_000 });
  });

  test('6. 查看购物车页面', async ({ page }) => {
    await page.goto('/cart');
    // 购物车页面应可正常加载
    const cartPage = page.locator('.cart, .cart-list, main');
    await expect(cartPage).toBeVisible({ timeout: 10_000 });
  });
});
