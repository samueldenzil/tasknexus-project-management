'use client'

import { PlusIcon } from 'lucide-react'

import { DottedSeparator } from '@/components/dotted-separator'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useCreateTaskModal } from '@/features/tasks/hooks/use-create-task-modal'

export const TaskViewSwitcher = () => {
  const { open } = useCreateTaskModal()

  return (
    <Tabs defaultValue="account" className="w-full flex-1 rounded-lg border">
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
        Data Filters
        {/* TODO: filters */}
        <DottedSeparator className="my-4" />
        <>
          <TabsContent value="table" className="mt-0">
            Data table
          </TabsContent>
          <TabsContent value="kanban" className="mt-0">
            Kanban
          </TabsContent>
          <TabsContent value="calendar" className="mt-0">
            Calendar
          </TabsContent>
        </>
      </div>
    </Tabs>
  )
}
