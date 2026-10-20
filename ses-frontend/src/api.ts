import type { Session } from './types';

const SESSION_KEY = 'ses.session';
export class ApiError extends Error {
  constructor(message: string, public status: number) { super(message); }
}
export function getSession(): Session | null {
  try {
    const value = JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null') as Session | null;
    return value?.token && value.memberDto ? value : null;
  } catch { return null; }
}
export function saveSession(session: Session | null) {
  if (session) sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  else sessionStorage.removeItem(SESSION_KEY);
  window.dispatchEvent(new Event('ses-session'));
}
export async function api<T>(path: string, options: { method?: string; body?: unknown; signal?: AbortSignal } = {}): Promise<T> {
  const session = getSession();
  let response: Response;
  try {
    response = await fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}${path}`, {
      method: options.method || 'GET',
      headers: {
        ...(options.body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...(session ? { Authorization: `Bearer ${session.token}` } : {}),
      },
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
      signal: options.signal,
    });
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') throw error;
    throw new ApiError('서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.', 0);
  }
  const text = await response.text();
  let data: Record<string, unknown> | null = null;
  try { data = text ? JSON.parse(text) : null; } catch { /* Handle proxy HTML errors below. */ }
  if (!response.ok) {
    if (response.status === 401 && path !== '/login') saveSession(null);
    const message = response.status === 401
      ? (path === '/login' ? '학번과 비밀번호를 확인해 주세요.' : '로그인이 만료되었습니다. 다시 로그인해 주세요.')
      : typeof data?.['error - '] === 'string' ? data['error - ']
      : typeof data?.message === 'string' ? data.message
      : '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.';
    throw new ApiError(message, response.status);
  }
  if (text && data === null) throw new ApiError('서버 응답을 읽지 못했습니다. API 연결 설정을 확인해 주세요.', response.status);
  return data as T;
}
export const languageName = (value: number) => value === 1 ? 'Java' : value === 2 ? 'C++' : 'Java · C++';
export function formatDate(value?: string, withTime = false) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long', day: 'numeric', ...(withTime ? { hour: '2-digit', minute: '2-digit' } as const : {}),
  }).format(date);
}
export function deadline(value: string, now = Date.now()) {
  const remaining = new Date(value).getTime() - now;
  if (!Number.isFinite(remaining)) return '마감일 미정';
  if (remaining <= 0) return '마감';
  return remaining < 86400000 ? '오늘 마감' : `D-${Math.ceil(remaining / 86400000)}`;
}
export function passwordError(password: string, confirmation: string) {
  if (!/^(?=.{8,30}$)(?=.*\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$/.test(password)) {
    return '비밀번호는 영문, 숫자, 특수문자(!@#$%^&+=)를 포함해 8~30자로 입력해 주세요.';
  }
  return password !== confirmation ? '비밀번호가 일치하지 않습니다.' : '';
}
