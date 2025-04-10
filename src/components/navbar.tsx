'use client'

import { usePathname } from 'next/navigation'

import { UserButton } from '@/features/auth/components/user-button'
import { MobileSiderbar } from './mobile-siderbar'

const pathnameMap = {
  tasks: {
    title: 'My Tasks',
    description: 'View all your tasks here',
  },
  projects: {
    title: 'My Project',
    description: 'View tasks of your project here',
  },
}

const defaultMap = {
  title: 'Home',
  description: 'Monitor all of your projects and tasks here',
}

export const Navbar = () => {
  const pathname = usePathname()
  const partnameParts = pathname.split('/')
  const partnameKey = partnameParts[3] as keyof typeof pathnameMap

  const { title, description } = pathnameMap[partnameKey] ?? defaultMap

  return (
    <nav className="flex items-center justify-between px-6 pt-4">
      <div className="hidden flex-col lg:flex">
        <h1 className="text-2xl font-semibold">{title}</h1>
        <p className="text-muted-foreground">{description}</p>
      </div>
      <MobileSiderbar />
      <UserButton />
    </nav>
  )
}
