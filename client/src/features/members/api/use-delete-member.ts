import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'
import { membersKeys } from '@/lib/query-keys'


type RequestType = { param: { memberId: string } }

export const useDeleteMember = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<void, Error, RequestType>({
    mutationFn: async ({ param }) => {
      return await api.delete(`/api/v1/members/${param.memberId}`)
    },
    onSuccess: () => {
      toast.success('Member deleted')
      queryClient.invalidateQueries({ queryKey: membersKeys.all })
    },
    onError: () => {
      toast.error('Failed to delete member')
    },
  })

  return mutation
}
