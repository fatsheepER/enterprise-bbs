<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import personPlaceholder from '@/assets/person-placeholder.svg'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const avatarInputRef = ref(null)
const pendingAvatarFile = ref(null)
const avatarPreviewUrl = ref('')
const avatarError = ref('')
const isSubmitting = ref(false)
const isAccountCreated = ref(false)
const avatarTypes = new Set(['image/jpeg', 'image/png', 'image/webp'])
const maxAvatarFileSize = 2 * 1024 * 1024

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  bio: '',
})
const errorMessage = ref('')

function selectAvatar() {
  avatarInputRef.value?.click()
}

function clearAvatarPreview() {
  if (avatarPreviewUrl.value) {
    URL.revokeObjectURL(avatarPreviewUrl.value)
    avatarPreviewUrl.value = ''
  }
}

function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''

  if (!file) {
    return
  }

  clearAvatarPreview()
  pendingAvatarFile.value = null

  if (!avatarTypes.has(file.type)) {
    avatarError.value = '仅支持 JPG、PNG 或 WebP 图片'
    return
  }

  if (file.size > maxAvatarFileSize) {
    avatarError.value = '头像文件不能超过 2MB'
    return
  }

  pendingAvatarFile.value = file
  avatarPreviewUrl.value = URL.createObjectURL(file)
  avatarError.value = ''
}

async function submitRegister() {
  if (isSubmitting.value) {
    return
  }

  errorMessage.value = ''
  avatarError.value = ''
  isSubmitting.value = true

  try {
    if (!isAccountCreated.value) {
      await authStore.register(form)
      isAccountCreated.value = true
    }

    if (pendingAvatarFile.value) {
      await authStore.uploadAvatar(pendingAvatarFile.value)
      pendingAvatarFile.value = null
      clearAvatarPreview()
    }

    router.push('/profile')
  } catch (error) {
    if (isAccountCreated.value) {
      avatarError.value = `${error.message || '头像上传失败'}，账户已创建，请重新选择头像或重试`
      return
    }

    errorMessage.value = error.message || '注册失败'
  } finally {
    isSubmitting.value = false
  }
}

onBeforeUnmount(clearAvatarPreview)
</script>

<template>
  <section class="auth-page">
    <form class="auth-card auth-card--register" @submit.prevent="submitRegister">
      <h1 class="auth-card__title">创建账户</h1>

      <div class="auth-avatar-control">
        <div class="auth-avatar-preview" aria-hidden="true">
          <img
            class="auth-avatar-preview__image"
            :src="avatarPreviewUrl || personPlaceholder"
            alt=""
          />
        </div>
        <input
          ref="avatarInputRef"
          class="auth-avatar-control__input"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          @change="handleAvatarChange"
        />
        <button
          class="auth-avatar-control__button"
          type="button"
          :disabled="isSubmitting"
          @click="selectAvatar"
        >
          {{ pendingAvatarFile ? '重新选择' : '上传头像' }}
        </button>
        <p v-if="avatarError" class="auth-avatar-control__error">{{ avatarError }}</p>
      </div>

      <div class="auth-card__fields">
        <label class="auth-field">
          <span class="auth-field__label">用户名</span>
          <input
            v-model="form.username"
            class="auth-field__input"
            type="text"
            placeholder="用户名"
            autocomplete="username"
            minlength="3"
            maxlength="20"
            :disabled="isAccountCreated || isSubmitting"
            required
          />
        </label>

        <label class="auth-field">
          <span class="auth-field__label">密码</span>
          <input
            v-model="form.password"
            class="auth-field__input"
            type="password"
            placeholder="密码"
            autocomplete="new-password"
            minlength="6"
            maxlength="32"
            :disabled="isAccountCreated || isSubmitting"
            required
          />
        </label>
      </div>

      <div class="auth-card__fields">
        <label class="auth-field">
          <span class="auth-field__label">邮箱</span>
          <input
            v-model="form.email"
            class="auth-field__input"
            type="email"
            placeholder="邮箱"
            autocomplete="email"
            :disabled="isAccountCreated || isSubmitting"
          />
        </label>
      </div>

      <div class="auth-card__fields">
        <label class="auth-field">
          <span class="auth-field__label">昵称</span>
          <input
            v-model="form.nickname"
            class="auth-field__input"
            type="text"
            placeholder="昵称"
            autocomplete="nickname"
            :disabled="isAccountCreated || isSubmitting"
          />
        </label>

        <label class="auth-field">
          <span class="auth-field__label">个人简介</span>
          <textarea
            v-model="form.bio"
            class="auth-field__input auth-field__input--textarea"
            placeholder="个人简介"
            rows="3"
            :disabled="isAccountCreated || isSubmitting"
          ></textarea>
        </label>
      </div>

      <p v-if="errorMessage" class="auth-card__error">{{ errorMessage }}</p>

      <RouterLink class="auth-card__link" to="/login">已有账户，去登录</RouterLink>

      <button class="auth-card__submit" type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '处理中...' : isAccountCreated ? '完成并进入个人主页' : '创建账户' }}
      </button>
    </form>
  </section>
</template>
