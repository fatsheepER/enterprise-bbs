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
  const userById = new Map(users.map((user) => [user.id, user]))

  for (const mockUser of readMockUsers()) {
    userById.set(mockUser.id, mockUser)
  }

  return [...userById.values()]
}

function upsertMockUser(user) {
  const mockUsers = readMockUsers()
  const nextMockUsers = mockUsers.some((item) => item.id === user.id)
    ? mockUsers.map((item) => (item.id === user.id ? user : item))
    : [...mockUsers, user]

  writeMockUsers(nextMockUsers)
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
    upsertMockUser(newUser)

    const safeUser = withoutPassword(newUser)
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(safeUser))
    currentUser.value = safeUser

    return safeUser
  }

  function updateProfile({ nickname, avatar, email, bio }) {
    if (!currentUser.value?.id) {
      throw new Error('请先登录')
    }

    const existingUser = allUsers().find((user) => user.id === currentUser.value.id)

    if (!existingUser) {
      throw new Error('用户不存在')
    }

    const updatedAt = formatTimestamp()
    const updatedUser = {
      ...existingUser,
      nickname: nickname.trim(),
      avatar: avatar?.trim() ?? '',
      email: email.trim(),
      bio: bio.trim(),
      updatedAt,
    }

    upsertMockUser(updatedUser)

    const safeUser = withoutPassword(updatedUser)
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(safeUser))
    currentUser.value = safeUser

    return safeUser
  }

  function changePassword({ oldPassword, newPassword }) {
    if (!currentUser.value?.id) {
      throw new Error('请先登录')
    }

    const existingUser = allUsers().find((user) => user.id === currentUser.value.id)

    if (!existingUser) {
      throw new Error('用户不存在')
    }

    if (existingUser.password !== oldPassword) {
      throw new Error('旧密码错误')
    }

    const updatedUser = {
      ...existingUser,
      password: newPassword,
      updatedAt: formatTimestamp(),
    }

    upsertMockUser(updatedUser)

    return true
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
    updateProfile,
    changePassword,
    logout,
    syncFromStorage,
  }
})
