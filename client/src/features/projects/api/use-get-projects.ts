import { useQuery } from '@tanstack/react-query'

import { Project } from '@/features/projects/types'
import { api } from '@/lib/api'
import { projectsKeys } from '@/lib/query-keys'


interface UseGetProjectsProps {
  workspaceId: string
}

export const useGetProjects = ({ workspaceId }: UseGetProjectsProps) => {
  const query = useQuery<Project[]>({
    queryKey: projectsKeys.list(workspaceId),
    queryFn: async ({}) => api.get<Project[]>(`/api/v1/projects?workspaceId=${workspaceId}`),
  })

  return query
}
