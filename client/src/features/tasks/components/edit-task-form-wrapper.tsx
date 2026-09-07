import { Loader } from 'lucide-react'

import { Card, CardContent } from '@/components/ui/card'
import { useGetMembers } from '@/features/members/api/use-get-members'
import { useGetProjects } from '@/features/projects/api/use-get-projects'
import { useGetTask } from '@/features/tasks/api/use-get-task'
import { useWorkspaceId } from '@/features/workspaces/hooks/use-workspace-id'
import { EditTaskForm } from './edit-task-form'

interface EditTaskFormWrapperProps {
  id: string
  onCancel: () => void
}

export const EditTaskFormWrapper = ({ id, onCancel }: EditTaskFormWrapperProps) => {
  const workspaceId = useWorkspaceId()

  const { data: initialValues, isLoading: isLoadingTask } = useGetTask({ taskId: id })
  const { data: projects, isLoading: isLoadingProjects } = useGetProjects({ workspaceId })
  const { data: members, isLoading: isLoadingMembers } = useGetMembers({ workspaceId })

  const isLoading = isLoadingProjects || isLoadingMembers || isLoadingTask

  const projectOptions = projects?.map((project) => ({
    id: project.id,
    name: project.name,
    imageUrl: project.imageUrl,
  }))

  const memberOptions = members?.documents.map((member) => ({
    id: member.$id,
    name: member.name,
  }))

  if (isLoading) {
    return (
      <Card className="h-[714px] w-full border-none shadow-none">
        <CardContent className="flex h-full items-center justify-center">
          <Loader className="size-5 animate-spin text-muted-foreground" />
        </CardContent>
      </Card>
    )
  }

  if (!initialValues) {
    return null
  }

  return (
    <EditTaskForm
      onCancel={onCancel}
      initialValues={initialValues}
      projectOptions={projectOptions ?? []}
      memberOptions={memberOptions ?? []}
    />
  )
}
