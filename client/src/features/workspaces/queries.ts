import { cookies } from 'next/headers'

import { api } from '@/lib/api'

const getAuthHeaders = async () => {
  const cookieStore = await cookies()
  const sessionCookie = cookieStore.get('jira-clone-session')
  return sessionCookie ? { Cookie: `jira-clone-session=${sessionCookie.value}` } : {}
}

export const getWorkspaces = async () => {
  try {
    const headers = await getAuthHeaders()

    if (!headers.Cookie) {
      return { documents: [], total: 0 }
    }

    const data = await api.get<unknown[]>('/api/v1/workspaces', { headers })

    return {
      documents: data,
      total: data.length,
    }
  } catch {
    return { documents: [], total: 0 }
  }
}

interface GetWorkspaceInfoProps {
  workspaceId: string
}

export const getWorkspaceInfo = async ({ workspaceId }: GetWorkspaceInfoProps) => {
  try {
    const headers = await getAuthHeaders()

    if (!headers.Cookie) {
      return null
    }

    const data = await api.get<any>(`/api/v1/workspaces/${workspaceId}`, { headers })

    return {
      name: data.name,
    }
  } catch {
    return null
  }
}
