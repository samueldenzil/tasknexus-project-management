import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { projectsKeys } from '@/lib/query-keys'


export type ProjectAnalyticsResponseType = {
  taskCount: number
  taskDifference: number
  assignedTaskCount: number
  assignedTaskDifference: number
  completedTaskCount: number
  completedTaskDifference: number
  incompleteTaskCount: number
  incompleteTaskDifference: number
  overdueTaskCount: number
  overdueTaskDifference: number
}

interface UseGetProjectAnalyticsProps {
  projectId: string
}

export const useGetProjectAnalytics = ({ projectId }: UseGetProjectAnalyticsProps) => {
  const query = useQuery<ProjectAnalyticsResponseType>({
    queryKey: projectsKeys.analytic(projectId),
    queryFn: async ({}) => {
      return await api.get<ProjectAnalyticsResponseType>(`/api/v1/projects/${projectId}/analytics`)
    },
  })

  return query
}
