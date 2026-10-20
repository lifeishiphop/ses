import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Play, Plus, Send } from 'lucide-react';
import { useUser } from '../App';
import { api, deadline, formatDate, languageName } from '../api';
import type { Assignment, Lesson, Problem, Student, Submission, Testcase } from '../types';
import { Badge, ConfirmDelete, Empty, Feedback, Field, Modal, Notice, ResourceState, useAction, useResource, values } from '../ui';
import { TaskEditor } from './Problems';

const templates: Record<number, string> = {
  1: 'public class Main {\n    public static void main(String[] args) {\n        // 코드를 작성해 주세요.\n    }\n}\n',
  2: '#include <iostream>\nusing namespace std;\n\nint main() {\n    // 코드를 작성해 주세요.\n    return 0;\n}\n',
};
export function Workspace({ kind }: { kind: 'problem' | 'assignment' }) {
  const { id } = useParams();
  return <TaskWorkspace key={`${kind}-${id}`} kind={kind} id={id!} />;
}
function TaskWorkspace({ kind, id }: { kind: 'problem' | 'assignment'; id: string }) {
  const user = useUser();
  const navigate = useNavigate();
  const resource = useResource<{ problem: Problem; submits: Submission[] } | Assignment>(kind === 'problem' ? `/problemDetail/${id}` : `/assignment/${id}`);
  const item = resource.data && ('problem' in resource.data ? resource.data.problem : resource.data);
  const lesson = useResource<Lesson>(item && 'lessonId' in item ? `/lesson/${item.lessonId}` : null);
  const canManage = item && ('uploader_id' in item ? item.uploader_id === user.id : lesson.data?.owner_id === user.id);
  const [language, setLanguage] = useState(1);
  const [drafts, setDrafts] = useState<Record<number, string>>({});
  const [initialized, setInitialized] = useState(false);
  const [result, setResult] = useState<Submission>();
  const [resultAction, setResultAction] = useState('');
  const [editor, setEditor] = useState(false);
  const [tab, setTab] = useState('description');
  const [draftWarning, setDraftWarning] = useState('');
  const action = useAction();
  const draftKey = `ses.code.${user.number}.${kind}.${id}`;
  useEffect(() => {
    if (!resource.data || initialized) return;
    const value = 'problem' in resource.data ? resource.data.problem : resource.data;
    const saved: Record<number, string> = {};
    if ('submits' in resource.data) resource.data.submits.forEach(s => { saved[s.language] = s.submit_contents; });
    else if (resource.data.submit_contents) saved[resource.data.submit_language || value.language || 1] = resource.data.submit_contents;
    try {
      const local = JSON.parse(sessionStorage.getItem(draftKey) || '{}');
      for (const lang of [1, 2]) if (typeof local[lang] === 'string') saved[lang] = local[lang];
    } catch { setDraftWarning('임시 코드를 읽지 못했습니다. 제출 전에 코드를 따로 보관해 주세요.'); }
    setDrafts(saved);
    setLanguage(value.language || ('submit_language' in resource.data ? resource.data.submit_language : undefined) || 1);
    setInitialized(true);
    if ('score' in resource.data && resource.data.score != null) {
      setResult({ language: resource.data.submit_language || 1, submit_contents: resource.data.submit_contents || '', score: resource.data.score, result: '최근 제출 결과' });
      setResultAction('submit');
    }
  }, [resource.data, initialized, draftKey]);
  useEffect(() => {
    if (item?.language && item.language !== language) setLanguage(item.language);
  }, [item?.language, language]);
  const updateCode = (code: string) => {
    const next = { ...drafts, [language]: code };
    setDrafts(next);
    try { sessionStorage.setItem(draftKey, JSON.stringify(next)); }
    catch { setDraftWarning('임시 저장 공간이 부족합니다. 제출 전에 코드를 따로 보관해 주세요.'); }
  };
  const code = drafts[language] ?? templates[language];
  const closed = item && 'end_time' in item && deadline(item.end_time) === '마감';
  const judge = (mode: 'run' | 'submit') => action.run(async () => {
    setResult(undefined);
    if (!code.trim()) throw new Error('실행할 코드를 입력해 주세요.');
    if (new TextEncoder().encode(code).length > 65536) throw new Error('코드는 UTF-8 기준 64 KiB 이하여야 합니다.');
    const response = await api<Submission>(`/${kind}/${mode}/${id}`, { method: 'POST', body: { language, submit_contents: code } });
    setResult(response); setResultAction(mode);
    if (mode === 'submit') resource.reload();
  });
  return <><Link className="back-link" to={kind === 'problem' ? '/problems' : item && 'lessonId' in item ? `/lessons/${item.lessonId}` : '/lessons'}><ArrowLeft size={16} />{kind === 'problem' ? '문제 목록' : '강의실'}으로</Link><ResourceState resource={resource} />
    {item && <><div className="workspace-heading"><div><div className="inline-group"><span className="eyebrow">{kind === 'problem' ? 'PROBLEM' : 'ASSIGNMENT'} {String(item.id).padStart(3, '0')}</span><Badge>{languageName(item.language)}</Badge>{'end_time' in item && <Badge tone={closed ? '' : 'orange'}>{deadline(item.end_time)}</Badge>}</div><h1>{item.title}</h1><p className="muted">{'uploader' in item ? `${item.uploader} 출제` : `${formatDate(item.end_time, true)}까지 제출`}</p></div>{canManage && <div className="inline-group"><button className="button secondary" onClick={() => setEditor(true)}>수정</button><ConfirmDelete label={kind === 'problem' ? '문제' : '과제'} onDelete={async () => { await api(`/${kind}/delete/${id}`, { method: 'DELETE' }); navigate(kind === 'problem' ? '/problems' : `/lessons/${(item as Assignment).lessonId}`); }} /></div>}</div>
      <div className="workspace-grid"><section className="panel problem-description"><div className="tabs"><button className={tab === 'description' ? 'active' : ''} onClick={() => setTab('description')}>문제 설명</button>{canManage && <><button className={tab === 'testcases' ? 'active' : ''} onClick={() => setTab('testcases')}>테스트케이스</button>{kind === 'assignment' && <button className={tab === 'scores' ? 'active' : ''} onClick={() => setTab('scores')}>제출·채점</button>}</>}</div>
        {tab === 'description' && <div className="description-body"><h2>문제 설명</h2><div className="prose">{item.description}</div><div className="problem-guidance"><h3>제출 안내</h3><p>Java는 package 선언 없는 Main 클래스, C++는 C++17 단일 소스로 작성해 주세요.</p><p>‘실행’으로 테스트 결과를 확인한 뒤 ‘제출하기’를 눌러 풀이를 저장하세요.</p></div>{kind === 'problem' && resource.data && 'submits' in resource.data && resource.data.submits.length > 0 && <div className="saved-submissions"><h3>언어별 최근 제출</h3><p className="muted small">같은 언어로 다시 제출하면 최근 코드가 갱신됩니다.</p>{resource.data.submits.map(s => <details key={s.language}><summary>{languageName(s.language)} 제출 코드</summary><pre className="code-preview">{s.submit_contents}</pre></details>)}</div>}</div>}
        {canManage && tab === 'testcases' && <Testcases kind={kind} id={id} />}{canManage && tab === 'scores' && <Scores id={id} />}
      </section><section className="editor-column"><div className="code-editor"><div className="editor-toolbar"><span className="mono">{language === 1 ? 'Main.java' : 'main.cpp'}</span><select aria-label="코드 언어" value={language} disabled={action.busy} onChange={e => { setLanguage(Number(e.target.value)); setResult(undefined); }}>{[1, 2].filter(lang => !item.language || item.language === lang).map(lang => <option key={lang} value={lang}>{languageName(lang)}</option>)}</select></div><label className="sr-only" htmlFor="source-code">소스 코드</label><textarea id="source-code" className="source-code" value={code} onChange={e => updateCode(e.target.value)} spellCheck={false} autoCapitalize="off" autoCorrect="off" disabled={action.busy} /><div className="editor-status"><span>UTF-8</span><span>{code.split('\n').length} lines</span><span>이 탭에 임시 저장</span></div></div>
        {draftWarning && <Notice error>{draftWarning}</Notice>}
        <div className="editor-actions"><span className="muted small">{closed ? '마감된 과제입니다.' : '실행 후 제출을 잊지 마세요.'}</span><button className="button secondary" disabled={action.busy} onClick={() => judge('run')}><Play size={16} />{action.busy ? '채점 중…' : '실행'}</button><button className="button primary" disabled={action.busy || !!closed} onClick={() => judge('submit')}><Send size={16} />제출하기</button></div>
        <Feedback action={action} /><div className="panel result-panel" aria-live="polite"><h2>실행 · 제출 결과</h2>{action.busy ? <p className="muted">코드를 컴파일하고 테스트를 실행하고 있어요. 잠시만 기다려 주세요.</p> : result ? <><div className={`judge-result ${result.score === 100 ? 'correct' : ''}`}><CheckCircle2 size={22} /><strong>{result.result || '채점이 완료되었습니다.'}</strong><Badge tone={result.score === 100 ? 'green' : 'orange'}>{result.score === -1 ? '테스트 없음' : `${result.score ?? '—'}점`}</Badge></div><p className="muted small">{resultAction === 'run' ? '실행 결과입니다. 풀이를 저장하려면 제출하기를 눌러 주세요.' : '제출한 코드와 채점 결과가 저장되었습니다.'}</p></> : <p className="muted">코드를 실행하면 이곳에 결과가 표시됩니다.</p>}</div>
      </section></div>
      {editor && <TaskEditor kind={kind} item={item} lessonId={'lessonId' in item ? item.lessonId : undefined} close={() => setEditor(false)} saved={() => { setEditor(false); resource.reload(); }} />}
    </>}
  </>;
}
function Testcases({ kind, id }: { kind: 'problem' | 'assignment'; id: string }) {
  const resource = useResource<Testcase[]>(`/${kind}/${id}/testcase`);
  const [editing, setEditing] = useState<Testcase | 'new'>();
  const action = useAction();
  return <div className="description-body stack"><div className="section-heading"><h2>테스트케이스</h2><button className="button secondary" onClick={() => { action.clear(); setEditing('new'); }} disabled={(resource.data?.length ?? 0) >= 20}><Plus size={16} />추가</button></div><p className="muted small">최대 20개 · 입력과 기대 출력은 각각 UTF-8 64 KiB 이하</p><ResourceState resource={resource} />{resource.data?.length === 0 && <Empty title="테스트케이스를 추가해 주세요.">자동 채점에 사용할 입력과 기대 출력이 필요합니다.</Empty>}
    {resource.data?.map((testcase, i) => <div className="testcase-card" key={testcase.id}><div className="section-heading"><strong>테스트 {i + 1}</strong><div className="inline-group"><button className="text-button" onClick={() => { action.clear(); setEditing(testcase); }}>수정</button><ConfirmDelete label="테스트케이스" onDelete={async () => { await api(`/${kind}/${id}/testcase/delete/${testcase.id}`, { method: 'DELETE' }); resource.reload(); }} /></div></div><div className="testcase-grid"><div><small>입력</small><pre>{testcase.input || '(빈 입력)'}</pre></div><div><small>기대 출력</small><pre>{testcase.output || '(빈 출력)'}</pre></div></div></div>)}
    {editing && <Modal title={`테스트케이스 ${editing === 'new' ? '추가' : '수정'}`} busy={action.busy} close={() => setEditing(undefined)}><form className="stack" onSubmit={e => {
      const data = values(e);
      void action.run(async () => {
        if ([data.input, data.output].some(s => new TextEncoder().encode(s).length > 65536)) throw new Error('입력과 기대 출력은 각각 64 KiB 이하여야 합니다.');
        await api(`/${kind}/${id}/testcase/${editing === 'new' ? 'add' : `edit/${editing.id}`}`, { method: editing === 'new' ? 'POST' : 'PUT', body: { ...data, [`${kind}_id`]: Number(id) } });
        setEditing(undefined); resource.reload();
      });
    }}><Field label="입력"><textarea name="input" rows={5} defaultValue={editing === 'new' ? '' : editing.input} /></Field><Field label="기대 출력"><textarea name="output" rows={5} defaultValue={editing === 'new' ? '' : editing.output} /></Field><Feedback action={action} /><button className="button primary" disabled={action.busy}>저장하기</button></form></Modal>}
  </div>;
}
function Scores({ id }: { id: string }) {
  const resource = useResource<Student[]>(`/assignment/${id}/score`);
  const [selected, setSelected] = useState<Student>();
  const action = useAction();
  return <div className="description-body"><h2>학생 제출 내역</h2><ResourceState resource={resource} />{resource.data?.length === 0 && <Empty title="아직 제출된 풀이가 없습니다." />}{resource.data?.map(student => <div className="student-row" key={student.id}><div><strong>{student.name}</strong><p className="muted small">{student.number}</p></div><Badge>{student.score}점</Badge><button className="button secondary" onClick={() => { action.clear(); setSelected(student); }}>검토</button></div>)}{selected && <Modal title={`${selected.name}님의 제출`} busy={action.busy} close={() => setSelected(undefined)}><pre className="code-preview">{selected.code}</pre><form className="stack" onSubmit={e => { const data = values(e); void action.run(async () => { await api(`/assignment/score/edit/${selected.id}`, { method: 'PUT', body: { score: Number(data.score) } }); setSelected(undefined); resource.reload(); }); }}><Field label="점수"><input name="score" type="number" min={0} max={100} step={1} defaultValue={selected.score < 0 ? 0 : selected.score} required /></Field><Feedback action={action} /><button className="button primary" disabled={action.busy}>점수 저장</button></form></Modal>}</div>;
}
