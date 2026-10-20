import { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Link, NavLink, Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { ArrowUpRight, BookOpen, Code2, GraduationCap, LayoutDashboard, LogOut, Menu, MessageSquare, Settings, ShieldCheck, X } from 'lucide-react';
import { getSession, saveSession } from './api';
import type { Member } from './types';
import { Auth, Account } from './pages/Account';
import { Dashboard } from './pages/Dashboard';
import { Problems } from './pages/Problems';
import { Workspace } from './pages/Workspace';
import { LessonDetail, Lessons } from './pages/Lessons';
import { Community, PostDetail } from './pages/Community';
import { Members } from './pages/Members';
import { Empty } from './ui';

const UserContext = createContext<Member | null>(null);
export function useUser() { return useContext(UserContext)!; }
const nav = [
  ['/', '학습 홈', LayoutDashboard],
  ['/problems', '문제 풀기', Code2],
  ['/lessons', '강의실', BookOpen],
  ['/community', '커뮤니티', MessageSquare],
] as const;

function Shell() {
  const user = useUser();
  const [open, setOpen] = useState(false);
  const [mobile, setMobile] = useState(() => window.matchMedia('(max-width: 900px)').matches);
  const sidebar = useRef<HTMLElement>(null);
  const menuButton = useRef<HTMLButtonElement>(null);
  const location = useLocation();
  useEffect(() => {
    const query = window.matchMedia('(max-width: 900px)');
    const sync = () => setMobile(query.matches);
    query.addEventListener('change', sync);
    return () => query.removeEventListener('change', sync);
  }, []);
  useEffect(() => {
    if (!open || !mobile) return;
    const elements = sidebar.current!.querySelectorAll<HTMLElement>('a, button:not(:disabled)');
    elements[0]?.focus();
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') { setOpen(false); requestAnimationFrame(() => menuButton.current?.focus()); }
      if (event.key === 'Tab') {
        const first = elements[0], last = elements[elements.length - 1];
        if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
        else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
      }
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, mobile]);
  useEffect(() => {
    setOpen(false);
    requestAnimationFrame(() => document.getElementById('main-content')?.focus({ preventScroll: true }));
  }, [location.pathname]);
  const current = nav.find(([path]) => path === '/' ? location.pathname === '/' : location.pathname.startsWith(path));
  return <div className="app-shell">
    <a href="#main-content" className="skip-link">본문으로 건너뛰기</a>
    {open && <button className="sidebar-scrim" aria-label="메뉴 닫기" onClick={() => setOpen(false)} />}
    <aside ref={sidebar} inert={mobile && !open} className={`sidebar ${open ? 'is-open' : ''}`}>
      {mobile && <button className="icon-button sidebar-close" aria-label="메뉴 닫기" onClick={() => { setOpen(false); requestAnimationFrame(() => menuButton.current?.focus()); }}><X size={20} /></button>}
      <Link className="brand" to="/"><span className="brand-symbol"><Code2 size={25} /></span><span>S.E.S<span className="brand-caption">LEARN. CODE. GROW.</span></span></Link>
      <p className="nav-label">MY WORKSPACE</p>
      <nav aria-label="주 메뉴">{nav.map(([path, label, Icon]) => <NavLink end={path === '/'} to={path} key={path}><Icon size={20} /><span>{label}</span></NavLink>)}{user.identity === 0 && <NavLink to="/members"><ShieldCheck size={20} />회원 관리</NavLink>}</nav>
      <div className="sidebar-bottom">
        <div className="sidebar-note"><GraduationCap size={25} /><strong>한 줄의 코드에서,<br />다음 가능성으로.</strong><p>배우고 도전하는<br />모든 순간을 함께합니다.</p></div>
        <NavLink className="settings-link" to="/account"><Settings size={19} />계정 설정</NavLink>
        <div className="profile"><div className="avatar">{user.name.slice(0, 1)}</div><div><strong>{user.name}</strong><span>{user.identity === 2 ? '강사' : user.identity === 0 ? '관리자' : '학생'} · {user.number}</span></div><button className="icon-button" aria-label="로그아웃" onClick={() => saveSession(null)}><LogOut size={18} /></button></div>
      </div>
    </aside>
    <div className="app-main" inert={mobile && open}>
      <header className="topbar"><div className="topbar-location"><button ref={menuButton} className="icon-button mobile-menu" onClick={() => setOpen(!open)} aria-label={open ? '메뉴 닫기' : '메뉴 열기'} aria-expanded={open}>{open ? <X /> : <Menu />}</button><span className="muted">나의 학습 공간</span><span className="breadcrumb-slash">/</span><strong>{current?.[1] || (location.pathname === '/account' ? '계정 설정' : '워크스페이스')}</strong></div><Link to="/problems" className="topbar-link">오늘도 한 걸음 <ArrowUpRight size={16} /></Link></header>
      <main id="main-content" className="main-content" tabIndex={-1}><Outlet /></main>
      <footer className="footer"><span>S.E.S · Software Engineering Supporter</span><span>함께 배우는 소프트웨어 학습 공간</span></footer>
    </div>
  </div>;
}
export default function App() {
  const [session, setSession] = useState(getSession);
  useEffect(() => {
    const sync = () => setSession(getSession());
    window.addEventListener('ses-session', sync);
    return () => window.removeEventListener('ses-session', sync);
  }, []);
  return <UserContext.Provider value={session?.memberDto || null}><Routes>
    <Route path="/login" element={session ? <Navigate to="/" replace /> : <Auth />} />
    <Route element={session ? <Shell /> : <Navigate to="/login" replace />}>
      <Route index element={<Dashboard />} />
      <Route path="problems" element={<Problems />} />
      <Route path="problems/:id" element={<Workspace kind="problem" />} />
      <Route path="lessons" element={<Lessons />} />
      <Route path="lessons/:id" element={<LessonDetail />} />
      <Route path="assignments/:id" element={<Workspace kind="assignment" />} />
      <Route path="community" element={<Community />} />
      <Route path="community/:boardId/:postId" element={<PostDetail />} />
      <Route path="account" element={<Account />} />
      <Route path="members" element={session?.memberDto.identity === 0 ? <Members /> : <Navigate to="/" replace />} />
      <Route path="*" element={<Empty title="페이지를 찾을 수 없습니다." to="/" label="학습 홈으로">주소를 확인하거나 학습 홈에서 다시 시작해 주세요.</Empty>} />
    </Route>
  </Routes></UserContext.Provider>;
}
