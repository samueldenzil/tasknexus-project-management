import * as React from 'react'
import { Slot } from '@radix-ui/react-slot'
import { cva, type VariantProps } from 'class-variance-authority'

import { cn } from '@/lib/utils'

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:bg-nutral-100 disabled:from-nutral-100 disabled:to-nutral-100 disabled:text-nutral-300 border border-nutral-200 shadown-sm [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        primary:
          'text-primary-foreground bg-gradient-to-b from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-700',
        destructive: 'bg-gradient-to-b from-amber-600 to-amber-700 text-white hover:from-amber-700 hover:to-amber-700',
        outline:
          'border border-input bg-background shadow-xs hover:bg-accent hover:text-accent-foreground',
        secondary: 'bg-white text-black hover:bg-neutral-100',
        ghost: 'hover:bg-accent hover:text-accent-foreground border-transparent shadow-none',
        muted: 'bg-neutral-200 text-neutral-600 hover:bg-neutral-200/80',
        tertiary: 'border-transparent bg-blue-100 text-blue-600 shadow-none hover:bg-blue-200',
      },
      size: {
        default: 'h-10 px-4 py-2',
        xs: 'h-7 rounded-md px-2 text-xs',
        sm: 'h-8 rounded-md px-3',
        lg: 'h-12 rounded-md px-8',
        icon: 'size-8',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'default',
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button'
    return (
      <Comp className={cn(buttonVariants({ variant, size, className }))} ref={ref} {...props} />
    )
  }
)
Button.displayName = 'Button'

export { Button, buttonVariants }
