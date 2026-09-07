import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'

import { api } from '@/lib/api'

export const useLogin = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: async ({ json }) => {
      const response = await api.post('/api/v1/auth/login', json)
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
