import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Workspace } from '@/features/workspaces/types'
import { api } from '@/lib/api'

type RequestType = { param: { workspaceId: string } }

type ResponseType = Workspace

export const useResetInviteCode = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ param }) => {
      return await api.post(`/api/v1/workspaces/${param.workspaceId}/reset-invite-code`, {})
    },
    onSuccess: (data) => {
      toast.success('Invite code reseted')

      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
      queryClient.invalidateQueries({ queryKey: ['workspace', data.id] })
    },
    onError: () => {
      toast.error('Failed to reset invite code')
    },
  })

  return mutation
}
