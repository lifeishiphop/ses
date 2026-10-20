import { useState } from 'react';
import type { PageResult } from '../types';
import { Badge, Empty, PageHeading, Pagination, ResourceState, useResource } from '../ui';

export function Members() {
  const [page, setPage] = useState(1);
  const resource = useResource<PageResult>(`/manage?page=${page}`);
  return <><PageHeading eyebrow="ADMINISTRATION" title="회원 관리" description="서비스에 가입한 구성원을 확인하세요." /><section className="panel list-panel"><ResourceState resource={resource} />{resource.data?.memberDtos?.length === 0 && <Empty title="등록된 회원이 없습니다." />}<div className="table-wrap"><table><thead><tr><th>이름</th><th>학번 / 교번</th><th>이메일</th><th>유형</th></tr></thead><tbody>{resource.data?.memberDtos?.map(member => <tr key={member.number}><td>{member.name}</td><td>{member.number}</td><td>{member.email}</td><td><Badge>{member.identity === 1 ? '학생' : member.identity === 2 ? '강사' : '관리자'}</Badge></td></tr>)}</tbody></table></div><Pagination page={page} total={resource.data?.totalPage || 0} setPage={setPage} /></section></>;
}
