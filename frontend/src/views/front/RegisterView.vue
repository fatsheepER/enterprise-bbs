<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import personPlaceholder from '@/assets/person-placeholder.svg'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  email: '',
  bio: '',
})
const errorMessage = ref('')

async function submitRegister() {
  errorMessage.value = ''

  try {
    await authStore.register(form)
    router.push('/profile')
  } catch (error) {
    errorMessage.value = error.message || '注册失败'
  }
}
</script>

<template>
  <section class="auth-page">
    <form class="auth-card auth-card--register" @submit.prevent="submitRegister">
      <h1 class="auth-card__title">创建账户</h1>

      <div class="auth-avatar-preview" aria-hidden="true">
        <img class="auth-avatar-preview__image" :src="personPlaceholder" alt="" />
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
          />
        </label>

        <label class="auth-field">
          <span class="auth-field__label">个人简介</span>
          <textarea
            v-model="form.bio"
            class="auth-field__input auth-field__input--textarea"
            placeholder="个人简介"
            rows="3"
          ></textarea>
        </label>
      </div>

      <p v-if="errorMessage" class="auth-card__error">{{ errorMessage }}</p>

      <RouterLink class="auth-card__link" to="/login">已有账户，去登录</RouterLink>

      <button class="auth-card__submit" type="submit">创建账户</button>
    </form>
  </section>
</template>
