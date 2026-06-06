/**
 * Mall Portal — Error Handling E2E Tests
 *
 * Tests that the frontend gracefully handles backend failures:
 *   1. Home API returns 500 — page should not crash
 *   2. Network errors intercepted — page should show graceful error state
 */

const { test, expect } = require('@playwright/test');

const BASE = 'http://localhost:3001';

test.describe('Error handling', () => {

  test('Home page does not crash when home API returns 500', async ({ page }) => {
    // Intercept the home API route and return 500
    await page.route('**/dev/product/home', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 500, message: 'Internal Server Error' }),
      });
    });

    // Navigate to the home page
    await page.goto(BASE + '/#/', { waitUntil: 'networkidle' });

    // The page should still render (not crash).  Verify body is present
    // and the page rendered content rather than a blank white screen.
    const body = page.locator('body');
    await expect(body).toBeVisible();

    // The header should be rendered (check for the logo link to home)
    const logo = page.locator('.logo');
    await expect(logo).toBeVisible();

    // The footer should be rendered
    const footer = page.locator('.footer');
    await expect(footer).toBeVisible();

    // The page title is present
    await expect(page).toHaveTitle(/家居商城/);
  });

  test('Home page shows product route returns empty gracefully', async ({ page }) => {
    // Intercept the home API and return a successful response but with empty data
    await page.route('**/dev/product/home', route => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 200, data: [] }),
      });
    });

    // Navigate to the home page
    await page.goto(BASE + '/#/', { waitUntil: 'networkidle' });

    // Page should render with empty state
    const emptyState = page.locator('.el-empty');
    await expect(emptyState).toBeVisible();

    // Header and footer still present
    await expect(page.locator('.header')).toBeVisible();
    await expect(page.locator('.footer')).toBeVisible();
  });

  test('Login page stays functional when backend is unreachable', async ({ page }) => {
    // Intercept all /dev/ requests to fail with connection refused style error
    await page.route('**/dev/**', route => {
      route.abort('connectionrefused');
    });

    // Navigate to the login page
    await page.goto(BASE + '/#/login', { waitUntil: 'networkidle' });

    // The login page should still render its form elements
    const loginForm = page.locator('.login-form, .el-form, form');
    await expect(loginForm).toBeVisible();

    // Body should be visible (not crashed)
    await expect(page.locator('body')).toBeVisible();
  });
});
