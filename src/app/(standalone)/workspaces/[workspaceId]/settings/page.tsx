import { redirect } from 'next/navigation'

import { getCurrent } from '@/features/auth/query'
import { EditWorkspaceForm } from '@/features/workspaces/components/edit-workspace-form'
import { getWorkspace } from '@/features/workspaces/query'

interface WorkspaceIdSettingsPageProps {
  params: Promise<{ workspaceId: string }>
}

const WorkspaceIdSettingsPage = async ({ params }: WorkspaceIdSettingsPageProps) => {
  const { workspaceId } = await params
  const user = await getCurrent()

  if (!user) {
    redirect('/sign-in')
  }

  const initialValues = await getWorkspace({ workspaceId })

  if (!initialValues) {
    redirect(`/workspaces/${workspaceId}`)
  }

  return (
    <div className="w-full lg:max-w-xl">
      <EditWorkspaceForm initialValue={initialValues} />
    </div>
  )
}

export default WorkspaceIdSettingsPage
