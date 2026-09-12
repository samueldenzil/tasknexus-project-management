import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Workspace } from '@/features/workspaces/types'
import { api } from '@/lib/api'

type RequestType = {
  json: {
    inviteCode: string
  }
  param: {
    workspaceId: string
  }
}

type ResponseType = Workspace

export const useJoinWorkspace = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ json, param }) => {
      return await api.post(`/api/v1/workspaces/${param.workspaceId}/join`, json)
    },
    onSuccess: (data) => {
      toast.success('Joined workspace')
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
      queryClient.invalidateQueries({ queryKey: ['workspace', data.id] })
    },
    onError: () => {
      toast.error('Failed to join workspace')
    },
  })

  return mutation
}
