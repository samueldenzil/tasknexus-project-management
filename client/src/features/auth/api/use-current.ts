import { useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'

export const useCurrent = () => {
  const query = useQuery({
    queryKey: ['current-user'],
    queryFn: async () => {
      try {
        const response = await api.get('/api/v1/auth/me')
        return response
      } catch {
        return null
      }
    },
  })

  return query
}
