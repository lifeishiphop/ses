import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { api, deadline, passwordError, saveSession } from './api';

const storage = new Map<string, string>();
beforeEach(() => {
  storage.clear();
  vi.stubGlobal('sessionStorage', {
    getItem: (key: string) => storage.get(key) ?? null,
    setItem: (key: string, value: string) => storage.set(key, value),
    removeItem: (key: string) => storage.delete(key),
  });
  vi.stubGlobal('window', { dispatchEvent: vi.fn() });
});
afterEach(() => vi.unstubAllGlobals());
describe('API client', () => {
  it('attaches the session token and preserves the exact submission payload', async () => {
    saveSession({ token: 'jwt', memberDto: { id: 1, number: 's1', name: '학생', email: 's@kumoh.ac.kr', identity: 1 } });
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ score: 100 })));
    vi.stubGlobal('fetch', fetchMock);
    await expect(api('/problem/submit/7', { method: 'POST', body: { language: 2, submit_contents: 'int main() {}' } })).resolves.toEqual({ score: 100 });
    expect(fetchMock.mock.calls[0][0]).toBe('/api/problem/submit/7');
    expect(fetchMock.mock.calls[0][1].headers.Authorization).toBe('Bearer jwt');
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ language: 2, submit_contents: 'int main() {}' });
  });
  it('clears an expired session and surfaces a readable error', async () => {
    storage.set('ses.session', JSON.stringify({ token: 'expired', memberDto: {} }));
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('', { status: 401 })));
    await expect(api('/problem')).rejects.toThrow('로그인이 만료');
    expect(storage.has('ses.session')).toBe(false);
  });
  it('accepts empty delete responses and exposes backend errors', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValueOnce(new Response('', { status: 200 })).mockResolvedValueOnce(new Response(JSON.stringify({ 'error - ': '작성자가 아닙니다.' }), { status: 400 })));
    await expect(api('/problem/delete/1', { method: 'DELETE' })).resolves.toBeNull();
    await expect(api('/problem/edit/1', { method: 'PUT' })).rejects.toThrow('작성자가 아닙니다.');
  });
  it('handles a non-JSON proxy failure without showing HTML to the user', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('<html>proxy error</html>', { status: 502 })));
    await expect(api('/problem')).rejects.toThrow('요청을 처리하지 못했습니다.');
  });
});
describe('input and deadline boundaries', () => {
  it('distinguishes overdue, today, and upcoming deadlines', () => {
    const now = new Date('2026-10-21T12:00:00').getTime();
    expect(deadline('2026-10-21T11:59:59', now)).toBe('마감');
    expect(deadline('2026-10-21T23:59:59', now)).toBe('오늘 마감');
    expect(deadline('2026-10-23T12:00:00', now)).toBe('D-2');
    expect(deadline('invalid', now)).toBe('마감일 미정');
  });
  it('matches server password requirements and checks confirmation', () => {
    expect(passwordError('SesTest123!', 'SesTest123!')).toBe('');
    expect(passwordError('password123', 'password123')).toContain('특수문자');
    expect(passwordError('SesTest123!', 'other')).toContain('일치');
  });
});
