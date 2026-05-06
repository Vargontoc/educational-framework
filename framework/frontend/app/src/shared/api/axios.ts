import axios from 'axios'
import router from '@/router'
import { useSessionStore } from '@/stores/useSessionStore'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL
})

api.interceptors.request.use((config) => {
  const session = useSessionStore()
  if (session.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const session = useSessionStore()
      session.$reset()
      router.replace('/')
    }
    return Promise.reject(error)
  }
)

export default api
