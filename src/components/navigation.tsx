import { SettingsIcon, UsersIcon } from 'lucide-react'
import Link from 'next/link'
import { GoCheckCircle, GoCheckCircleFill, GoHome, GoHomeFill } from 'react-icons/go'

import { cn } from '@/lib/utils'

const routes = [
  {
    label: 'Home',
    href: '/',
    icon: GoHome,
    activeIcon: GoHomeFill,
  },
  {
    label: 'My Tasks',
    href: '/tasks',
    icon: GoCheckCircle,
    activeIcon: GoCheckCircleFill,
  },
  {
    label: 'Settings',
    href: '/settings',
    icon: SettingsIcon,
    activeIcon: SettingsIcon,
  },
  {
    label: 'Members',
    href: '/members',
    icon: UsersIcon,
    activeIcon: UsersIcon,
  },
]

export const Navigation = () => {
  return (
    <ul>
      {routes.map((item) => {
        const isActive = false
        const Icon = isActive ? item.activeIcon : item.icon
        return (
          <Link href={item.href} key={item.href}>
            <div
              className={cn(
                'flex items-center gap-2.5 rounded-md p-2.5 font-medium text-neutral-500 transition hover:text-primary',
                isActive && 'bg-white text-primary shadow-sm hover:opacity-100'
              )}
            >
              <Icon className="size-5 text-neutral-500" />
              {item.label}
            </div>
          </Link>
        )
      })}
    </ul>
  )
}
