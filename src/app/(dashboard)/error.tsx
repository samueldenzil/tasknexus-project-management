'use client'

import { AlertTriangle } from 'lucide-react'
import Link from 'next/link'

import { Button } from '@/components/ui/button'

const ErrorPage = () => {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-y-4">
      <AlertTriangle className="size-6 text-muted-foreground" />
      <p className="text-sm text-muted-foreground">Something went wrong</p>
      <Button variant={'secondary'} size={'sm'} asChild>
        <Link href="/">Back to home</Link>
      </Button>
    </div>
  )
}

export default ErrorPage
