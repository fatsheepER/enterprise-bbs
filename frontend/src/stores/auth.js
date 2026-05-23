import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { users } from '../mock/users'

const CURRENT_USER_KEY = 'currentUser'

function withoutPassword(user) {
  const safeUser = { ...user }
  delete safeUser.password

  return safeUser
}

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(CURRENT_USER_KEY) || 'null')
  } catch {
    localStorage.removeItem(CURRENT_USER_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref(readStoredUser())

  const isLoggedIn = computed(() => Boolean(currentUser.value?.id))
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
  const displayName = computed(
    () => currentUser.value?.nickname || currentUser.value?.username || '当前用户',
  )

  function login({ username, password }) {
    const normalizedUsername = username.trim()
    const matchedUser = users.find(
      (user) => user.username === normalizedUsername && user.password === password,
    )

    if (!matchedUser) {
      throw new Error('用户名或密码错误')
    }

    const safeUser = withoutPassword(matchedUser)
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(safeUser))
    currentUser.value = safeUser

    return safeUser
  }

  function logout() {
    localStorage.removeItem(CURRENT_USER_KEY)
    currentUser.value = null
  }

  function syncFromStorage() {
    currentUser.value = readStoredUser()
  }

  return {
    currentUser,
    isLoggedIn,
    isAdmin,
    displayName,
    login,
    logout,
    syncFromStorage,
  }
})
