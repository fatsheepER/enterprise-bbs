<script setup>
import { computed, reactive, watch, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import personPlaceholder from '../assets/person-placeholder.svg'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  bio: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const errors = reactive({
  username: '',
  nickname: '',
  email: '',
  bio: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

watchEffect(() => {
  if (!authStore.isLoggedIn) {
    router.replace({
      path: '/login',
      query: { redirect: route.fullPath },
    })
  }
})

watch(
  () => authStore.currentUser,
  (user) => {
    if (!user) {
      return
    }

    form.username = user.username || ''
    form.nickname = user.nickname || ''
    form.email = user.email || ''
    form.bio = user.bio || ''
  },
  { immediate: true },
)

const currentUser = computed(() => authStore.currentUser)
const avatarSrc = computed(() => currentUser.value?.avatar || personPlaceholder)
const hasProfileChanges = computed(() => {
  const user = currentUser.value

  if (!user) {
    return false
  }

  return (
    form.nickname.trim() !== (user.nickname || '') ||
    form.email.trim() !== (user.email || '') ||
    form.bio.trim() !== (user.bio || '')
  )
})
const hasAnyPassword = computed(() =>
  Boolean(form.oldPassword || form.newPassword || form.confirmPassword),
)
const hasCompletePassword = computed(() =>
  Boolean(form.oldPassword && form.newPassword && form.confirmPassword),
)
const canSave = computed(
  () => Boolean(form.username.trim()) && (hasProfileChanges.value || hasCompletePassword.value),
)

function clearErrors() {
  for (const key of Object.keys(errors)) {
    errors[key] = ''
  }
}

function validateForm() {
  clearErrors()

  if (!form.username.trim()) {
    errors.username = '用户名不能为空'
  }

  if (hasAnyPassword.value) {
    if (!form.oldPassword) {
      errors.oldPassword = '请输入旧密码'
    }

    if (!form.newPassword) {
      errors.newPassword = '请输入新密码'
    } else if (form.newPassword.length < 6) {
      errors.newPassword = '新密码长度至少 6 位'
    }

    if (!form.confirmPassword) {
      errors.confirmPassword = '请再次输入新密码'
    } else if (form.newPassword !== form.confirmPassword) {
      errors.confirmPassword = '两次输入的新密码不一致'
    }
  }

  return Object.values(errors).every((message) => !message)
}

function resetPasswordFields() {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}

function submitProfile() {
  if (!validateForm()) {
    return
  }

  try {
    if (hasProfileChanges.value) {
      authStore.updateProfile({
        nickname: form.nickname,
        avatar: currentUser.value?.avatar || '',
        email: form.email,
        bio: form.bio,
      })
    }

    if (hasCompletePassword.value) {
      authStore.changePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
      })
      resetPasswordFields()
    }

    router.push('/profile')
  } catch (error) {
    if (error.message === '旧密码错误') {
      errors.oldPassword = '旧密码错误'
      return
    }

    errors.bio = error.message || '保存失败'
  }
}
</script>

<template>
  <section v-if="currentUser" class="profile-edit-page">
    <header class="profile-edit-header">
      <h1 class="profile-edit-header__title">编辑个人资料</h1>
      <button
        class="profile-edit-header__save"
        type="button"
        :disabled="!canSave"
        @click="submitProfile"
      >
        保存
      </button>
    </header>

    <form class="profile-edit-card" @submit.prevent="submitProfile">
      <section class="profile-edit-section profile-edit-section--avatar">
        <div class="profile-edit-section__label">
          <h2>头像</h2>
          <p>用于个人主页和回复列表展示。</p>
        </div>

        <div class="profile-edit-avatar">
          <div class="profile-edit-avatar__preview" aria-hidden="true">
            <img class="profile-edit-avatar__image" :src="avatarSrc" alt="" />
          </div>
          <button class="profile-edit-avatar__button" type="button" disabled>更换头像</button>
        </div>
      </section>

      <section class="profile-edit-section">
        <div class="profile-edit-section__label">
          <h2>基础信息</h2>
          <p>用户名不可修改。</p>
        </div>

        <div class="profile-edit-fields">
          <label class="profile-edit-field">
            <span class="profile-edit-field__label">用户名</span>
            <input
              v-model="form.username"
              class="profile-edit-field__input profile-edit-field__input--disabled"
              type="text"
              disabled
            />
            <span v-if="errors.username" class="profile-edit-field__error">
              {{ errors.username }}
            </span>
          </label>

          <label class="profile-edit-field">
            <span class="profile-edit-field__label">昵称</span>
            <input
              v-model="form.nickname"
              class="profile-edit-field__input"
              type="text"
              autocomplete="nickname"
            />
            <span v-if="errors.nickname" class="profile-edit-field__error">
              {{ errors.nickname }}
            </span>
          </label>

          <label class="profile-edit-field">
            <span class="profile-edit-field__label">电子邮箱</span>
            <input
              v-model="form.email"
              class="profile-edit-field__input"
              type="email"
              autocomplete="email"
            />
            <span v-if="errors.email" class="profile-edit-field__error">
              {{ errors.email }}
            </span>
          </label>

          <label class="profile-edit-field">
            <span class="profile-edit-field__label">Bio</span>
            <textarea
              v-model="form.bio"
              class="profile-edit-field__input profile-edit-field__textarea"
              rows="4"
            ></textarea>
            <span v-if="errors.bio" class="profile-edit-field__error">
              {{ errors.bio }}
            </span>
          </label>
        </div>
      </section>

      <section class="profile-edit-section">
        <div class="profile-edit-section__label">
          <h2>修改密码</h2>
          <p>不修改密码时可留空。</p>
        </div>

        <div class="profile-edit-fields">
          <label class="profile-edit-field">
            <span class="profile-edit-field__label">旧密码</span>
            <input
              v-model="form.oldPassword"
              class="profile-edit-field__input"
              type="password"
              autocomplete="current-password"
            />
            <span v-if="errors.oldPassword" class="profile-edit-field__error">
              {{ errors.oldPassword }}
            </span>
          </label>

          <label class="profile-edit-field">
            <span class="profile-edit-field__label">新密码</span>
            <input
              v-model="form.newPassword"
              class="profile-edit-field__input"
              type="password"
              autocomplete="new-password"
            />
            <span v-if="errors.newPassword" class="profile-edit-field__error">
              {{ errors.newPassword }}
            </span>
          </label>

          <label class="profile-edit-field">
            <span class="profile-edit-field__label">确认新密码</span>
            <input
              v-model="form.confirmPassword"
              class="profile-edit-field__input"
              type="password"
              autocomplete="new-password"
            />
            <span v-if="errors.confirmPassword" class="profile-edit-field__error">
              {{ errors.confirmPassword }}
            </span>
          </label>
        </div>
      </section>
    </form>

    <div class="profile-edit-footer">
      <button
        class="profile-edit-header__save"
        type="button"
        :disabled="!canSave"
        @click="submitProfile"
      >
        保存
      </button>
    </div>
  </section>
</template>
