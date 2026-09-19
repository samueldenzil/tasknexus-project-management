import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { api } from '@/lib/api'
import { projectsKeys } from '@/lib/query-keys'


type RequestType = { param: { projectId: string } }

export const useDeleteProject = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<void, Error, RequestType>({
    mutationFn: async ({ param }) => {
      await api.delete(`/api/v1/projects/${param.projectId}`)
    },
    onSuccess: () => {
      toast.success('Project deleted')
      queryClient.invalidateQueries({ queryKey: projectsKeys.lists() })
    },
    onError: () => {
      toast.error('Failed to delete project')
    },
  })

  return mutation
}
