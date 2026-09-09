const BASE_URL = process.env.NEXT_PUBLIC_APP_URL

const parseResponse = async (response: Response) => {
  const text = await response.text()

  if (!text) {
    return {}
  }

  try {
    return JSON.parse(text)
  } catch {
    return { error: text }
  }
}

const get = async <T>(endpoint: string, customConfig?: RequestInit): Promise<T> => {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'GET',
    credentials: 'include',
    ...customConfig,
    headers: {
      ...customConfig?.headers,
    },
  })

  const data = await parseResponse(response)

  if (!response.ok) {
    throw new Error(data.error ?? 'An error occurred')
  }

  return data as T
}

const post = async <T>(endpoint: string, payload: unknown): Promise<T> => {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify(payload),
  })

  const data = await parseResponse(response)

  if (!response.ok) {
    throw new Error(data.error ?? 'An error occurred')
  }

  return data as T
}

const patch = async <T>(endpoint: string, payload: unknown): Promise<T> => {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify(payload),
  })

  const data = await parseResponse(response)

  if (!response.ok) {
    throw new Error(data.error ?? 'An error occurred')
  }

  return data as T
}

const del = async <T>(endpoint: string, customConfig?: RequestInit): Promise<T> => {
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'DELETE',
    credentials: 'include',
    ...customConfig,
    headers: {
      ...customConfig?.headers,
    },
  })

  const data = await parseResponse(response)

  if (!response.ok) {
    throw new Error(data.error ?? 'An error occurred')
  }

  return data as T
}

export const api = {
  get,
  post,
  patch,
  delete: del,
}
