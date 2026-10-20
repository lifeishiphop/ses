import { expect, test, type Page } from '@playwright/test';

const member = { id: 1, number: '20260001', name: '김서연', email: 's@kumoh.ac.kr', identity: 1 };
const problem = { id: 11, title: '두 정수의 합', description: '두 정수 A와 B를 입력받아 합을 출력하세요.\n\n입력\n첫 줄에 두 정수가 주어집니다.\n\n출력\n두 정수의 합을 출력합니다.', language: 0, uploader_id: 2, uploader: '이정훈', upload_time: '2026-10-20T10:00:00', solve: '-' };
const lessons = [
  { id: 1, name: '자료구조', year: '2026', semester: '2', owner: '이정훈', owner_id: 2 },
  { id: 2, name: '객체지향 프로그래밍', year: '2026', semester: '2', owner: '박민수', owner_id: 3 },
  { id: 3, name: '알고리즘', year: '2026', semester: '2', owner: '정하은', owner_id: 4 },
];
async function mockApi(page: Page, identity = 1) {
  const dueDate = new Date(Date.now() + 5 * 86400000).toISOString().slice(0, 19);
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname.replace('/api', '');
    const json = path === '/login' ? { token: 'test-token', memberDto: { ...member, identity } }
      : path === '/problem' ? [problem, { ...problem, id: 12, title: '문자열 뒤집기', language: 1 }, { ...problem, id: 13, title: '배열에서 최댓값 찾기', solve: 'o', language: 2 }]
      : path === '/problem/search' ? { problemDtos: [problem], totalPage: 1 }
      : path === '/problemDetail/11' ? { problem, submits: [] }
      : path.startsWith('/problem/run/') ? { language: 2, score: 100, result: '정답입니다.' }
      : path.startsWith('/problem/submit/') ? { language: 2, score: 100, result: '정답입니다.' }
      : path === '/lesson/s' || path === '/lesson' || path === '/lesson/t' ? lessons
      : path === '/myRegister' ? [{ id: 1, member_id: 1, lesson_id: 1, accept: 1 }]
      : path.match(/^\/lesson\/\d+\/assignment$/) ? [{ id: Number(path.split('/')[2]), title: ['연결 리스트 구현하기', '클래스와 상속', '이진 탐색 구현하기'][Number(path.split('/')[2]) - 1], description: '과제 설명', language: 0, lessonId: Number(path.split('/')[2]), end_time: dueDate, solve: '미제출' }]
      : path === '/lesson/1' ? lessons[0]
      : path === '/assignment/1' ? { id: 1, title: '연결 리스트 구현하기', description: '연결 리스트를 구현하세요.', language: 0, lessonId: 1, end_time: '2099-10-26T23:59:00', submit_contents: '#include <iostream>\nint main() {}', submit_language: 2, score: 100 }
      : [];
    await route.fulfill({ json });
  });
}
async function login(page: Page) {
  await page.goto('/login');
  await page.getByLabel('학번 / 교번').fill('20260001');
  await page.getByLabel('비밀번호', { exact: true }).fill('SesTest123!');
  await page.getByRole('button', { name: '로그인', exact: true }).click();
  await expect(page.getByRole('heading', { name: '김서연님, 오늘도 한 걸음 더.' })).toBeVisible();
}
test('login, learning dashboard, responsive layout and navigation', async ({ page }, testInfo) => {
  await mockApi(page);
  await login(page);
  await expect(page.getByRole('heading', { name: '다가오는 과제' })).toBeVisible();
  await expect(page.getByRole('heading', { name: '자료구조', exact: true })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy();
  await page.screenshot({ path: `test-results/dashboard-${testInfo.project.name}.png`, fullPage: true });
  if (testInfo.project.name === 'mobile') {
    await page.getByRole('button', { name: '메뉴 열기' }).click();
    await expect(page.getByRole('navigation', { name: '주 메뉴' })).toBeVisible();
  }
  await page.getByRole('link', { name: '문제 풀기', exact: true }).click();
  await page.getByRole('searchbox').fill('두 정수');
  await page.getByRole('button', { name: '검색', exact: true }).click();
  await page.getByRole('link', { name: '두 정수의 합', exact: true }).click();
  await expect(page.getByLabel('소스 코드')).toBeVisible();
});
test('language drafts survive switching and reload; run and submit use separate API calls', async ({ page }, testInfo) => {
  await mockApi(page);
  await login(page);
  await page.goto('/problems/11');
  await page.getByLabel('소스 코드').fill('public class Main { /* 나의 Java 코드 */ }');
  await page.getByLabel('코드 언어').selectOption('2');
  await page.getByLabel('소스 코드').fill('#include <iostream>\nint main() { std::cout << 3; }');
  await page.getByLabel('코드 언어').selectOption('1');
  await expect(page.getByLabel('소스 코드')).toContainText('나의 Java 코드');
  await page.getByLabel('코드 언어').selectOption('2');
  const runRequest = page.waitForRequest('**/api/problem/run/11');
  await page.getByRole('button', { name: '실행', exact: true }).click();
  expect((await runRequest).postDataJSON().language).toBe(2);
  await expect(page.getByText('실행 결과입니다.', { exact: false })).toBeVisible();
  const submitRequest = page.waitForRequest('**/api/problem/submit/11');
  await page.getByRole('button', { name: '제출하기' }).click();
  expect((await submitRequest).postDataJSON().submit_contents).toContain('std::cout');
  await expect(page.getByText('제출한 코드와 채점 결과가 저장되었습니다.')).toBeVisible();
  await page.screenshot({ path: `test-results/workspace-${testInfo.project.name}.png`, fullPage: true });
  await page.reload();
  await page.getByLabel('코드 언어').selectOption('2');
  await expect(page.getByLabel('소스 코드')).toContainText('std::cout');
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy();
});
test('expired sessions return to login and network errors can be retried', async ({ page }) => {
  await mockApi(page);
  await login(page);
  await page.route('**/api/problem', route => route.fulfill({ status: 502, body: '<html>unavailable</html>' }));
  await page.goto('/problems');
  await expect(page.getByRole('alert')).toContainText('요청을 처리하지 못했습니다.');
  await page.route('**/api/problem', route => route.fulfill({ status: 401, body: '' }));
  await page.getByRole('button', { name: '다시 불러오기' }).click();
  await expect(page.getByRole('button', { name: '로그인', exact: true })).toBeVisible();
});
test('assignment restores the actual submitted C++ language and score', async ({ page }) => {
  await mockApi(page);
  await login(page);
  await page.goto('/assignments/1');
  await expect(page.getByLabel('코드 언어')).toHaveValue('2');
  await expect(page.getByLabel('소스 코드')).toContainText('#include');
  await expect(page.getByText('최근 제출 결과')).toBeVisible();
  await expect(page.getByText('100점')).toBeVisible();
});
test('post and comment ownership, edit, cancel, and delete', async ({ page }) => {
  await mockApi(page);
  let content = '원래 내용';
  let commentExists = true;
  await page.route('**/api/board/1/**', async route => {
    const path = new URL(route.request().url()).pathname;
    if (path === '/api/board/1/edit/1') {
      content = route.request().postDataJSON().content;
      await route.fulfill({ json: { id: 1 } });
    } else if (path === '/api/board/1/1/delete/2') {
      commentExists = false;
      await route.fulfill({ status: 200, body: '' });
    } else await route.fulfill({ json: {
      post: { id: 1, title: '반복문 질문', content, writer: member.name, writer_id: 1, upload_time: '2026-10-20T12:00:00', views: 2 },
      comments: commentExists ? [{ id: 2, content: '도움이 되는 답변', writer: member.name, writer_id: 1, upload_time: '2026-10-20T13:00:00' }, { id: 3, content: '다른 회원의 답변', writer: '다른 회원', writer_id: 9, upload_time: '2026-10-20T13:00:00' }] : [],
    } });
  });
  await login(page);
  await page.goto('/community/1/1');
  await page.locator('article').getByRole('button', { name: '수정', exact: true }).click();
  await page.getByLabel('내용', { exact: true }).fill('수정한 질문 내용');
  await page.getByRole('button', { name: '게시글 저장' }).click();
  await expect(page.getByText('수정한 질문 내용')).toBeVisible();
  const own = page.locator('.comment-row').filter({ hasText: '도움이 되는 답변' });
  const other = page.locator('.comment-row').filter({ hasText: '다른 회원의 답변' });
  await expect(other.getByRole('button', { name: '삭제', exact: true })).toHaveCount(0);
  await own.getByRole('button', { name: '삭제', exact: true }).click();
  await page.getByRole('button', { name: '취소', exact: true }).click();
  await expect(own).toBeVisible();
  await own.getByRole('button', { name: '삭제', exact: true }).click();
  await page.getByRole('button', { name: '삭제하기', exact: true }).click();
  await expect(page.getByText('도움이 되는 답변')).toHaveCount(0);
});
