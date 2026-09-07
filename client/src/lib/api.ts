const BASE_URL = process.env.NEXT_PUBLIC_APP_URL

const get = async <T>(endpoint: string, customConfig?: RequestInit): Promise<T> => {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'GET',
    // CRITICAL: Tells the browser to send the HTTP-Only cookie to Spring Boot
    credentials: 'include',
    ...customConfig,
    headers: {
      ...customConfig?.headers,
    },
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error ?? 'An error occured')
  }

  return response.json()
}

const post = async <T>(endpoint: string, data: unknown): Promise<T> => {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify(data),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error ?? 'An error occurred')
  }

  return response.json()
}

export const api = { get, post }
