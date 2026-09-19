import { TaskStatus } from '@/features/tasks/types'

export interface TaskFilters {
  workspaceId: string
  projectId?: string | null
  status?: TaskStatus | null
  assigneeId?: string | null
  createdById?: string | null
  search?: string | null
  dueDate?: string | null
}

export const authKeys = {
  all: ['auth'] as const,
  currentUser: () => [...authKeys.all, 'current-user'] as const,
} as const

export const workspacesKeys = {
  all: ['workspaces'] as const,
  lists: () => [...workspacesKeys.all, 'list'] as const,
  list: () => [...workspacesKeys.lists()] as const,
  details: () => [...workspacesKeys.all, 'detail'] as const,
  detail: (id: string) => [...workspacesKeys.details(), id] as const,
  analytics: () => [...workspacesKeys.all, 'analytics'] as const,
  analytic: (id: string) => [...workspacesKeys.analytics(), id] as const,
} as const

export const projectsKeys = {
  all: ['projects'] as const,
  lists: () => [...projectsKeys.all, 'list'] as const,
  list: (workspaceId: string) => [...projectsKeys.lists(), { workspaceId }] as const,
  details: () => [...projectsKeys.all, 'detail'] as const,
  detail: (id: string) => [...projectsKeys.details(), id] as const,
  analytics: () => [...projectsKeys.all, 'analytics'] as const,
  analytic: (id: string) => [...projectsKeys.analytics(), id] as const,
} as const

export const membersKeys = {
  all: ['members'] as const,
  lists: () => [...membersKeys.all, 'list'] as const,
  list: (workspaceId: string) => [...membersKeys.lists(), { workspaceId }] as const,
} as const

export const tasksKeys = {
  all: ['tasks'] as const,
  lists: () => [...tasksKeys.all, 'list'] as const,
  list: (filters: TaskFilters) => [...tasksKeys.lists(), filters] as const,
  details: () => [...tasksKeys.all, 'detail'] as const,
  detail: (id: string) => [...tasksKeys.details(), id] as const,
} as const
