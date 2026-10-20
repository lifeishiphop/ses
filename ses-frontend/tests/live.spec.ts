import { expect, test } from '@playwright/test';

test('live API: instructor authoring and student enrollment', async ({ page, request }, testInfo) => {
  test.skip(!process.env.SES_LIVE_API || testInfo.project.name !== 'desktop', 'Requires a disposable backend configured via API_TARGET.');
  const suffix = String(Date.now()).slice(-7);
  const teacherNumber = `t${suffix}`;
  const studentNumber = `s${suffix}`;
  for (const [number, name, identity] of [[teacherNumber, '검증강사', 2], [studentNumber, '검증학생', 1]] as const) {
    const response = await request.post('/api/signup', { data: {
      number, name, identity, email: `${number}@kumoh.ac.kr`, password: 'SesTest123!', passwordCheck: 'SesTest123!',
    } });
    expect(response.ok(), await response.text()).toBeTruthy();
  }
  async function login(number: string) {
    await page.goto('/login');
    await page.getByLabel('학번 / 교번').fill(number);
    await page.getByLabel('비밀번호', { exact: true }).fill('SesTest123!');
    await page.getByRole('button', { name: '로그인', exact: true }).click();
    await expect(page.getByRole('heading', { name: /님, 오늘도 한 걸음 더/ })).toBeVisible();
  }
  await login(teacherNumber);
  await page.goto('/lessons');
  await page.getByRole('button', { name: '강의실 만들기' }).click();
  await page.getByLabel('강의실 이름', { exact: true }).fill(`FE 연동 검증 ${suffix}`);
  await page.getByRole('button', { name: '저장하기', exact: true }).click();
  await expect(page).toHaveURL(/\/lessons\/\d+$/);
  const lessonUrl = page.url();
  await page.getByRole('button', { name: '과제 만들기' }).click();
  await page.getByLabel('제목', { exact: true }).fill('배열 합계 과제');
  await page.getByLabel('설명', { exact: true }).fill('배열의 합계를 출력하세요.');
  await page.getByLabel('마감 일시').fill('2099-12-31T23:59');
  await page.getByRole('button', { name: '저장하기', exact: true }).click();
  await expect(page).toHaveURL(/\/assignments\/\d+$/);
  await page.getByRole('button', { name: '테스트케이스', exact: true }).click();
  await page.getByRole('button', { name: '추가', exact: true }).click();
  await page.getByLabel('입력', { exact: true }).fill('1 2');
  await page.getByLabel('기대 출력', { exact: true }).fill('3');
  await page.getByRole('button', { name: '저장하기', exact: true }).click();
  await expect(page.getByText('테스트 1', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: '로그아웃', exact: true }).click();
  await login(studentNumber);
  await page.goto(lessonUrl);
  await page.getByRole('button', { name: '수강 신청하기' }).click();
  await expect(page.getByRole('button', { name: '승인 대기', exact: true })).toBeDisabled();
  await page.getByRole('button', { name: '로그아웃', exact: true }).click();
  await login(teacherNumber);
  await page.goto(lessonUrl);
  await page.getByRole('button', { name: '수강생 관리', exact: true }).click();
  await page.getByRole('button', { name: '승인', exact: true }).click();
  await expect(page.getByText('수강 신청을 승인했습니다.')).toBeVisible();
  await page.getByRole('button', { name: '로그아웃', exact: true }).click();
  await login(studentNumber);
  await page.goto('/lessons');
  await expect(page.getByRole('heading', { name: `FE 연동 검증 ${suffix}` })).toBeVisible();
  await page.goto('/problems');
  await expect(page.getByRole('button', { name: '문제 만들기' })).toHaveCount(0);
});
