import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import {
  loginUser,
  registerUser,
  updateUserPassword,
  updateUserProfile,
  uploadUserAvatar,
} from '@/api/user'

const CURRENT_USER_KEY = 'currentUser'

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(CURRENT_USER_KEY) || 'null')
  } catch {
    localStorage.removeItem(CURRENT_USER_KEY)
    return null
  }
}

function persistCurrentUser(user) {
  localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user))
}

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref(readStoredUser())

  const isLoggedIn = computed(() => Boolean(currentUser.value?.id))
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
  const displayName = computed(
    () => currentUser.value?.nickname || currentUser.value?.username || '当前用户',
  )

  async function login({ username, password }) {
    const data = await loginUser({
      username: username.trim(),
      password,
    })
    const safeUser = data.user
    persistCurrentUser(safeUser)
    currentUser.value = safeUser

    return safeUser
  }

  async function register({ username, password, nickname, email, bio }) {
    const safeUser = await registerUser({
      username: username.trim(),
      password,
      nickname: nickname.trim(),
      email: email.trim(),
      bio: bio.trim(),
    })
    persistCurrentUser(safeUser)
    currentUser.value = safeUser

    return safeUser
  }

  async function updateProfile({ nickname, email, bio }) {
    if (!currentUser.value?.id) {
      throw new Error('请先登录')
    }

    const safeUser = await updateUserProfile({
      nickname: nickname.trim(),
      email: email.trim(),
      bio: bio.trim(),
    })
    persistCurrentUser(safeUser)
    currentUser.value = safeUser

    return safeUser
  }

  async function uploadAvatar(file) {
    if (!currentUser.value?.id) {
      throw new Error('请先登录')
    }

    const safeUser = await uploadUserAvatar(file)
    persistCurrentUser(safeUser)
    currentUser.value = safeUser

    return safeUser
  }

  async function changePassword({ oldPassword, newPassword }) {
    if (!currentUser.value?.id) {
      throw new Error('请先登录')
    }

    return updateUserPassword({ oldPassword, newPassword })
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
    uploadAvatar,
    changePassword,
    logout,
    syncFromStorage,
  }
})
