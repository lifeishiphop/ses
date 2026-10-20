import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BookOpen, Check, Code2, Flag, MoveUpRight } from 'lucide-react';
import { useUser } from '../App';
import { api, deadline, formatDate, languageName } from '../api';
import type { Assignment, Lesson, Problem } from '../types';
import { Badge, Empty, Notice, PageHeading, ResourceState, useResource } from '../ui';

export function Dashboard() {
  const user = useUser();
  const lessons = useResource<Lesson[]>(user.identity === 0 ? '/lesson' : `/lesson/${user.identity === 2 ? 't' : 's'}`);
  const problems = useResource<Problem[]>('/problem');
  const [assignments, setAssignments] = useState<(Assignment & { lessonName: string })[] | null>(null);
  const [assignmentError, setAssignmentError] = useState('');
  useEffect(() => {
    const controller = new AbortController();
    setAssignments(null); setAssignmentError('');
    if (lessons.data) Promise.all(lessons.data.map(async lesson => {
      const list = await api<Assignment[]>(`/lesson/${lesson.id}/assignment`, { signal: controller.signal });
      return list.map(item => ({ ...item, lessonName: lesson.name }));
    })).then(data => setAssignments(data.flat().sort((a, b) => a.end_time.localeCompare(b.end_time))))
      .catch(e => { if (!controller.signal.aborted) setAssignmentError(e.message); });
    return () => controller.abort();
  }, [lessons.data]);
  const pending = assignments?.filter(item => deadline(item.end_time) !== '마감' && item.solve !== '제출완료');
  const solved = problems.data?.filter(item => item.solve === 'o').length;
  const recommended = problems.data?.filter(item => item.solve !== 'o').slice(0, 3);
  return <>
    <PageHeading eyebrow="MY LEARNING SPACE" title={`${user.name}님, 오늘도 한 걸음 더.`} description="작은 도전들이 모여 더 나은 코드를 만듭니다."><span className="date-label">{new Intl.DateTimeFormat('ko-KR', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())}</span></PageHeading>
    <section className="welcome-banner"><div><span className="banner-label">BUILD YOUR NEXT POSSIBILITY</span><h2>배운 것을 코드로,<br />오늘의 도전을 시작해 볼까요?</h2><p>한 문제씩 풀어가며 나만의 실력을 쌓아보세요.</p><Link className="button light" to="/problems">문제 풀러 가기 <ArrowRight size={17} /></Link></div><div className="banner-art" aria-hidden="true"><div className="orbit orbit-one" /><div className="orbit orbit-two" /><div className="code-tile">{'{ }'}<span>one step forward</span></div><span className="floating-symbol symbol-one">+</span><span className="floating-symbol symbol-two">↗</span><span className="small-code-tile">&lt;/&gt;</span></div></section>
    <div className="stat-grid">
      {[{ label: user.identity === 2 ? '운영 중인 강의' : '나의 강의실', count: lessons.data?.length, Icon: BookOpen, detail: '함께 배우는 공간', tone: 'green' }, { label: '진행 중인 과제', count: pending?.length, Icon: Flag, detail: '마감 전에 확인하세요', tone: 'orange' }, { label: '해결한 문제', count: solved, Icon: Check, detail: '차곡차곡 쌓이는 실력', tone: 'blue' }].map(({ label, count, Icon, detail, tone }) => <div className="stat-card" key={label}><div className={`stat-icon ${tone}`}><Icon size={21} /></div><div><span className="muted">{label}</span><strong>{count ?? '—'}<small>개</small></strong></div><p>{detail}</p></div>)}
    </div>
    <div className="dashboard-grid"><section className="panel assignment-panel"><div className="section-heading"><h2>다가오는 과제 <span className="count">{pending?.length ?? '—'}</span></h2><Link className="text-link" to="/lessons">강의실 보기 <ArrowRight size={15} /></Link></div>
      <ResourceState resource={lessons} />{assignmentError && <Notice error>{assignmentError}</Notice>}
      {!lessons.loading && !lessons.error && !assignmentError && assignments === null && <p className="muted">과제를 확인하고 있어요.</p>}
      {pending?.length === 0 && <Empty title="지금은 마감 예정인 과제가 없어요." to="/lessons" label="강의실 둘러보기">새로운 수업에 참여하거나 문제 풀이를 이어가세요.</Empty>}
      {pending?.slice(0, 4).map(item => <Link className="assignment-row" to={`/assignments/${item.id}`} key={item.id}><div className="assignment-date"><span>{new Date(item.end_time).getMonth() + 1}월</span><strong>{new Date(item.end_time).getDate()}</strong></div><div className="row-grow"><small>{item.lessonName}</small><h3>{item.title}</h3><span className="muted small">{formatDate(item.end_time, true)}까지</span></div><Badge tone={deadline(item.end_time) === '오늘 마감' ? 'orange' : 'green'}>{deadline(item.end_time)}</Badge><ArrowRight size={18} /></Link>)}
    </section><section className="learning-tip"><span className="eyebrow">A LITTLE EVERY DAY</span><Code2 size={31} /><h2>정답보다 중요한 건<br />생각하는 과정.</h2><p>실행 결과를 확인하고,<br />다른 방법으로 다시 도전해 보세요.<br />시도할 때마다 한 걸음 성장합니다.</p><Link className="text-link" to="/problems">새로운 문제 만나기 <MoveUpRight size={17} /></Link><span className="tip-decoration" aria-hidden="true">↗</span></section></div>
    <section className="section-block"><div className="section-heading"><h2>나의 강의실</h2><Link className="text-link" to="/lessons">전체 보기 <ArrowRight size={15} /></Link></div>
      {lessons.data?.length === 0 && <div className="panel"><Empty title="아직 참여한 강의실이 없어요." to="/lessons" label="강의실 찾기">강의실을 찾아 수강을 신청해 보세요.</Empty></div>}
      <div className="lesson-grid">{lessons.data?.slice(0, 3).map((lesson, index) => <Link className={`lesson-card tone-${index % 3}`} key={lesson.id} to={`/lessons/${lesson.id}`}><div className="lesson-card-top"><span className="course-icon"><BookOpen size={22} /></span><Badge>{lesson.year} · {lesson.semester}학기</Badge></div><h3>{lesson.name}</h3><p>{lesson.owner} 강사</p><div className="lesson-card-bottom"><span>강의실 들어가기</span><ArrowRight size={17} /></div></Link>)}</div>
    </section>
    <section className="panel"><div className="section-heading"><h2>다음 도전, 이 문제 어때요?</h2><Link className="text-link" to="/problems">문제 전체 보기 <ArrowRight size={15} /></Link></div><ResourceState resource={problems} />
      {recommended?.length === 0 && <Empty title={problems.data?.length ? '모든 문제를 해결했어요!' : '새로운 문제를 기다리고 있어요.'}>문제가 등록되면 이곳에서 바로 도전할 수 있어요.</Empty>}
      {recommended?.map((problem, index) => <Link className="recommend-row" to={`/problems/${problem.id}`} key={problem.id}><span className="problem-number">{String(index + 1).padStart(2, '0')}</span><h3>{problem.title}</h3><Badge>{languageName(problem.language)}</Badge><ArrowRight size={18} /></Link>)}
    </section>
  </>;
}
