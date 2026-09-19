import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'
import { AuthResponse } from '@/features/auth/types'
import { authKeys } from '@/lib/query-keys'


export const useCurrent = () => {
  const query = useQuery<AuthResponse | null>({
    queryKey: authKeys.currentUser(),
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
