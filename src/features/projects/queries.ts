import { DATABASE_ID, PROJECTS_ID } from '@/config'
import { getMember } from '@/features/members/utils'
import { createSessionClient } from '@/lib/appwrite'
import { Project } from './types'

interface GetProjectProps {
  projectId: string
}

export const getProject = async ({ projectId }: GetProjectProps) => {
  const { account, databases } = await createSessionClient()

  const project = await databases.getDocument<Project>(DATABASE_ID, PROJECTS_ID, projectId)

  if (!project) {
    throw new Error('Project not found')
  }

  const user = await account.get()
  const member = await getMember({
    databases,
    workspaceId: project.workspaceId,
    userId: user.$id,
  })

  if (!member) {
    throw new Error('Unauthorized')
  }

  return project
}
