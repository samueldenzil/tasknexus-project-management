import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Task, TaskStatus } from '@/features/tasks/types'
import { api } from '@/lib/api'
import { workspacesKeys, projectsKeys, tasksKeys } from '@/lib/query-keys'


type RequestType = {
  json: {
    status?: TaskStatus
    name?: string
    workspaceId?: string
    projectId?: string
    assigneeId?: string
    dueDate?: Date | string
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

      queryClient.invalidateQueries({ queryKey: projectsKeys.analytics() })
      queryClient.invalidateQueries({ queryKey: workspacesKeys.analytics() })
      queryClient.invalidateQueries({ queryKey: tasksKeys.lists() })
      queryClient.invalidateQueries({ queryKey: tasksKeys.detail(data.id) })
    },
    onError: () => {
      toast.error('Failed to update task')
    },
  })

  return mutation
}
