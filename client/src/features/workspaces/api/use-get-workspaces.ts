import { useQuery } from '@tanstack/react-query'

import { Workspace } from '@/features/workspaces/types'
import { api } from '@/lib/api'

export const useGetWorkspaces = () => {
  const query = useQuery<Workspace[]>({
    queryKey: ['workspaces'],
    queryFn: async () => api.get<Workspace[]>('/api/v1/workspaces'),
  })

  return query
}
