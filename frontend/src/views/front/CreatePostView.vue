<script setup>
import { computed, onMounted, reactive, ref, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getBoards } from '@/api/boards'
import { createPost } from '@/api/posts'
import { useAuthStore } from '@/stores/auth'

const TITLE_LIMIT = 100
const CONTENT_LIMIT = 5000

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const boards = ref([])
const form = reactive({
  title: '',
  content: '',
  boardId: '',
})
const errors = reactive({
  title: '',
  content: '',
  boardId: '',
})
const submitError = ref('')

const selectedBoard = computed(() =>
  boards.value.find((board) => board.id === Number(form.boardId)),
)

onMounted(async () => {
  boards.value = await getBoards()
})

watchEffect(() => {
  if (!authStore.isLoggedIn) {
    router.replace({
      path: '/login',
      query: { redirect: route.fullPath },
    })
  }
})

function clearErrors() {
  for (const key of Object.keys(errors)) {
    errors[key] = ''
  }

  submitError.value = ''
}

function validateForm() {
  clearErrors()

  const title = form.title.trim()
  const content = form.content.trim()

  if (!title) {
    errors.title = '请输入帖子标题'
  } else if (title.length > TITLE_LIMIT) {
    errors.title = `标题不能超过 ${TITLE_LIMIT} 个字符`
  }

  if (!content) {
    errors.content = '请输入帖子内容'
  } else if (content.length > CONTENT_LIMIT) {
    errors.content = `内容不能超过 ${CONTENT_LIMIT} 个字符`
  }

  if (!selectedBoard.value) {
    errors.boardId = '请选择有效版块'
  }

  return Object.values(errors).every((message) => !message)
}

async function submitPost() {
  if (!validateForm()) {
    return
  }

  try {
    const post = await createPost({
      boardId: Number(form.boardId),
      title: form.title,
      content: form.content,
    })

    router.push(`/posts/${post.id}`)
  } catch (error) {
    submitError.value = error.message || '发布失败'
  }
}
</script>

<template>
  <section v-if="authStore.isLoggedIn" class="create-post-page">
    <header class="create-post-header">
      <h1 class="create-post-header__title">发布新帖文</h1>
    </header>

    <form class="create-post-form" @submit.prevent="submitPost">
      <label class="create-post-field">
        <span class="create-post-field__label">标题</span>
        <input
          v-model="form.title"
          class="create-post-field__input"
          type="text"
          placeholder="输入帖子标题"
          autocomplete="off"
          maxlength="100"
        />
        <span v-if="errors.title" class="create-post-field__error">{{ errors.title }}</span>
      </label>

      <label class="create-post-field">
        <span class="create-post-field__label">内容</span>
        <textarea
          v-model="form.content"
          class="create-post-field__input create-post-field__textarea"
          placeholder="输入帖子内容"
          maxlength="5000"
          rows="12"
        ></textarea>
        <span v-if="errors.content" class="create-post-field__error">{{ errors.content }}</span>
        <span class="create-post-field__counter">{{ form.content.trim().length }} / 5000</span>
      </label>

      <label class="create-post-field create-post-field--select">
        <span class="create-post-field__label">版块</span>
        <select v-model="form.boardId" class="create-post-field__input create-post-field__select">
          <option value="" disabled>选择版块</option>
          <option v-for="board in boards" :key="board.id" :value="board.id">
            {{ board.name }}
          </option>
        </select>
        <span v-if="errors.boardId" class="create-post-field__error">{{ errors.boardId }}</span>
      </label>

      <p v-if="submitError" class="create-post-form__error">{{ submitError }}</p>

      <div class="create-post-form__actions">
        <button class="create-post-form__submit" type="submit">发布</button>
      </div>
    </form>
  </section>
</template>
