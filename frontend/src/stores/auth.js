import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { users } from '../mock/users'

const CURRENT_USER_KEY = 'currentUser'
const MOCK_USERS_KEY = 'mockUsers'

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

function readMockUsers() {
  try {
    const storedUsers = JSON.parse(localStorage.getItem(MOCK_USERS_KEY) || '[]')

    return Array.isArray(storedUsers) ? storedUsers : []
  } catch {
    localStorage.removeItem(MOCK_USERS_KEY)
    return []
  }
}

function writeMockUsers(mockUsers) {
  localStorage.setItem(MOCK_USERS_KEY, JSON.stringify(mockUsers))
}

function allUsers() {
  return [...users, ...readMockUsers()]
}

function formatTimestamp(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
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
    const matchedUser = allUsers().find(
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

  function register({ username, password, nickname, avatar, email, bio }) {
    const normalizedUsername = username.trim()
    const normalizedNickname = nickname.trim() || normalizedUsername
    const normalizedAvatar = avatar?.trim() ?? ''
    const normalizedEmail = email.trim()
    const normalizedBio = bio.trim()

    if (allUsers().some((user) => user.username === normalizedUsername)) {
      throw new Error('用户名已存在')
    }

    const now = formatTimestamp()
    const newUser = {
      id: Math.max(0, ...allUsers().map((user) => user.id)) + 1,
      username: normalizedUsername,
      password,
      nickname: normalizedNickname,
      avatar: normalizedAvatar,
      email: normalizedEmail,
      bio: normalizedBio,
      role: 'USER',
      createdAt: now,
      updatedAt: now,
    }
    const nextMockUsers = [...readMockUsers(), newUser]
    writeMockUsers(nextMockUsers)

    const safeUser = withoutPassword(newUser)
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
    register,
    logout,
    syncFromStorage,
  }
})
