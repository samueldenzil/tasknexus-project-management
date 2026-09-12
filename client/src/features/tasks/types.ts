export enum TaskStatus {
  BACKLOG = 'BACKLOG',
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  IN_REVIEW = 'IN_REVIEW',
  DONE = 'DONE',
}

export type Task = {
  id: string
  name: string
  status: TaskStatus
  description: string | null
  dueDate: string | null
  position: number
  workspaceId: string
  project: {
    id: string
    name: string
    imageUrl: string | null
  }
  assignee: {
    id: string
    name: string
  } | null
  createdBy: {
    id: string
    name: string
  }
}
