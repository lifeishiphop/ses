import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, Plus } from 'lucide-react';
import { useUser } from '../App';
import { api, languageName } from '../api';
import type { Assignment, PageResult, Problem } from '../types';
import { Badge, Empty, Feedback, Field, Modal, PageHeading, Pagination, ResourceState, Search, useAction, useResource, values } from '../ui';

export function TaskEditor({ kind, item, lessonId, close, saved }: {
  kind: 'problem' | 'assignment'; item?: Problem | Assignment; lessonId?: number;
  close: () => void; saved: (item: Problem | Assignment) => void;
}) {
  const action = useAction();
  const label = kind === 'problem' ? '문제' : '과제';
  return <Modal title={`${label} ${item ? '수정' : '만들기'}`} close={close} busy={action.busy}>
    <form className="stack" onSubmit={e => {
      const data = values(e);
      void action.run(async () => {
        if (!data.title.trim() || !data.description.trim()) throw new Error('제목과 설명을 입력해 주세요.');
        const result = await api<Problem | Assignment>(item ? `/${kind}/edit/${item.id}` : kind === 'problem' ? '/problem/write' : '/assignment/add', {
          method: item ? 'PUT' : 'POST',
          body: { ...data, title: data.title.trim(), language: Number(data.language), ...(kind === 'assignment' ? { lessonId, end_time: `${data.end_time}:00` } : {}) },
        });
        saved(result);
      });
    }}>
      <Field label="제목"><input name="title" defaultValue={item?.title} required maxLength={100} /></Field>
      <Field label="설명" hint="문제 조건, 입력과 출력 형식을 자세히 작성해 주세요."><textarea name="description" defaultValue={item?.description} rows={9} required /></Field>
      <Field label="허용 언어"><select name="language" defaultValue={item?.language ?? 0}><option value="0">Java · C++</option><option value="1">Java</option><option value="2">C++</option></select></Field>
      {kind === 'assignment' && <Field label="마감 일시" hint="서버와 같은 시간대를 기준으로 입력해 주세요."><input type="datetime-local" name="end_time" defaultValue={item && 'end_time' in item ? item.end_time.slice(0, 16) : ''} required /></Field>}
      <Feedback action={action} /><div className="form-actions"><button type="button" className="button secondary" disabled={action.busy} onClick={close}>취소</button><button className="button primary" disabled={action.busy}>{action.busy ? '저장 중…' : '저장하기'}</button></div>
    </form>
  </Modal>;
}
export function Problems() {
  const user = useUser();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [tab, setTab] = useState('all');
  const [language, setLanguage] = useState('all');
  const [editor, setEditor] = useState(false);
  const resource = useResource<Problem[] | PageResult>(tab === 'mine' ? '/myProblem' : query ? `/problem/search?keyword=${encodeURIComponent(query)}&page=${page}` : '/problem');
  const all = Array.isArray(resource.data) ? resource.data : resource.data?.problemDtos;
  const list = all?.filter(item => (tab !== 'solved' || item.solve === 'o') && (tab !== 'unsolved' || item.solve !== 'o') && (language === 'all' || item.language === 0 || item.language === Number(language)));
  return <><PageHeading eyebrow="PRACTICE MAKES PROGRESS" title="문제 풀기" description="코드로 생각하고, 실행하며 배우세요. 한 문제씩 나만의 실력을 쌓아갑니다.">{user.identity !== 1 && <button className="button primary" onClick={() => setEditor(true)}><Plus size={18} />문제 만들기</button>}</PageHeading>
    <div className="practice-intro"><CodeMark /><div><strong>작은 도전, 꾸준한 성장</strong><p>Java와 C++로 문제를 풀고 채점 결과를 바로 확인하세요.</p></div><Badge tone="green">CODE & LEARN</Badge></div>
    <section className="panel list-panel"><div className="toolbar"><div className="tabs" aria-label="문제 분류">{[['all', '전체 문제'], ['unsolved', '도전할 문제'], ['solved', '해결한 문제'], ...(user.identity !== 1 ? [['mine', '내가 만든 문제']] : [])].map(([value, label]) => <button className={tab === value ? 'active' : ''} key={value} onClick={() => { setTab(value); setPage(1); }} aria-pressed={tab === value}>{label}</button>)}</div><div className="filter-row"><Search onSearch={value => { setQuery(value); setPage(1); if (tab === 'mine') setTab('all'); }} /><select aria-label="문제 언어" value={language} onChange={e => setLanguage(e.target.value)}><option value="all">모든 언어</option><option value="1">Java</option><option value="2">C++</option></select></div></div>
      <ResourceState resource={resource} />{list?.length === 0 && <Empty title={query ? '검색 결과가 없습니다.' : '아직 표시할 문제가 없습니다.'}>{query ? '다른 검색어나 필터로 다시 찾아보세요.' : '문제가 등록되면 이곳에서 도전할 수 있어요.'}</Empty>}
      {!!list?.length && <div className="table-wrap"><table><thead><tr><th>번호</th><th>문제</th><th>언어</th><th>출제자</th><th>풀이 상태</th><th><span className="sr-only">문제 열기</span></th></tr></thead><tbody>{list.map(item => <tr key={item.id}><td className="mono muted">{String(item.id).padStart(3, '0')}</td><td><Link className="table-title" to={`/problems/${item.id}`}>{item.title}</Link></td><td><Badge>{languageName(item.language)}</Badge></td><td>{item.uploader}</td><td><Badge tone={item.solve === 'o' ? 'green' : item.solve === 'x' ? 'orange' : ''}>{item.solve === 'o' ? '해결 완료' : item.solve === 'x' ? '다시 도전' : '미풀이'}</Badge></td><td><Link className="icon-button" aria-label={`${item.title} 풀기`} to={`/problems/${item.id}`}><ArrowRight size={17} /></Link></td></tr>)}</tbody></table></div>}
      <Pagination page={page} total={!Array.isArray(resource.data) ? resource.data?.totalPage || 0 : 0} setPage={setPage} />
    </section>{editor && <TaskEditor kind="problem" close={() => setEditor(false)} saved={item => navigate(`/problems/${item.id}`)} />}</>;
}

function CodeMark() { return <span className="practice-code" aria-hidden="true">&lt;/&gt;</span>; }
