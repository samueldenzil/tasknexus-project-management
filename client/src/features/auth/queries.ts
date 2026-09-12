import { cookies } from 'next/headers'

import { api } from '@/lib/api'

export const getCurrent = async () => {
  try {
    // 1. Manually grab the cookie from the Next.js request
    const cookieStore = await cookies()
    const sessionCookie = cookieStore.get('jira-clone-session')

    if (!sessionCookie) {
      return null
    }

    // 2. Call Spring Boot, manually passing the cookie in the headers
    const response = await api.get('/api/v1/auth/me', {
      headers: {
        Cookie: `jira-clone-session=${sessionCookie.value}`,
      },
    })

    return response
  } catch {
    return null
  }
}
