import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Project } from '@/features/projects/types'
import { api } from '@/lib/api'

type RequestType = {
  form: {
    name?: string
    image?: string | File
  }
  param: {
    projectId: string
  }
}

type ResponseType = Project

export const useUpdateProject = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ form, param }) => {
      return await api.patch<ResponseType>(`/api/v1/projects/${param.projectId}`, form)
    },
    onSuccess: (data) => {
      toast.success('Project updated')
      queryClient.invalidateQueries({ queryKey: ['projects'] })
      queryClient.invalidateQueries({ queryKey: ['project', data.id] })
    },
    onError: () => {
      toast.error('Failed to update project')
    },
  })

  return mutation
}
