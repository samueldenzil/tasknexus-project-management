import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { workspacesKeys } from '@/lib/query-keys'


export type WorkspaceAnalyticsResponseType = {
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

interface UseGetWorkspaceAnalyticsProps {
  workspaceId: string
}

export const useGetWorkspaceAnalytics = ({ workspaceId }: UseGetWorkspaceAnalyticsProps) => {
  const query = useQuery<WorkspaceAnalyticsResponseType>({
    queryKey: workspacesKeys.analytic(workspaceId),
    queryFn: async ({}) => {
      return await api.get<WorkspaceAnalyticsResponseType>(
        `/api/v1/workspaces/${workspaceId}/analytics`
      )
    },
  })

  return query
}
