import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'
import { TaskStatus } from '../types'
import { workspacesKeys, projectsKeys, tasksKeys } from '@/lib/query-keys'


type RequestType = {
  workspaceId: string
  tasks: {
    taskId: string
    status: TaskStatus
    position: number
  }[]
}

export const useBulkUpdateTasks = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<void, Error, { json: RequestType }>({
    mutationFn: async ({ json }) => {
      await api.post('/api/v1/tasks/bulk-update', json)
    },
    onSuccess: () => {
      toast.success('Tasks updated')

      queryClient.invalidateQueries({ queryKey: projectsKeys.analytics() })
      queryClient.invalidateQueries({ queryKey: workspacesKeys.analytics() })
      queryClient.invalidateQueries({ queryKey: tasksKeys.lists() })
    },
    onError: () => {
      toast.error('Failed to update tasks')
    },
  })

  return mutation
}
