import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'

import { api } from '@/lib/api'

export const useLogout = () => {
  const router = useRouter()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: async () => {
      return await api.post('/api/v1/auth/logout', {})
    },
    onSuccess: () => {
      toast.success('Logged out')
      router.refresh()
      queryClient.invalidateQueries()
    },
    onError: () => {
      toast.error('Failed to log out')
    },
  })

  return mutation
}
