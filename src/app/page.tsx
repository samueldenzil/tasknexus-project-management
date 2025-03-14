'use client'

import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

import { Button } from '@/components/ui/button'
import { useCurrent } from '@/features/auth/api/use-current'
import { useLogout } from '@/features/auth/api/use-logout'

export default function Home() {
  const router = useRouter()
  const { data, isLoading } = useCurrent()
  const { mutate } = useLogout()

  useEffect(() => {
    if (!data && isLoading) {
      router.push('/sign-in')
    }
  }, [data, isLoading, router])

  return (
    <div className="flex gap-2">
      only vissible to authorized users
      <Button onClick={() => mutate()}>Logout</Button>
      <pre>{JSON.stringify(data, undefined, 2)}</pre>
    </div>
  )
}
