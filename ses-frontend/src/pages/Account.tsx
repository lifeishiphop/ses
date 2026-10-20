import { useState } from 'react';
import { ArrowRight, BookOpen, Code2, MessageSquare } from 'lucide-react';
import { api, passwordError, saveSession } from '../api';
import type { Session } from '../types';
import { Feedback, Field, PageHeading, useAction, values } from '../ui';
import { useUser } from '../App';

export function Auth() {
  const [signup, setSignup] = useState(false);
  const action = useAction();
  return <div className="auth-page">
    <section className="auth-story">
      <a className="brand" href="/"><span className="brand-symbol"><Code2 size={26} /></span><span>S.E.S<span className="brand-caption">SOFTWARE ENGINEERING SUPPORTER</span></span></a>
      <div className="auth-copy"><span className="auth-kicker">YOUR NEXT LINE STARTS HERE</span><h1>배움이 코드가 되고,<br />코드가 가능성이 되는 곳.</h1><p>문제 풀이부터 수업과 과제까지.<br />당신의 소프트웨어 여정을 함께합니다.</p>
        <div className="code-illustration" aria-hidden="true"><div className="code-window-bar"><i /><i /><i /><span>your-next-step.java</span></div><pre><span className="code-comment">// 작은 도전이 만드는 변화</span>{'\n'}<span className="code-purple">while</span> (curiosity) {'{\n'}  learn();{'\n'}  code();{'\n'}  <span className="code-green">grow</span>();{'\n}'}</pre><div className="code-window-bottom"><span className="green-dot" />Ready for your next step</div></div>
      </div>
      <div className="auth-features"><span><Code2 size={17} />코딩 문제</span><span><BookOpen size={17} />강의와 과제</span><span><MessageSquare size={17} />함께하는 학습</span></div>
    </section>
    <section className="auth-form-panel"><div className="auth-form-inner"><p className="eyebrow">WELCOME TO S.E.S</p><h2>{signup ? '새로운 배움의 시작' : '다시 만나 반가워요.'}</h2><p className="muted">{signup ? '계정을 만들고 학습 공간에 참여하세요.' : '로그인하고 오늘의 배움을 이어가세요.'}</p>
      <form className="stack" onSubmit={e => {
        const data = values(e);
        void action.run(async () => {
          if (signup) {
            const error = passwordError(data.password, data.passwordCheck);
            if (error) throw new Error(error);
            await api('/signup', { method: 'POST', body: { ...data, identity: Number(data.identity) } });
            setSignup(false);
          } else saveSession(await api<Session>('/login', { method: 'POST', body: { id: data.number, password: data.password } }));
        }, signup ? '가입이 완료되었습니다. 학번과 비밀번호로 로그인해 주세요.' : '');
      }}>
        {signup && <Field label="이름"><input name="name" autoComplete="name" required maxLength={50} /></Field>}
        <Field label="학번 / 교번" hint={signup ? '최대 8자' : undefined}><input name="number" autoComplete="username" placeholder="학번 또는 교번을 입력하세요" required maxLength={8} /></Field>
        {signup && <><Field label="학교 이메일" hint="현재 계정은 이메일 전체 20자까지 지원합니다."><input name="email" type="email" autoComplete="email" placeholder="name@kumoh.ac.kr" pattern=".+@kumoh\.ac\.kr" maxLength={20} required /></Field><Field label="가입 유형"><select name="identity"><option value="1">학생</option><option value="2">강사</option></select></Field></>}
        <Field label="비밀번호" hint={signup ? '영문·숫자·특수문자(!@#$%^&+=) 포함 8~30자' : undefined}><input name="password" type="password" autoComplete={signup ? 'new-password' : 'current-password'} placeholder="비밀번호를 입력하세요" required maxLength={30} /></Field>
        {signup && <Field label="비밀번호 확인"><input name="passwordCheck" type="password" autoComplete="new-password" required maxLength={30} /></Field>}
        <Feedback action={action} />
        <button className="button primary auth-submit" disabled={action.busy}>{action.busy ? '처리 중…' : signup ? '회원가입' : '로그인'}<ArrowRight size={18} /></button>
      </form><div className="auth-switch">{signup ? '이미 계정이 있나요?' : 'S.E.S가 처음인가요?'} <button className="text-button" disabled={action.busy} onClick={() => { setSignup(!signup); action.clear(); }}>{signup ? '로그인' : '회원가입'}</button></div><p className="auth-footnote">금오공과대학교 구성원을 위한 학습 공간</p>
    </div></section>
  </div>;
}
export function Account() {
  const user = useUser();
  const action = useAction();
  return <><PageHeading eyebrow="ACCOUNT" title="계정 설정" description="내 정보를 확인하고 비밀번호를 관리하세요." /><div className="account-grid"><section className="panel"><h2>내 정보</h2><dl className="info-list"><dt>이름</dt><dd>{user.name}</dd><dt>학번 / 교번</dt><dd>{user.number}</dd><dt>이메일</dt><dd>{user.email}</dd><dt>계정 유형</dt><dd>{user.identity === 1 ? '학생' : user.identity === 2 ? '강사' : '관리자'}</dd></dl></section><section className="panel"><h2>비밀번호 변경</h2><form className="stack" onSubmit={e => {
    const form = e.currentTarget; const data = values(e);
    void action.run(async () => {
      const error = passwordError(data.newPassword, data.newPasswordCheck);
      if (error) throw new Error(error);
      await api('/changePassword', { method: 'PUT', body: data }); form.reset();
    }, '비밀번호를 변경했습니다.');
  }}><Field label="현재 비밀번호"><input type="password" name="password" autoComplete="current-password" required /></Field><Field label="새 비밀번호" hint="영문·숫자·특수문자(!@#$%^&+=) 포함 8~30자"><input type="password" name="newPassword" autoComplete="new-password" required /></Field><Field label="새 비밀번호 확인"><input type="password" name="newPasswordCheck" autoComplete="new-password" required /></Field><Feedback action={action} /><button className="button primary" disabled={action.busy}>{action.busy ? '변경 중…' : '비밀번호 변경'}</button></form></section></div></>;
}
