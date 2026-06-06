/**
 * 权限简化后 E2E 验证
 *
 * 验证场景：
 *   1. 管理员登录 → 侧边栏显示全部 5 个菜单
 *   2. 普通用户登录 → 侧边栏只显示"首页"
 *   3. 管理员可访问 /user、/category、/product、/order
 *   4. 未登录访问任意页面 → 跳转 /login
 *   5. 管理员退出登录 → 回到登录页
 *
 * Redis 验证码 0000 + 拦截 captcha 请求
 */


const { test, expect } = require('@playwright/test');
const { execSync } = require('child_process');

const BASE_URL = 'http://localhost:3016';
const REDIS_CLI = 'D:/software/redis/redis-cli.exe';
const CAPTCHA = '0000';

test.describe.serial('权限简化验证', () => {

  let adminPage;
  let userPage;

  // ------- 管理员登录 -------
  test('管理员登录 — 侧边栏显示全部菜单', async ({ browser }) => {
    adminPage = await browser.newPage();
    execSync(`"${REDIS_CLI}" -a 123456 SET validate:code "\\"${CAPTCHA}\\""`);
    await adminPage.route('**/auth/verificationCode', route => route.abort());
    await adminPage.goto(`${BASE_URL}/#/login`);
    await adminPage.waitForSelector('h2:has-text("商城后台管理")');

    await adminPage.fill('input[placeholder="用户名"]', 'admin');
    await adminPage.fill('input[placeholder="密码"]', 'admin123');
    await adminPage.fill('input[placeholder="验证码"]', CAPTCHA);
    await adminPage.click('button:has-text("登录")');

    await adminPage.waitForSelector('text=今日订单数', { timeout: 15000 });

    // 验证管理员侧边栏有全部菜单
    const menuItems = adminPage.locator('.el-menu-item');
    await expect(menuItems.first()).toBeVisible();
    const menuCount = await menuItems.count();
    expect(menuCount).toBeGreaterThanOrEqual(5);  // 首页 + 用户 + 分类 + 商品 + 订单
  });

  // ------- 管理员访问管理页面 -------
  test('管理员 — 可正常访问 User 页面', async () => {
    await adminPage.goto(`${BASE_URL}/#/user`);
    await adminPage.waitForSelector('.el-table', { timeout: 10000 });
    await expect(adminPage.locator('.el-table')).toBeVisible();
  });

  test('管理员 — 可正常访问 Product 页面', async () => {
    await adminPage.goto(`${BASE_URL}/#/product`);
    await adminPage.waitForSelector('.el-table', { timeout: 10000 });
    await expect(adminPage.locator('.el-table')).toBeVisible();
  });

  test('管理员 — 可正常访问 Category 页面', async () => {
    await adminPage.goto(`${BASE_URL}/#/category`);
    await adminPage.waitForSelector('input[placeholder="请输入分类名称"]', { timeout: 10000 });
    await expect(adminPage.locator('input[placeholder="请输入分类名称"]')).toBeVisible();
  });

  test('管理员 — 可正常访问 Order 页面', async () => {
    await adminPage.goto(`${BASE_URL}/#/order`);
    await adminPage.waitForSelector('.el-table', { timeout: 10000 });
    await expect(adminPage.locator('.el-table')).toBeVisible();
  });

  // ------- 普通用户登录 -------
  test('普通用户登录 — 侧边栏只显示首页', async ({ browser }) => {
    userPage = await browser.newPage();
    execSync(`"${REDIS_CLI}" -a 123456 SET validate:code "\\"${CAPTCHA}\\""`);
    await userPage.route('**/auth/verificationCode', route => route.abort());
    await userPage.goto(`${BASE_URL}/#/login`);
    await userPage.waitForSelector('h2:has-text("商城后台管理")');

    await userPage.fill('input[placeholder="用户名"]', 'user');
    await userPage.fill('input[placeholder="密码"]', '123');
    await userPage.fill('input[placeholder="验证码"]', CAPTCHA);
    await userPage.click('button:has-text("登录")');

    await userPage.waitForSelector('text=今日订单数', { timeout: 15000 });

    // 验证普通用户侧边栏只有"首页"
    const menuItems = userPage.locator('.el-menu-item');
    await expect(menuItems.first()).toBeVisible();
    const menuCount = await menuItems.count();
    expect(menuCount).toBe(1);  // 只有首页
  });

  // ------- 未登录访问保护 -------
  test('未登录访问 /user — 重定向到登录页', async ({ browser }) => {
    const page = await browser.newPage();
    await page.goto(`${BASE_URL}/#/user`);
    // 路由守卫应该重定向到 login
    await page.waitForURL(/\/login/, { timeout: 10000 });
    await expect(page.locator('h2:has-text("商城后台管理")')).toBeVisible();
    await page.close();
  });

  // ------- 管理退出登录 -------
  test('管理员退出登录 — 回到登录页', async () => {
    await adminPage.locator('.layout-header .el-dropdown .cursor-pointer').click();
    const logoutItem = adminPage.locator('.el-dropdown-menu__item:has-text("退出登录")');
    await expect(logoutItem).toBeVisible({ timeout: 3000 });
    await logoutItem.click();
    await expect(adminPage).toHaveURL(/\/login/, { timeout: 10000 });
  });

  // ------- 清理 -------
  test.afterAll(async () => {
    if (adminPage) await adminPage.close();
    if (userPage) await userPage.close();
  });
});
