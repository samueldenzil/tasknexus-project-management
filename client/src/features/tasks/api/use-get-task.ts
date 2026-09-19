import { useQuery } from '@tanstack/react-query'

import { Task } from '@/features/tasks/types'
import { api } from '@/lib/api'
import { tasksKeys } from '@/lib/query-keys'


interface UseGetTaskProps {
  taskId: string
}

export const useGetTask = ({ taskId }: UseGetTaskProps) => {
  const query = useQuery<Task>({
    queryKey: tasksKeys.detail(taskId),
    queryFn: async () => {
      return await api.get<Task>(`/api/v1/tasks/${taskId}`)
    },
  })

  return query
}
