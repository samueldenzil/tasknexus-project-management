'use client'

import { ArrowLeftIcon, MoreVerticalIcon } from 'lucide-react'
import Link from 'next/link'
import { Fragment } from 'react'

import { DottedSeparator } from '@/components/dotted-separator'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'
import { useDeleteMember } from '@/features/members/api/use-delete-member'
import { useGetMembers } from '@/features/members/api/use-get-workspaces'
import { useUpdateMember } from '@/features/members/api/use-update-member'
import { MemberAvatar } from '@/features/members/components/member-avatar'
import { MemberRole } from '@/features/members/types'
import { useWorkspaceId } from '@/features/workspaces/hooks/use-workspace-id'
import { useConfirm } from '@/hooks/use-confirm'

export const MembersList = () => {
  const workspaceId = useWorkspaceId()
  const [ConfirmDialog, confirm] = useConfirm(
    'Remove Member',
    'This member will be removed from the workspace',
    'destructive'
  )

  const { data: members } = useGetMembers({ workspaceId })
  const { mutate: deleteMember, isPending: isDeletingMember } = useDeleteMember()
  const { mutate: updateMember, isPending: isUpdatingMember } = useUpdateMember()

  const handleUpdateMember = (memberId: string, role: MemberRole) => {
    updateMember({ param: { memberId }, json: { role } })
  }

  const handleDeleteMember = async (memberId: string) => {
    const ok = await confirm()

    if (!ok) {
      return
    }

    deleteMember({ param: { memberId } }, { onSuccess: () => window.location.reload() })
  }

  return (
    <Card className="h-full w-full border-none shadow-none">
      <ConfirmDialog />
      <CardHeader className="flex flex-row items-center gap-x-4 space-y-0 p-7">
        <Button variant="secondary" size="sm" asChild>
          <Link href={`/workspaces/${workspaceId}`}>
            <ArrowLeftIcon className="mr-2 size-4" />
            Back
          </Link>
        </Button>
        <CardTitle className="text-xl font-bold">Members list</CardTitle>
      </CardHeader>
      <div className="px-7">
        <DottedSeparator />
      </div>
      <CardContent className="p-7">
        {members?.documents.map((member, index) => (
          <Fragment key={index}>
            <div className="flex items-center gap-x-2">
              <MemberAvatar name={member.name} className="size-10" fallbackClassName="text-lg" />
              <div className="flex flex-col">
                <p className="text-sm font-medium">{member.name}</p>
                <p className="text-xs text-muted-foreground">{member.email}</p>
              </div>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button className="ml-auto" variant={'secondary'} size={'icon'}>
                    <MoreVerticalIcon className="size-4 text-muted-foreground" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent side="bottom" align="end">
                  <DropdownMenuItem
                    className="font-medium"
                    onClick={() => handleUpdateMember(member.$id, MemberRole.ADMIN)}
                    disabled={isUpdatingMember}
                  >
                    Set as Administrator
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    className="font-medium"
                    onClick={() => handleUpdateMember(member.$id, MemberRole.MEMBER)}
                    disabled={isUpdatingMember}
                  >
                    Set as Member
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    className="font-medium text-amber-700"
                    onClick={() => handleDeleteMember(member.$id)}
                    disabled={isDeletingMember}
                  >
                    Remove {member.name}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            {index < members.documents.length - 1 && <Separator className="my-2.5" />}
          </Fragment>
        ))}
      </CardContent>
    </Card>
  )
}
