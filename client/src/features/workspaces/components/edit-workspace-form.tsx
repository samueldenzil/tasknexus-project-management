'use client'

import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeftIcon, CopyIcon, ImageIcon } from 'lucide-react'
import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { useRef } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'

import { DottedSeparator } from '@/components/dotted-separator'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useDeleteWorkspace } from '@/features/workspaces/api/use-delete-workspace'
import { useResetInviteCode } from '@/features/workspaces/api/use-reset-invite-code'
import { useUpdateWorkspace } from '@/features/workspaces/api/use-update-workspace'
import { updateWorkspaceSchema } from '@/features/workspaces/schemas'
import { Workspace } from '@/features/workspaces/types'
import { useConfirm } from '@/hooks/use-confirm'
import { cn } from '@/lib/utils'

interface EditWorkspaceFormProps {
  onCancel?: () => void
  initialValue: Workspace
}

export const EditWorkspaceForm = ({ initialValue, onCancel }: EditWorkspaceFormProps) => {
  const router = useRouter()
  const { mutate: updateWorkspace, isPending: isUpdatingWorkspace } = useUpdateWorkspace()
  const { mutate: resetInviteCode, isPending: isResetingInviteCode } = useResetInviteCode()
  const { mutate: deleteWorkspace, isPending: isDeletingWorkspace } = useDeleteWorkspace()

  const [DeleteDialog, confirmDelete] = useConfirm(
    'Delete Workspace',
    'This action cannot be undone.',
    'destructive'
  )
  const [ResetDialog, confirmReset] = useConfirm(
    'Reset Invite Link',
    'This will invalidate the current invite link.',
    'destructive'
  )

  const inputRef = useRef<HTMLInputElement>(null)

  const form = useForm<z.infer<typeof updateWorkspaceSchema>>({
    resolver: zodResolver(updateWorkspaceSchema),
    defaultValues: {
      ...initialValue,
      image: initialValue.imageUrl ?? '',
    },
  })

  const handleDelete = async () => {
    const ok = await confirmDelete()

    if (!ok) {
      return
    }

    deleteWorkspace(
      { param: { workspaceId: initialValue.id } },
      {
        onSuccess: () => {
          window.location.href = '/'
        },
      }
    )
  }

  const handleResetInviteCode = async () => {
    const ok = await confirmReset()

    if (!ok) {
      return
    }

    resetInviteCode({ param: { workspaceId: initialValue.id } })
  }

  const onSubmit = (values: z.infer<typeof updateWorkspaceSchema>) => {
    const finalValue = {
      ...values,
      image: values.image instanceof File ? values.image : '',
    }

    updateWorkspace(
      { form: finalValue, param: { workspaceId: initialValue.id } },
      {
        onError: () => {
          form.reset()
        },
      }
    )
  }

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]

    if (file) {
      form.setValue('image', file)
    }
  }

  const fullInviteLink = `${window.location.origin}/workspaces/${initialValue.id}/join/${initialValue.inviteCode}`

  const handleCopyInviteLink = () => {
    navigator.clipboard
      .writeText(fullInviteLink)
      .then(() => toast.success('Invite link copied to clipboard'))
  }

  return (
    <div className="flex flex-col gap-y-4">
      <ResetDialog />
      <DeleteDialog />
      <Card className="h-full w-full border-none shadow-none">
        <CardHeader className="flex flex-row items-center gap-x-4 space-y-0 p-7">
          <Button
            variant="secondary"
            size="sm"
            onClick={onCancel ? onCancel : () => router.push(`/workspaces/${initialValue.id}`)}
          >
            <ArrowLeftIcon className="mr-2 size-4" />
            Back
          </Button>
          <CardTitle className="text-xl font-bold">{initialValue.name}</CardTitle>
        </CardHeader>
        <div className="px-7">
          <DottedSeparator />
        </div>
        <CardContent className="p-7">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)}>
              <div className="flex flex-col gap-y-4">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Workspace Name</FormLabel>
                      <FormControl>
                        <Input
                          placeholder="Enter workspace name"
                          disabled={isUpdatingWorkspace}
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="image"
                  render={({ field }) => (
                    <div className="flex flex-col gap-y-2">
                      <div className="flex items-center gap-x-5">
                        {field.value ? (
                          <div className="relative size-[72px] overflow-hidden rounded-md">
                            <Image
                              src={
                                field.value instanceof File
                                  ? URL.createObjectURL(field.value)
                                  : field.value
                              }
                              fill
                              className="object-cover"
                              alt="workspace-icon-image"
                            />
                          </div>
                        ) : (
                          <Avatar className="size-[72px]">
                            <AvatarFallback>
                              <ImageIcon className="size-9 text-neutral-400" />
                            </AvatarFallback>
                          </Avatar>
                        )}
                        <div className="flex flex-col">
                          <p className="text-sm">Workspace Icon</p>
                          <p className="text-sm text-muted-foreground">
                            JPG, PNG, SVG or JPEG, max 1mb
                          </p>
                          <input
                            className="hidden"
                            type="file"
                            accept=".jpg, .png, .svg, .jpeg"
                            ref={inputRef}
                            onChange={handleImageChange}
                            disabled={isUpdatingWorkspace}
                          />
                          {field.value ? (
                            <Button
                              variant={'destructive'}
                              size={'xs'}
                              type="button"
                              disabled={isUpdatingWorkspace}
                              className="mt-2 w-fit"
                              onClick={() => {
                                field.onChange(null)
                                if (inputRef.current) {
                                  inputRef.current.value = ''
                                }
                              }}
                            >
                              Remove image
                            </Button>
                          ) : (
                            <Button
                              variant={'tertiary'}
                              size={'xs'}
                              type="button"
                              disabled={isUpdatingWorkspace}
                              className="mt-2 w-fit"
                              onClick={() => inputRef.current?.click()}
                            >
                              Upload image
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                />
              </div>
              <DottedSeparator className="py-7" />
              <div className="flex items-center justify-between">
                <Button
                  type="button"
                  size="lg"
                  variant="secondary"
                  disabled={isUpdatingWorkspace}
                  onClick={onCancel}
                  className={cn(!onCancel && 'invisible')}
                >
                  Cancel
                </Button>
                <Button type="submit" size="lg" disabled={isUpdatingWorkspace}>
                  Save Changes
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>

      <Card className="h-full w-full border-none shadow-none">
        <CardContent className="p-7">
          <div className="flex flex-col">
            <h3 className="font-bold">Invite Members</h3>
            <p className="text-sm text-muted-foreground">
              Use the invite link to add members to your workspace.
            </p>
            <div className="mt-4">
              <div className="flex items-center gap-x-2">
                <Input disabled value={fullInviteLink} />
                <Button onClick={handleCopyInviteLink} variant="secondary" className="size-12">
                  <CopyIcon className="size-5" />
                </Button>
              </div>
            </div>
            <DottedSeparator className="py-7" />
            <Button
              variant="destructive"
              size="sm"
              className="ml-auto mt-6 w-fit"
              type="button"
              disabled={isUpdatingWorkspace || isResetingInviteCode}
              onClick={handleResetInviteCode}
            >
              Reset invite link
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card className="h-full w-full border-none shadow-none">
        <CardContent className="p-7">
          <div className="flex flex-col">
            <h3 className="font-bold">Danger Zone</h3>
            <p className="text-sm text-muted-foreground">
              Deleting a workspace is irreversible and will remove all associated data.
            </p>
            <DottedSeparator className="py-7" />
            <Button
              variant="destructive"
              size="sm"
              className="ml-auto mt-6 w-fit"
              type="button"
              disabled={isUpdatingWorkspace || isDeletingWorkspace}
              onClick={handleDelete}
            >
              Delete workspace
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
