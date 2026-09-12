import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'

type RequestType = { param: { taskId: string } }

export const useDeleteTask = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<void, Error, RequestType>({
    mutationFn: async ({ param }) => {
      await api.delete(`/api/v1/tasks/${param.taskId}`)
    },
    onSuccess: (_, { param }) => {
      toast.success('Task deleted')

      queryClient.invalidateQueries({ queryKey: ['project-analytics'] })
      queryClient.invalidateQueries({ queryKey: ['workspace-analytics'] })
      queryClient.invalidateQueries({ queryKey: ['tasks'] })
      queryClient.invalidateQueries({ queryKey: ['task', param.taskId] })
    },
    onError: () => {
      toast.error('Failed to delete task')
    },
  })

  return mutation
}
