import { useQuery } from '@tanstack/react-query'

import { Project } from '@/features/projects/types'
import { api } from '@/lib/api'

interface UseGetProjectsProps {
  workspaceId: string
}

export const useGetProjects = ({ workspaceId }: UseGetProjectsProps) => {
  const query = useQuery<Project[]>({
    queryKey: ['projects', workspaceId],
    queryFn: async ({}) => api.get<Project[]>(`/api/v1/projects?workspaceId=${workspaceId}`),
  })

  return query
}
