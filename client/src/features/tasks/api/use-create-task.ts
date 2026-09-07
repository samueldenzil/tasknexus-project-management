import { useMutation, useQueryClient } from '@tanstack/react-query'
import { InferRequestType, InferResponseType } from 'hono'
import { toast } from 'sonner'

import { client } from '@/lib/rpc'
import { api } from '@/lib/api'

type RequestType = InferRequestType<(typeof client.api.tasks)['$post']>
type ResponseType = InferResponseType<(typeof client.api.tasks)['$post'], 200>

export const useCreateTask = () => {
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, RequestType>({
    mutationFn: async ({ json }) => {
      return await api.post('/api/v1/tasks', { json })
    },
    onSuccess: () => {
      toast.success('Task created')
      queryClient.invalidateQueries({ queryKey: ['tasks'] })
    },
    onError: () => {
      toast.error('Failed to create task')
    },
  })

  return mutation
}
