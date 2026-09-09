export enum MemberRole {
  ADMIN = 'ADMIN',
  MEMBER = 'MEMBER',
}

export type Member = {
  id: string
  userId: string
  name: string
  email: string
  role: MemberRole
}
