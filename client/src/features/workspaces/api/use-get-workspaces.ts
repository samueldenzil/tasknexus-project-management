import { useQuery } from '@tanstack/react-query'

import { Workspace } from '@/features/workspaces/types'
import { api } from '@/lib/api'
import { workspacesKeys } from '@/lib/query-keys'


export const useGetWorkspaces = () => {
  const query = useQuery<Workspace[]>({
    queryKey: workspacesKeys.list(),
    queryFn: async () => api.get<Workspace[]>('/api/v1/workspaces'),
  })

  return query
}
