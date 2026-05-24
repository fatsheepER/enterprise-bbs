<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})
const errorMessage = ref('')

async function submitLogin() {
  errorMessage.value = ''

  try {
    await authStore.login(form)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message || '用户名或密码错误'
  }
}
</script>

<template>
  <section class="auth-page">
    <form class="auth-card" @submit.prevent="submitLogin">
      <h1 class="auth-card__title">登录 Enterprise BBS</h1>

      <div class="auth-card__fields">
        <label class="auth-field">
          <span class="auth-field__label">用户名</span>
          <input
            v-model="form.username"
            class="auth-field__input"
            type="text"
            placeholder="用户名"
            autocomplete="username"
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
            autocomplete="current-password"
            required
          />
        </label>
      </div>

      <p v-if="errorMessage" class="auth-card__error">{{ errorMessage }}</p>

      <RouterLink class="auth-card__link" to="/register">创建账户</RouterLink>

      <button class="auth-card__submit" type="submit">登录</button>
    </form>
  </section>
</template>
