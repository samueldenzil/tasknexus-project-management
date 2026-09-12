import { cookies } from 'next/headers'

import { api } from '@/lib/api'
import { Workspace } from './types'

const getAuthHeaders = async () => {
  const cookieStore = await cookies()
  const sessionCookie = cookieStore.get('jira-clone-session')
  return sessionCookie ? { Cookie: `jira-clone-session=${sessionCookie.value}` } : {}
}

export const getWorkspaces = async () => {
  try {
    const headers = await getAuthHeaders()

    if (!headers.Cookie) {
      return []
    }

    return await api.get<Workspace[]>('/api/v1/workspaces', { headers })
  } catch {
    return []
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

    const data = await api.get<{ id: string; name: string; imageUrl?: string }>(
      `/api/v1/workspaces/${workspaceId}/info`,
      { headers }
    )

    return {
      name: data.name,
    }
  } catch {
    return null
  }
}
