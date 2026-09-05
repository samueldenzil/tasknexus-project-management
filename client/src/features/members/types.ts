import { Models } from 'node-appwrite'

export enum MemberRole {
  ADMIN = 'ADMIN',
  MEMBER = 'MEMBER',
}

export type Member = {
  userId: string
  workspaceId: string
  role: MemberRole
} & Models.Document
