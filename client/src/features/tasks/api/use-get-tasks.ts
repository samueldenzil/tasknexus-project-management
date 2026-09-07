import { useQuery } from '@tanstack/react-query'

import { Task, TaskStatus } from '@/features/tasks/types'
import { api } from '@/lib/api'

interface UseGetTasksProps {
  workspaceId: string
  projectId?: string | null
  status?: TaskStatus | null
  assigneeId?: string | null
  createdById?: string | null
  search?: string | null
  dueDate?: string | null
}

export const useGetTasks = ({
  workspaceId,
  projectId,
  status,
  assigneeId,
  createdById,
  search,
  dueDate,
}: UseGetTasksProps) => {
  const query = useQuery<Task[]>({
    queryKey: ['tasks', workspaceId, projectId, status, search, assigneeId, dueDate],
    queryFn: async () => {
      const params = new URLSearchParams({
        workspaceId,
      })

      if (projectId) params.append('projectId', projectId)
      if (status) params.append('status', status)
      if (assigneeId) params.append('assigneeId', assigneeId)
      if (createdById) params.append('createdById', createdById)
      // TODO: Check the backend for support
      if (search) params.append('search', search)
      if (dueDate) params.append('dueDate', dueDate)

      return await api.get<Task[]>(`/api/v1/tasks?${params.toString()}`)
    },
  })

  return query
}
