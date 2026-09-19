import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'
import { Member, MemberRole } from '../types'
import { membersKeys } from '@/lib/query-keys'


type RequestType = {
  json: {
    role: MemberRole
  }
  param: {
    memberId: string
  }
}

type ResponseType = Member

export const useUpdateMember = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ param, json }) => {
      return await api.patch<Member>(`/api/v1/members/${param.memberId}`, json)
    },
    onSuccess: () => {
      toast.success('Member updated')
      queryClient.invalidateQueries({ queryKey: membersKeys.all })
    },
    onError: () => {
      toast.error('Failed to update member')
    },
  })

  return mutation
}
