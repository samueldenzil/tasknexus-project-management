import { zValidator } from '@hono/zod-validator'
import { Hono } from 'hono'

import { loginSchema, registerSchema } from '@/features/auth/schemas'

const app = new Hono()
  .post('/login', zValidator('json', loginSchema), async (c) => {
    const { email, password } = c.req.valid('json')

    console.log({ email, password })

    return c.json({ email, password })
  })
  .post('/register', zValidator('json', registerSchema), async (c) => {
    const { email, name, password } = c.req.valid('json')

    console.log({ email, name, password })

    return c.json({ email, name, password })
  })

export default app
