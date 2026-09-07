import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'
import { z } from 'zod'

import { loginSchema } from '@/features/auth/schemas'
import { AuthResponse } from '@/features/auth/types'
import { api } from '@/lib/api'

type RequestType = z.infer<typeof loginSchema>
type ResponseType = AuthResponse

export const useLogin = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, { json: RequestType }>({
    mutationFn: async ({ json }) => {
      const response = await api.post<ResponseType>('/api/v1/auth/login', json)
      return response
    },
    onSuccess: () => {
      toast.success('Logged in')
      router.refresh()
      queryClient.invalidateQueries({ queryKey: ['current-user'] })
    },
    onError: () => {
      toast.error('Failed to login')
    },
  })

  return mutation
}
