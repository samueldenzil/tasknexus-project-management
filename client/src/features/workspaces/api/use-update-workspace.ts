import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'

import { Workspace } from '@/features/workspaces/types'
import { api } from '@/lib/api'
import { workspacesKeys } from '@/lib/query-keys'


type RequestType = {
  form: {
    name?: string
    image?: string | File // TODO: add support for this
  },
  param: {
    workspaceId: string
  }
}

type ResponseType = Workspace

export const useUpdateWorkspace = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ form, param }) => {
      return await api.patch<Workspace>(`/api/v1/workspaces/${param.workspaceId}`, form)
    },
    onSuccess: (data) => {
      toast.success('Workspace updated')
      queryClient.invalidateQueries({ queryKey: workspacesKeys.lists() })
      queryClient.invalidateQueries({ queryKey: workspacesKeys.detail(data.id) })
    },
    onError: () => {
      toast.error('Failed to update workspace')
    },
  })

  return mutation
}
