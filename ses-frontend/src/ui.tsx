import { cloneElement, useEffect, useId, useRef, useState, type FormEvent, type ReactElement, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, ArrowRight, BookOpen, LoaderCircle, X } from 'lucide-react';
import { api } from './api';

export function useResource<T>(path: string | null) {
  const [data, setData] = useState<T>();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [version, setVersion] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setData(undefined); setError(''); setLoading(!!path);
    if (path) api<T>(path, { signal: controller.signal })
      .then(value => { if (!controller.signal.aborted) setData(value); })
      .catch((e: Error) => { if (!controller.signal.aborted) setError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [path, version]);
  return { data, error, loading, reload: () => setVersion(v => v + 1) };
}
export function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const lock = useRef(false);
  async function run(action: () => Promise<void>, message = '') {
    if (lock.current) return;
    lock.current = true; setBusy(true); setError(''); setSuccess('');
    try { await action(); setSuccess(message); }
    catch (e) { setError(e instanceof Error ? e.message : '요청을 처리하지 못했습니다.'); }
    finally { lock.current = false; setBusy(false); }
  }
  return { busy, error, success, run, clear: () => { setError(''); setSuccess(''); } };
}
export function Notice({ children, error = false }: { children: ReactNode; error?: boolean }) {
  return <div className={`notice ${error ? 'error' : ''}`} role={error ? 'alert' : 'status'}><AlertCircle size={18} aria-hidden="true" /><span>{children}</span></div>;
}
export function Feedback({ action }: { action: ReturnType<typeof useAction> }) {
  return <>{action.error && <Notice error>{action.error}</Notice>}{action.success && <Notice>{action.success}</Notice>}</>;
}
export function Loading() { return <div className="loading" role="status"><LoaderCircle size={24} className="spin" /> 불러오는 중입니다.</div>; }
export function ResourceState({ resource }: { resource: { loading: boolean; error: string; reload: () => void } }) {
  if (resource.loading) return <Loading />;
  if (resource.error) return <div className="stack"><Notice error>{resource.error}</Notice><button className="button secondary" onClick={resource.reload}>다시 불러오기</button></div>;
  return null;
}
export function Empty({ title, children, to, label }: { title: string; children?: ReactNode; to?: string; label?: string }) {
  return <div className="empty"><div className="empty-icon"><BookOpen size={26} /></div><h3>{title}</h3><p>{children}</p>{to && <Link className="button secondary" to={to}>{label}<ArrowRight size={16} /></Link>}</div>;
}
export function PageHeading({ eyebrow, title, description, children }: { eyebrow: string; title: string; description: string; children?: ReactNode }) {
  return <div className="page-heading"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p className="muted">{description}</p></div>{children && <div className="heading-actions">{children}</div>}</div>;
}
export function Badge({ children, tone = '' }: { children: ReactNode; tone?: string }) { return <span className={`badge ${tone}`}>{children}</span>; }
export function Field({ label, hint, children }: { label: string; hint?: string; children: ReactNode }) {
  const id = useId();
  return <div className="field"><label htmlFor={id}>{label}</label>{cloneElement(children as ReactElement<{ id: string; 'aria-describedby'?: string }>, { id, 'aria-describedby': hint ? `${id}-hint` : undefined })}{hint && <small id={`${id}-hint`}>{hint}</small>}</div>;
}
export function Search({ onSearch, placeholder = '제목이나 내용으로 검색' }: { onSearch: (value: string) => void; placeholder?: string }) {
  return <form className="search-form" onSubmit={e => { e.preventDefault(); onSearch(String(new FormData(e.currentTarget).get('keyword') || '').trim()); }}>
    <input name="keyword" type="search" aria-label={placeholder} placeholder={placeholder} /><button className="button secondary">검색</button>
  </form>;
}
export function Pagination({ page, total, setPage }: { page: number; total: number; setPage: (page: number) => void }) {
  if (total <= 1) return null;
  return <nav className="pagination" aria-label="페이지 이동"><button className="button secondary" disabled={page <= 1} onClick={() => setPage(page - 1)}>이전</button><span>{page} / {total}</span><button className="button secondary" disabled={page >= total} onClick={() => setPage(page + 1)}>다음</button></nav>;
}
export function Modal({ title, children, close, busy = false }: { title: string; children: ReactNode; close: () => void; busy?: boolean }) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => { const dialog = ref.current!; dialog.showModal(); return () => dialog.close(); }, []);
  return <dialog ref={ref} className="modal" aria-label={title} onCancel={e => { e.preventDefault(); if (!busy) close(); }}>
    <div className="modal-heading"><h2>{title}</h2><button type="button" className="icon-button" aria-label="닫기" disabled={busy} onClick={close}><X size={20} /></button></div>{children}
  </dialog>;
}
export function values(e: FormEvent<HTMLFormElement>) {
  e.preventDefault();
  return Object.fromEntries(new FormData(e.currentTarget).entries()) as Record<string, string>;
}
export function ConfirmDelete({ label, onDelete }: { label: string; onDelete: () => Promise<void> }) {
  const [open, setOpen] = useState(false);
  const action = useAction();
  return <><button type="button" className="button danger-quiet" onClick={() => setOpen(true)}>삭제</button>{open && <Modal title={`${label} 삭제`} close={() => setOpen(false)} busy={action.busy}><p>삭제한 내용은 복구할 수 없습니다. 삭제하시겠습니까?</p><Feedback action={action} /><div className="form-actions"><button type="button" className="button secondary" disabled={action.busy} onClick={() => setOpen(false)}>취소</button><button type="button" className="button danger" disabled={action.busy} onClick={() => action.run(async () => { await onDelete(); setOpen(false); })}>{action.busy ? '삭제 중…' : '삭제하기'}</button></div></Modal>}</>;
}
