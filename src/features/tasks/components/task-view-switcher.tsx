'use client'

import { Loader, PlusIcon } from 'lucide-react'
import { useQueryState } from 'nuqs'

import { DottedSeparator } from '@/components/dotted-separator'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useGetTasks } from '@/features/tasks/api/use-get-tasks'
import { useCreateTaskModal } from '@/features/tasks/hooks/use-create-task-modal'
import { useTaskFilters } from '@/features/tasks/hooks/use-task-filters'
import { useWorkspaceId } from '@/features/workspaces/hooks/use-workspace-id'
import { columns } from './columns'
import { DataFilters } from './data-filters'
import { DataTable } from './data-table'

export const TaskViewSwitcher = () => {
  const [{ status, assigneeId, projectId, dueDate }] = useTaskFilters()
  const [view, setView] = useQueryState('task-view', { defaultValue: 'table' })

  const workspaceId = useWorkspaceId()
  const { open } = useCreateTaskModal()
  const { data: task, isLoading: isLoadingTask } = useGetTasks({
    workspaceId,
    projectId,
    assigneeId,
    status,
    dueDate,
  })

  return (
    <Tabs defaultValue={view} onValueChange={setView} className="w-full flex-1 rounded-lg border">
      <div className="flex h-full flex-col overflow-auto p-4">
        <div className="flex flex-col items-center justify-between gap-y-2 lg:flex-row">
          <TabsList className="w-full lg:w-auto">
            <TabsTrigger value="table" className="h-8 w-full lg:w-auto">
              Table
            </TabsTrigger>
            <TabsTrigger value="kanban" className="h-8 w-full lg:w-auto">
              Kanban
            </TabsTrigger>
            <TabsTrigger value="calendar" className="h-8 w-full lg:w-auto">
              Calendar
            </TabsTrigger>
          </TabsList>
          <Button className="w-full lg:w-auto" size={'sm'} onClick={open}>
            <PlusIcon className="mr-2 size-4" />
            New
          </Button>
        </div>
        <DottedSeparator className="my-4" />
        <DataFilters />
        <DottedSeparator className="my-4" />
        {isLoadingTask ? (
          <div className="flex h-[200px] w-full flex-col items-center justify-center rounded-lg border">
            <Loader className="size-5 animate-spin text-muted-foreground" />
          </div>
        ) : (
          <>
            <TabsContent value="table" className="mt-0">
              <DataTable columns={columns} data={task?.documents ?? []} />
            </TabsContent>
            <TabsContent value="kanban" className="mt-0"></TabsContent>
            <TabsContent value="calendar" className="mt-0">
              {JSON.stringify(task, undefined, 2)}
            </TabsContent>
          </>
        )}
      </div>
    </Tabs>
  )
}
