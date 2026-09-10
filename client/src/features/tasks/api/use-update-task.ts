import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Task, TaskStatus } from '@/features/tasks/types'
import { api } from '@/lib/api'

type RequestType = {
  json: {
    status?: TaskStatus
    name?: string
    workspaceId?: string
    projectId?: string
    assigneeId?: string
    dueDate?: Date
    description?: string
  }
  param: {
    taskId: string
  }
}
type ResponseType = Task

export const useUpdateTask = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ json, param }) => {
      return await api.patch<ResponseType>(`/api/v1/tasks/${param.taskId}`, json)
    },
    onSuccess: (data) => {
      toast.success('Task updated')

      queryClient.invalidateQueries({ queryKey: ['project-analytics'] })
      queryClient.invalidateQueries({ queryKey: ['workspace-analytics'] })
      queryClient.invalidateQueries({ queryKey: ['tasks'] })
      queryClient.invalidateQueries({ queryKey: ['task', data.id] })
    },
    onError: () => {
      toast.error('Failed to update task')
    },
  })

  return mutation
}
