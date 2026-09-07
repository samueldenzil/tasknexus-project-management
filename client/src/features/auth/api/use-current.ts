import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { AuthResponse } from '@/features/auth/types'

export const useCurrent = () => {
  const query = useQuery<AuthResponse | null>({
    queryKey: ['current-user'],
    queryFn: async () => {
      try {
        const response = await api.get<AuthResponse>('/api/v1/auth/me')
        return response
      } catch {
        return null
      }
    },
  })

  return query
}
