import { useState } from 'react';
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { ArrowLeft, ArrowRight, MessageSquare, Plus } from 'lucide-react';
import { useUser } from '../App';
import { api, formatDate } from '../api';
import type { Comment, PageResult, Post } from '../types';
import { ConfirmDelete, Empty, Feedback, Field, Modal, PageHeading, Pagination, ResourceState, Search, useAction, useResource, values } from '../ui';

const boardIds = (import.meta.env.VITE_BOARD_IDS || '').split(',').map((v: string) => v.trim()).filter((v: string) => /^[1-9]\d*$/.test(v));
function PostEditor({ boardId, item, close, saved }: { boardId: string; item?: Post; close: () => void; saved: (post: Post) => void }) {
  const action = useAction();
  return <Modal title={item ? '게시글 수정' : '새 이야기 작성'} close={close} busy={action.busy}><form className="stack" onSubmit={e => {
    const data = values(e);
    void action.run(async () => {
      if (!data.title.trim() || !data.content.trim()) throw new Error('제목과 내용을 입력해 주세요.');
      saved(await api<Post>(item ? `/board/${boardId}/edit/${item.id}` : `/board/${boardId}/write`, { method: item ? 'PUT' : 'POST', body: data }));
    });
  }}><Field label="제목"><input name="title" maxLength={200} required defaultValue={item?.title} placeholder="어떤 이야기를 나누고 싶나요?" /></Field><Field label="내용"><textarea name="content" rows={12} required defaultValue={item?.content} placeholder="질문이나 함께 나누고 싶은 내용을 작성해 주세요." /></Field><Feedback action={action} /><button className="button primary" disabled={action.busy}>{action.busy ? '저장 중…' : '게시글 저장'}</button></form></Modal>;
}
export function Community() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const requested = params.get('board');
  const boardId = requested && boardIds.includes(requested) ? requested : boardIds[0];
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [editor, setEditor] = useState(false);
  const resource = useResource<Post[] | PageResult>(boardId ? query ? `/board/${boardId}/search?keyword=${encodeURIComponent(query)}&page=${page}` : `/board/${boardId}` : null);
  const list = Array.isArray(resource.data) ? resource.data : resource.data?.postDtos;
  return <><PageHeading eyebrow="BETTER TOGETHER" title="커뮤니티" description="막히는 부분은 질문하고, 알게 된 것은 나누세요. 함께라서 더 멀리 갈 수 있어요.">{boardId && <button className="button primary" onClick={() => setEditor(true)}><Plus size={18} />글 작성</button>}</PageHeading>
    {!boardId ? <div className="panel"><Empty title="커뮤니티를 준비하고 있습니다.">게시판이 열리면 질문과 이야기를 나눌 수 있어요.</Empty></div> : <section className="panel list-panel"><div className="toolbar"><div className="inline-group"><MessageSquare size={22} /><select aria-label="게시판 선택" value={boardId} onChange={e => { setParams({ board: e.target.value }); setPage(1); }}>{boardIds.map((value: string) => <option key={value} value={value}>{boardIds.length === 1 ? '학습 게시판' : `게시판 ${value}`}</option>)}</select></div><Search onSearch={value => { setQuery(value); setPage(1); }} /></div><ResourceState resource={resource} />{list?.length === 0 && <Empty title={query ? '검색 결과가 없습니다.' : '첫 이야기를 기다리고 있어요.'}>{query ? '다른 검색어로 다시 찾아보세요.' : '질문이나 학습 경험을 자유롭게 나눠보세요.'}</Empty>}{list?.map(post => <Link className="post-row" to={`/community/${boardId}/${post.id}`} key={post.id}><span className="post-icon"><MessageSquare size={21} /></span><div className="row-grow"><h3>{post.title}</h3><p>{post.writer}<span>·</span>{formatDate(post.upload_time)}<span>·</span>조회 {post.views}</p></div><ArrowRight size={17} /></Link>)}<Pagination page={page} total={!Array.isArray(resource.data) ? resource.data?.totalPage || 0 : 0} setPage={setPage} /></section>}
    {editor && boardId && <PostEditor boardId={boardId} close={() => setEditor(false)} saved={post => navigate(`/community/${boardId}/${post.id}`)} />}
  </>;
}
export function PostDetail() {
  const { boardId, postId } = useParams();
  const user = useUser();
  const navigate = useNavigate();
  const resource = useResource<{ post: Post; comments: Comment[] }>(`/board/${boardId}/${postId}`);
  const action = useAction();
  const [editing, setEditing] = useState(false);
  const post = resource.data?.post;
  return <><Link to={`/community?board=${boardId}`} className="back-link"><ArrowLeft size={16} />게시판으로</Link><ResourceState resource={resource} />{post && <><article className="panel post-detail"><div className="post-heading"><p className="eyebrow">COMMUNITY</p><h1>{post.title}</h1><div className="section-heading"><p className="muted">{post.writer} · {formatDate(post.upload_time, true)} · 조회 {post.views}</p>{post.writer_id === user.id && <div className="inline-group"><button className="button secondary" onClick={() => setEditing(true)}>수정</button><ConfirmDelete label="게시글" onDelete={async () => { await api(`/board/${boardId}/delete/${postId}`, { method: 'DELETE' }); navigate(`/community?board=${boardId}`); }} /></div>}</div></div><div className="prose">{post.content}</div></article>
    <section className="panel comments-panel"><h2>댓글 <span className="count">{resource.data?.comments.length}</span></h2>{resource.data?.comments.map(comment => <CommentRow key={comment.id} comment={comment} boardId={boardId!} postId={postId!} reload={resource.reload} />)}<form className="stack" onSubmit={e => {
      const form = e.currentTarget; const data = values(e);
      void action.run(async () => {
        if (!data.content.trim()) throw new Error('댓글 내용을 입력해 주세요.');
        await api(`/board/${boardId}/${postId}/comment`, { method: 'POST', body: data }); form.reset(); resource.reload();
      }, '댓글을 등록했습니다.');
    }}><Field label="댓글 남기기" hint="최대 100자"><textarea name="content" required maxLength={100} rows={3} placeholder="도움이 되는 답변이나 의견을 남겨주세요." /></Field><Feedback action={action} /><div className="form-actions"><button className="button primary" disabled={action.busy}>댓글 등록</button></div></form></section>{editing && <PostEditor boardId={boardId!} item={post} close={() => setEditing(false)} saved={() => { setEditing(false); resource.reload(); }} />}</>}</>;
}
function CommentRow({ comment, boardId, postId, reload }: { comment: Comment; boardId: string; postId: string; reload: () => void }) {
  const user = useUser();
  const [editing, setEditing] = useState(false);
  const action = useAction();
  return <div className="comment-row"><div className="section-heading"><div><strong>{comment.writer}</strong><span className="muted small"> · {formatDate(comment.upload_time, true)}</span></div>{comment.writer_id === user.id && <div className="inline-group"><button className="text-button" onClick={() => setEditing(!editing)}>{editing ? '취소' : '수정'}</button><ConfirmDelete label="댓글" onDelete={async () => { await api(`/board/${boardId}/${postId}/delete/${comment.id}`, { method: 'DELETE' }); reload(); }} /></div>}</div>{editing ? <form className="stack" onSubmit={e => { const data = values(e); void action.run(async () => { if (!data.content.trim()) throw new Error('댓글 내용을 입력해 주세요.'); await api(`/board/${boardId}/${postId}/edit/${comment.id}`, { method: 'PUT', body: data }); setEditing(false); reload(); }); }}><Field label="댓글 수정" hint="최대 100자"><textarea name="content" required maxLength={100} defaultValue={comment.content} rows={3} /></Field><Feedback action={action} /><button className="button secondary" disabled={action.busy}>수정 저장</button></form> : <p className="prose">{comment.content}</p>}</div>;
}
