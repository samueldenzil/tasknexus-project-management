import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { Member } from '../types'
import { membersKeys } from '@/lib/query-keys'


interface UseGetMembersProps {
  workspaceId: string
}

export const useGetMembers = ({ workspaceId }: UseGetMembersProps) => {
  const query = useQuery<Member[]>({
    queryKey: membersKeys.list(workspaceId),
    queryFn: async () => {
      return await api.get<Member[]>(`/api/v1/members?workspaceId=${workspaceId}`)
    },
  })

  return query
}
