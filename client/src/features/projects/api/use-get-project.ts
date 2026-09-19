import { useQuery } from '@tanstack/react-query'

import { Project } from '@/features/projects/types'
import { api } from '@/lib/api'
import { projectsKeys } from '@/lib/query-keys'


interface UseGetProjectsProps {
  projectId: string
}

export const useGetProject = ({ projectId }: UseGetProjectsProps) => {
  const query = useQuery({
    queryKey: projectsKeys.detail(projectId),
    queryFn: async ({}) => {
      return await api.get<Project>(`/api/v1/projects/${projectId}`)
    },
  })

  return query
}
