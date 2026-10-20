export interface Member {
  id: number;
  number: string;
  email: string;
  name: string;
  identity: 0 | 1 | 2;
}
export interface Session { token: string; memberDto: Member }
export interface Problem {
  id: number;
  title: string;
  description: string;
  language: number;
  uploader_id: number;
  uploader: string;
  upload_time: string;
  solve?: string;
}
export interface Lesson {
  id: number;
  name: string;
  year: string;
  semester: string;
  owner: string;
  owner_id: number;
}
export interface Assignment {
  id: number;
  title: string;
  description: string;
  language: number;
  lessonId: number;
  start_time: string;
  end_time: string;
  solve?: string;
  submit_contents?: string;
  submit_language?: number;
  score?: number;
  submitStudents?: number;
  totalStudents?: number;
}
export interface Submission {
  id?: number;
  language: number;
  submit_contents: string;
  score?: number;
  result?: string;
  submit_time?: string;
}
export interface Testcase { id: number; input: string; output: string }
export interface Registration { id: number; member_id: number; lesson_id: number; accept: number }
export interface Student { id: number; number: string; name: string; code?: string; score: number }
export interface Post {
  id: number; title: string; content: string; writer: string; writer_id: number;
  upload_time: string; views: number;
}
export interface Comment {
  id: number; content: string; writer: string; writer_id: number; upload_time: string;
}
export interface PageResult {
  problemDtos?: Problem[];
  lessonDtos?: Lesson[];
  postDtos?: Post[];
  memberDtos?: Member[];
  totalPage: number;
}
