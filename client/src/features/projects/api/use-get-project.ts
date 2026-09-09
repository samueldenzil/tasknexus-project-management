import { useQuery } from '@tanstack/react-query'

import { Project } from '@/features/projects/types'
import { api } from '@/lib/api'

interface UseGetProjectsProps {
  projectId: string
}

export const useGetProject = ({ projectId }: UseGetProjectsProps) => {
  const query = useQuery({
    queryKey: ['project', projectId],
    queryFn: async ({}) => {
      return await api.get<Project>(`/api/v1/projects/${projectId}`)
    },
  })

  return query
}
