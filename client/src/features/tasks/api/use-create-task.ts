import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Task, TaskStatus } from '@/features/tasks/types'
import { api } from '@/lib/api'
import { tasksKeys } from '@/lib/query-keys'


type RequestType = {
  json: {
    status: TaskStatus
    name: string
    workspaceId: string
    projectId: string
    assigneeId: string
    dueDate: Date | string
    description?: string
  }
}
type ResponseType = Task

export const useCreateTask = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ json }) => {
      return await api.post<ResponseType>('/api/v1/tasks', json)
    },
    onSuccess: () => {
      toast.success('Task created')
      queryClient.invalidateQueries({ queryKey: tasksKeys.lists() })
    },
    onError: () => {
      toast.error('Failed to create task')
    },
  })

  return mutation
}
