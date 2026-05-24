import axios from 'axios'

const CURRENT_USER_KEY = 'currentUser'

function readCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem(CURRENT_USER_KEY) || 'null')
  } catch {
    localStorage.removeItem(CURRENT_USER_KEY)
    return null
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const currentUser = readCurrentUser()

  if (currentUser?.id) {
    config.headers['X-User-Id'] = currentUser.id
  }

  if (currentUser?.role) {
    config.headers['X-User-Role'] = currentUser.role
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data

    if (!result || typeof result.code === 'undefined') {
      return result
    }

    if (result.code !== 200) {
      const error = new Error(result.message || '请求失败')
      error.code = result.code
      error.data = result.data
      throw error
    }

    return result.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络请求失败'
    throw new Error(message)
  },
)

export default request
