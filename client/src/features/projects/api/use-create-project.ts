import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import z from 'zod'

import { createProjectSchema } from '@/features/projects/schemas'
import { Project } from '@/features/projects/types'
import { api } from '@/lib/api'
import { projectsKeys } from '@/lib/query-keys'


type RequestType = z.infer<typeof createProjectSchema>
type ResponseType = Project

export const useCreateProject = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, { form: RequestType }>({
    mutationFn: async ({ form }) => {
      const response = await api.post<ResponseType>('/api/v1/projects', form)
      return response
    },
    onSuccess: () => {
      toast.success('Project created')
      queryClient.invalidateQueries({ queryKey: projectsKeys.lists() })
    },
    onError: () => {
      toast.error('Failed to create project')
    },
  })

  return mutation
}
