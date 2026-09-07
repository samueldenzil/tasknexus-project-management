import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'

import { api } from '@/lib/api'
import { z } from 'zod'
import { createWorkspaceSchema } from '../schemas'
import { Workspace } from '../types'

type RequestType = z.infer<typeof createWorkspaceSchema>
type ResponseType = Workspace

export const useCreateWorkspace = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, { json: RequestType }>({
    mutationFn: async ({ json }) => {
      const response = await api.post<ResponseType>('/api/v1/workspaces', json)
      return response
    },
    onSuccess: () => {
      toast.success('Workspace created')

      router.refresh()
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
    },
    onError: () => {
      toast.error('Failed to create workspace')
    },
  })

  return mutation
}
