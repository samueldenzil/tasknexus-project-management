import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'
import { z } from 'zod'

import { registerSchema } from '@/features/auth/schemas'
import { AuthResponse } from '@/features/auth/types'
import { api } from '@/lib/api'
import { authKeys } from '@/lib/query-keys'


type RequestType = z.infer<typeof registerSchema>
type ResponseType = AuthResponse

export const useRegister = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  const mutation = useMutation<ResponseType, Error, { json: RequestType }>({
    mutationFn: async ({ json }) => {
      const response = await api.post<ResponseType>('/api/v1/auth/register', json)
      return response
    },
    onSuccess: () => {
      toast.success('Registered')
      router.refresh()
      queryClient.invalidateQueries({ queryKey: authKeys.currentUser() })
    },
    onError: () => {
      toast.error('Failed to register')
    },
  })

  return mutation
}
