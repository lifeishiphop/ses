import { expect, test } from '@playwright/test';

test('seeded demo accounts log in through the actual login form', async ({ page }, testInfo) => {
  test.skip(!process.env.SES_DEMO_CHECK || testInfo.project.name !== 'desktop', 'Requires a locally seeded demo backend.');
  for (const number of ['20990001', '20990002', '20999901', '20999999']) {
    await page.goto('/login');
    await page.getByLabel('학번 / 교번', { exact: true }).fill(number);
    await page.getByLabel('비밀번호', { exact: true }).fill('SesDemo123!');
    const loginResponse = page.waitForResponse(response => new URL(response.url()).pathname === '/api/login' && response.request().method() === 'POST');
    await page.getByRole('button', { name: '로그인', exact: true }).click();
    expect((await loginResponse).status()).toBe(200);
    await expect(page.getByRole('heading', { level: 1 })).toContainText('(데모)님, 오늘도 한 걸음 더.');
    await page.getByRole('button', { name: '로그아웃', exact: true }).click();
    await expect(page.getByRole('button', { name: '로그인', exact: true })).toBeVisible();
  }
});
