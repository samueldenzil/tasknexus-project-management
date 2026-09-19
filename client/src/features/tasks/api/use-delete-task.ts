import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'
import { workspacesKeys, projectsKeys, tasksKeys } from '@/lib/query-keys'


type RequestType = { param: { taskId: string } }

export const useDeleteTask = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<void, Error, RequestType>({
    mutationFn: async ({ param }) => {
      await api.delete(`/api/v1/tasks/${param.taskId}`)
    },
    onSuccess: (_, { param }) => {
      toast.success('Task deleted')

      queryClient.invalidateQueries({ queryKey: projectsKeys.analytics() })
      queryClient.invalidateQueries({ queryKey: workspacesKeys.analytics() })
      queryClient.invalidateQueries({ queryKey: tasksKeys.lists() })
      queryClient.invalidateQueries({ queryKey: tasksKeys.detail(param.taskId) })
    },
    onError: () => {
      toast.error('Failed to delete task')
    },
  })

  return mutation
}
