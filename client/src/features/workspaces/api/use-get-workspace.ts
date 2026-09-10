import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { Workspace } from '../types'

interface UseGetWorkspaceProps {
  workspaceId: string
}

export const useGetWorkspace = ({ workspaceId }: UseGetWorkspaceProps) => {
  const query = useQuery({
    queryKey: ['workspace', workspaceId],
    queryFn: async () => {
      const data = await api.get<Workspace>(`/api/v1/workspaces/${workspaceId}`)
      return data
    },
  })

  return query
}
