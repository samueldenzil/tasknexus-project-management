import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'

type RequestType = { param: { workspaceId: string } }

export const useDeleteWorkspace = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<void, Error, RequestType>({
    mutationFn: async ({ param }) => {
      return await api.delete(`/api/v1/workspaces/${param.workspaceId}`)
    },
    onSuccess: (_, { param }) => {
      toast.success('Workspace deleted')
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
      queryClient.invalidateQueries({ queryKey: ['workspace', param.workspaceId] })
    },
    onError: () => {
      toast.error('Failed to delete workspace')
    },
  })

  return mutation
}
