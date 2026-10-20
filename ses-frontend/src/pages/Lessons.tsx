import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, ArrowRight, BookOpen, Plus, Users } from 'lucide-react';
import { useUser } from '../App';
import { api, deadline, formatDate, languageName } from '../api';
import type { Assignment, Lesson, PageResult, Registration, Student } from '../types';
import { Badge, ConfirmDelete, Empty, Feedback, Field, Modal, PageHeading, Pagination, ResourceState, Search, useAction, useResource, values } from '../ui';
import { TaskEditor } from './Problems';

function LessonEditor({ item, close, saved }: { item?: Lesson; close: () => void; saved: (lesson: Lesson) => void }) {
  const action = useAction();
  return <Modal title={item ? '강의실 수정' : '새 강의실 만들기'} close={close} busy={action.busy}><form className="stack" onSubmit={e => {
    const data = values(e);
    void action.run(async () => {
      if (!data.name.trim()) throw new Error('강의실 이름을 입력해 주세요.');
      saved(await api<Lesson>(item ? `/lesson/edit/${item.id}` : '/lesson/add', { method: item ? 'PUT' : 'POST', body: data }));
    });
  }}><Field label="강의실 이름"><input name="name" required maxLength={100} defaultValue={item?.name} placeholder="예: 자료구조" /></Field><div className="form-grid"><Field label="개설 연도"><input name="year" type="number" min={2000} max={2100} required defaultValue={item?.year || new Date().getFullYear()} /></Field><Field label="학기"><select name="semester" defaultValue={item?.semester || '1'}><option value="1">1학기</option><option value="2">2학기</option></select></Field></div><Feedback action={action} /><button className="button primary" disabled={action.busy}>{action.busy ? '저장 중…' : '저장하기'}</button></form></Modal>;
}
export function Lessons() {
  const user = useUser();
  const navigate = useNavigate();
  const [tab, setTab] = useState('mine');
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [editor, setEditor] = useState(false);
  const resource = useResource<Lesson[] | PageResult>(tab === 'mine' && user.identity !== 0 ? `/lesson/${user.identity === 2 ? 't' : 's'}` : query ? `/lesson/search?keyword=${encodeURIComponent(query)}&page=${page}` : '/lesson');
  const registrations = useResource<Registration[]>(user.identity === 1 ? '/myRegister' : null);
  const list = Array.isArray(resource.data) ? resource.data : resource.data?.lessonDtos;
  return <><PageHeading eyebrow="LEARN TOGETHER" title="강의실" description="수업에서 배운 내용을 실습하고, 과제를 통해 한 단계 성장하세요.">{user.identity === 2 && <button className="button primary" onClick={() => setEditor(true)}><Plus size={18} />강의실 만들기</button>}</PageHeading><div className="toolbar standalone"><div className="tabs">{[['mine', user.identity === 2 ? '내가 운영하는 강의실' : '나의 강의실'], ['all', '전체 강의실']].map(([value, label]) => <button key={value} className={tab === value ? 'active' : ''} aria-pressed={tab === value} onClick={() => { setTab(value); setPage(1); }}>{label}</button>)}</div><Search placeholder="강의실 이름으로 검색" onSearch={value => { setQuery(value); setPage(1); setTab('all'); }} /></div><ResourceState resource={resource} />
    {user.identity === 1 && <ResourceState resource={registrations} />}
    {tab === 'mine' && registrations.data?.some(r => r.accept === 0) && <div className="notice"><Users size={19} /><span>{registrations.data.filter(r => r.accept === 0).length}개의 수강 신청이 승인을 기다리고 있습니다. 전체 강의실에서 신청 상태를 확인할 수 있어요.</span></div>}
    {list?.length === 0 && <div className="panel"><Empty title={tab === 'mine' ? '아직 나의 강의실이 없어요.' : '강의실을 찾을 수 없습니다.'}>{tab === 'mine' ? '전체 강의실에서 수업을 찾아 수강 신청을 해보세요.' : '다른 검색어로 다시 찾아보세요.'}</Empty></div>}
    <div className="lesson-grid">{list?.map((lesson, i) => { const registration = registrations.data?.find(r => r.lesson_id === lesson.id); return <Link to={`/lessons/${lesson.id}`} key={lesson.id} className={`lesson-card tone-${i % 3}`}><div className="lesson-card-top"><span className="course-icon"><BookOpen size={23} /></span><Badge>{lesson.year} · {lesson.semester}학기</Badge></div><h3>{lesson.name}</h3><p>{lesson.owner} 강사</p><div className="lesson-card-bottom"><span>{registration ? registration.accept === 1 ? '수강 중' : '승인 대기' : '강의실 둘러보기'}</span><ArrowRight size={18} /></div></Link>; })}</div>
    <Pagination page={page} total={!Array.isArray(resource.data) ? resource.data?.totalPage || 0 : 0} setPage={setPage} />
    {editor && <LessonEditor close={() => setEditor(false)} saved={lesson => navigate(`/lessons/${lesson.id}`)} />}
  </>;
}
export function LessonDetail() {
  const { id } = useParams();
  return <LessonContent key={id} id={id!} />;
}
function LessonContent({ id }: { id: string }) {
  const user = useUser();
  const navigate = useNavigate();
  const resource = useResource<Lesson>(`/lesson/${id}`);
  const assignments = useResource<Assignment[]>(`/lesson/${id}/assignment`);
  const registrations = useResource<Registration[]>(user.identity === 1 ? '/myRegister' : null);
  const [editing, setEditing] = useState(false);
  const [taskEditor, setTaskEditor] = useState(false);
  const [tab, setTab] = useState('assignments');
  const action = useAction();
  const lesson = resource.data;
  const owner = lesson?.owner_id === user.id;
  const registration = registrations.data?.find(r => r.lesson_id === Number(id));
  return <><Link to="/lessons" className="back-link"><ArrowLeft size={16} />강의실 목록으로</Link><ResourceState resource={resource} />{lesson && <>
    <PageHeading eyebrow={`${lesson.year} · ${lesson.semester}학기`} title={lesson.name} description={`${lesson.owner} 강사와 함께하는 학습 공간`}>{owner ? <><button className="button secondary" onClick={() => setEditing(true)}>강의실 수정</button><ConfirmDelete label="강의실" onDelete={async () => { await api(`/lesson/delete/${id}`, { method: 'DELETE' }); navigate('/lessons'); }} /></> : user.identity === 1 ? <button className="button primary" disabled={action.busy || registrations.loading || !!registrations.error || !!registration} onClick={() => action.run(async () => { await api(`/lesson/${id}/register`, { method: 'POST', body: {} }); registrations.reload(); }, '수강 신청을 보냈습니다. 강사의 승인을 기다려 주세요.')}>{registration ? registration.accept === 1 ? '수강 중' : '승인 대기' : '수강 신청하기'}</button> : null}</PageHeading><Feedback action={action} />{registrations.error && <ResourceState resource={registrations} />}
    <section className="panel list-panel"><div className="toolbar"><div className="tabs"><button className={tab === 'assignments' ? 'active' : ''} onClick={() => setTab('assignments')}>수업 과제</button>{owner && <button className={tab === 'students' ? 'active' : ''} onClick={() => setTab('students')}>수강생 관리</button>}</div>{owner && tab === 'assignments' && <button className="button primary" onClick={() => setTaskEditor(true)}><Plus size={16} />과제 만들기</button>}</div>
      {tab === 'assignments' ? <><ResourceState resource={assignments} />{assignments.data?.length === 0 && <Empty title="아직 등록된 과제가 없습니다.">수업 과제가 등록되면 마감일과 함께 확인할 수 있어요.</Empty>}{assignments.data?.map(item => <Link to={`/assignments/${item.id}`} className="assignment-row" key={item.id}><div className="assignment-date"><span>{new Date(item.end_time).getMonth() + 1}월</span><strong>{new Date(item.end_time).getDate()}</strong></div><div className="row-grow"><h3>{item.title}</h3><p className="muted small">{formatDate(item.end_time, true)}까지 · {languageName(item.language)}</p></div><Badge tone={item.solve === '제출완료' ? 'green' : deadline(item.end_time) === '마감' ? '' : 'orange'}>{owner ? `${item.submitStudents ?? 0}/${item.totalStudents ?? 0}명 제출` : item.solve === '제출완료' ? '제출 완료' : deadline(item.end_time)}</Badge><ArrowRight size={18} /></Link>)}</> : owner && <Students id={id} />}
    </section>
    {editing && <LessonEditor item={lesson} close={() => setEditing(false)} saved={() => { setEditing(false); resource.reload(); }} />}
    {taskEditor && <TaskEditor kind="assignment" lessonId={Number(id)} close={() => setTaskEditor(false)} saved={item => navigate(`/assignments/${item.id}`)} />}
  </>}</>;
}
function Students({ id }: { id: string }) {
  const pending = useResource<Student[]>(`/lesson/${id}/register`);
  const accepted = useResource<Student[]>(`/lesson/${id}/student`);
  const action = useAction();
  const reload = () => { pending.reload(); accepted.reload(); };
  return <div className="description-body"><Feedback action={action} /><h2>승인 대기 <span className="count">{pending.data?.length ?? '—'}</span></h2><ResourceState resource={pending} />{pending.data?.length === 0 && <p className="muted">대기 중인 수강 신청이 없습니다.</p>}{pending.data?.map(student => <div className="student-row" key={student.id}><div className="row-grow"><strong>{student.name}</strong><p className="muted small">{student.number}</p></div><button className="button primary" disabled={action.busy} onClick={() => action.run(async () => { await api(`/lesson/accept/${student.id}`, { method: 'PUT' }); reload(); }, '수강 신청을 승인했습니다.')}>승인</button><button className="button secondary" disabled={action.busy} onClick={() => action.run(async () => { await api(`/lesson/refuse/${student.id}`, { method: 'DELETE' }); reload(); }, '수강 신청을 거절했습니다.')}>거절</button></div>)}
    <h2 className="section-block">수강 중 <span className="count">{accepted.data?.length ?? '—'}</span></h2><ResourceState resource={accepted} />{accepted.data?.length === 0 && <p className="muted">아직 수강 중인 학생이 없습니다.</p>}{accepted.data?.map(student => <div className="student-row" key={student.id}><div className="row-grow"><strong>{student.name}</strong><p className="muted small">{student.number}</p></div><Badge tone="green">수강 중</Badge><ConfirmDelete label={`${student.name}님의 수강 정보`} onDelete={async () => { await api(`/lesson/${id}/student/delete/${student.id}`, { method: 'DELETE' }); reload(); }} /></div>)}
  </div>;
}
