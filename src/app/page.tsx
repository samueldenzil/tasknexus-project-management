import { Button } from '@/components/ui/button'

export default function Home() {
  return (
    <div className="flex gap-2">
      <button className=""></button>
      <Button>Primary</Button>
      <Button variant={'secondary'} className="">
        Secondary
      </Button>
      <Button className="" variant={'destructive'}>
        Destructive
      </Button>
      <Button variant={'ghost'} className="">
        Ghost
      </Button>
      <Button
        variant={'tertiary'}
        className="border-transparent bg-blue-100 text-blue-600 shadow-none hover:bg-blue-200"
      >
        Link
      </Button>
      <Button variant={'outline'}>Outline</Button>
    </div>
  );
}
