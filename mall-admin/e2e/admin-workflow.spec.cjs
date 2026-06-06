/**
 * Admin Management E2E Tests
 *
 * Covers: Login, Dashboard, Category, Product, User, Order pages.
 * Uses test.describe.serial to share a single page across all tests (login once).
 *
 * Credentials: admin / admin123
 * Captcha: set directly in Redis via redis-cli (JSON-encoded for Jackson2JsonRedisSerializer)
 *          and the HTTP request is intercepted/aborted so the known code is not overwritten.
 *
 * Run: npx playwright test e2e/admin-workflow.spec.cjs --reporter=list
 */

const { test, expect } = require('@playwright/test');
const { execSync } = require('child_process');

const BASE_URL = 'http://localhost:3002';
const REDIS_CLI = 'D:/software/redis/redis-cli.exe';
const CAPTCHA = '0000';

let page;

test.describe.serial('Admin Management Workflow', () => {

  test.beforeAll(async ({ browser }) => {
    // ---------------------------------------------------------------
    // 1. Set a known captcha code in Redis (JSON-encoded string so
    //    Jackson2JsonRedisSerializer can deserialize it).
    //    The code is stored as "0000" (with JSON quotes).
    // ---------------------------------------------------------------
    execSync(`"${REDIS_CLI}" -a 123456 SET validate:code "\\"${CAPTCHA}\\""`);

    // ---------------------------------------------------------------
    // 2. Create a fresh page and intercept the captcha HTTP request
    //    so it never reaches the server.  This prevents the server
    //    from overwriting our known captcha code in Redis.
    // ---------------------------------------------------------------
    page = await browser.newPage();
    await page.route('**/auth/verificationCode', route => {
      route.abort();
    });

    // ---------------------------------------------------------------
    // 3. Navigate to login page
    // ---------------------------------------------------------------
    await page.goto(`${BASE_URL}/#/login`);
    await page.waitForSelector('h2:has-text("商城后台管理")');

    // ---------------------------------------------------------------
    // 4. Fill credentials and captcha, then submit
    // ---------------------------------------------------------------
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin123');
    await page.fill('input[placeholder="验证码"]', CAPTCHA);
    await page.click('button:has-text("登录")');

    // ---------------------------------------------------------------
    // 5. Wait for redirect to home/dashboard page (/#/).
    //    The router guard redirects to / (the Main layout with home child).
    // ---------------------------------------------------------------
    // Verify navigation happened by waiting for dashboard content
    await page.waitForSelector('text=今日订单数', { timeout: 15000 });
  });

  // ------------------------------------------------------------------
  // Test 1 — Dashboard
  // ------------------------------------------------------------------
  test('1. Dashboard displays statistics cards', async () => {
    // Should be on the home/dashboard page (hash is / or /home)
    const hash = await page.evaluate(() => window.location.hash);
    expect(['#/', '#/home']).toContain(hash);

    // Stat cards
    await expect(page.locator('text=今日订单数').first()).toBeVisible();
    await expect(page.locator('text=今日销售额').first()).toBeVisible();
    await expect(page.locator('text=总用户数').first()).toBeVisible();
    await expect(page.locator('text=在售商品').first()).toBeVisible();

    // Chart section headings
    await expect(page.locator('h3:has-text("销售趋势")').first()).toBeVisible();
    await expect(page.locator('h3:has-text("分类销量 TOP5")').first()).toBeVisible();
    await expect(page.locator('h3:has-text("订单状态概览")').first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 2 — Category Management
  // ------------------------------------------------------------------
  test('2. Category management page loads with tree table', async () => {
    await page.goto(`${BASE_URL}/#/goods/category`);
    await page.waitForSelector('input[placeholder="请输入分类名称"]');

    // Toolbar buttons
    await expect(page.locator('button:has-text("新增分类")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("查询")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("重置")')).toBeVisible();

    // Tree table renders rows
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 3 — Product List
  // ------------------------------------------------------------------
  test('3. Product list page loads with data', async () => {
    await page.goto(`${BASE_URL}/#/goods/product`);
    await page.waitForSelector('.el-table');

    // Toolbar buttons
    await expect(page.locator('button:has-text("新增商品")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("查询")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("重置")')).toBeVisible();

    // Table body has rows
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 4 — User List
  // ------------------------------------------------------------------
  test('4. User list page loads with data', async () => {
    await page.goto(`${BASE_URL}/#/system/user`);
    await page.waitForSelector('.el-table');

    // Toolbar / action buttons
    await expect(page.locator('button:has-text("新增用户")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("查询")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("重置")')).toBeVisible();

    // Table body has rows
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 5 — Order List
  // ------------------------------------------------------------------
  test('5. Order list page loads with data', async () => {
    await page.goto(`${BASE_URL}/#/oms/order`);
    await page.waitForSelector('.el-table');

    // Toolbar / action buttons
    await expect(page.locator('button:has-text("批量发货")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("查询")')).toBeVisible();
    await expect(page.locator('.page-search button:has-text("重置")')).toBeVisible();

    // Table body has rows
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 6 — User Management Search
  // ------------------------------------------------------------------
  test('6. User management search filters results', async () => {
    await page.goto(`${BASE_URL}/#/system/user`);
    await page.waitForSelector('.el-table');

    // Type in name search field
    const nameInput = page.locator('input[placeholder="请输入姓名"]');
    await nameInput.fill('admin');

    // Click search and wait for API response
    await Promise.all([
      page.waitForResponse(resp => resp.url().includes('/dev/user') && resp.status() === 200),
      page.click('.page-search button:has-text("查询")')
    ]);

    // Verify table updated with filtered results
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 7 — Product Management Search
  // ------------------------------------------------------------------
  test('7. Product management search filters results', async () => {
    await page.goto(`${BASE_URL}/#/goods/product`);
    await page.waitForSelector('.el-table');

    // Type product name in search field
    const nameInput = page.locator('input[placeholder="请输入名称"]');
    await nameInput.fill('商品');

    // Click search and wait for response
    await Promise.all([
      page.waitForResponse(resp => resp.url().includes('/dev/product/list') && resp.status() === 200),
      page.click('.page-search button:has-text("查询")')
    ]);

    // Verify table updated with filtered results
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 8 — Order Management View Detail
  // ------------------------------------------------------------------
  test('8. Order management view detail', async () => {
    await page.goto(`${BASE_URL}/#/oms/order`);
    await page.waitForSelector('.el-table');

    // Wait for data rows to load
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible({ timeout: 10000 });

    // Click the first "详情" button
    await page.locator('button:has-text("详情")').first().click();

    // Verify order detail dialog appears with descriptions
    await expect(page.locator('.el-dialog:has-text("订单详情")')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('.el-dialog .el-descriptions')).toBeVisible();

    // Close dialog
    await page.locator('.el-dialog button:has-text("关闭")').click();
    await expect(page.locator('.el-dialog:has-text("订单详情")')).not.toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 9 — Menu Management
  // ------------------------------------------------------------------
  test('9. Menu management page loads with tree', async () => {
    await page.goto(`${BASE_URL}/#/permission/menu`);
    await page.waitForSelector('.el-table');

    // Verify page elements
    await expect(page.locator('button:has-text("新增菜单")')).toBeVisible();

    // Tree table renders rows with menu data
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 10 — Role Management
  // ------------------------------------------------------------------
  test('10. Role management page loads with data', async () => {
    await page.goto(`${BASE_URL}/#/permission/role`);
    await page.waitForSelector('.el-table');

    await expect(page.locator('button:has-text("新增角色")')).toBeVisible();
    const rows = page.locator('.el-table__body-wrapper tbody tr');
    await expect(rows.first()).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Test 11 — Logout
  // ------------------------------------------------------------------
  test('11. Logout redirects to login page', async () => {
    // Click user avatar area in header to open dropdown menu
    await page.locator('.layout-header .el-dropdown .cursor-pointer').click();

    // Click "退出登录" from dropdown menu
    const logoutItem = page.locator('.el-dropdown-menu__item:has-text("退出登录")');
    await expect(logoutItem).toBeVisible({ timeout: 3000 });
    await logoutItem.click();

    // Verify redirected to login page
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
    await expect(page.locator('h2:has-text("商城后台管理")')).toBeVisible();
  });

  // ------------------------------------------------------------------
  // Cleanup
  // ------------------------------------------------------------------
  test.afterAll(async () => {
    if (page) await page.close();
  });
});
