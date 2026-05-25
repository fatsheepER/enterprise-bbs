<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import personPlaceholder from '../assets/person-placeholder.svg'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const isMenuOpen = ref(false)
const searchKeyword = ref('')
const accountMenuRef = ref(null)
const avatarSrc = computed(() => authStore.currentUser?.avatar || personPlaceholder)
const validPostSorts = new Set(['latest', 'views', 'replies'])

watch(
  () => (route.name === 'posts' ? String(route.query.keyword || '') : ''),
  (keyword) => {
    searchKeyword.value = keyword
  },
  { immediate: true },
)

function toggleMenu() {
  isMenuOpen.value = !isMenuOpen.value
}

function closeMenu() {
  isMenuOpen.value = false
}

function signOut() {
  authStore.logout()
  closeMenu()

  if (route.path === '/profile' || route.path.startsWith('/admin')) {
    router.push('/')
  }
}

function goToCreatePost() {
  router.push({
    name: 'create-post',
    query: route.name === 'board-detail' ? { boardId: route.params.id } : {},
  })
}

function searchPosts() {
  const keyword = searchKeyword.value.trim()
  const query = {}

  if (keyword) {
    query.keyword = keyword
  }

  if (
    route.name === 'posts' &&
    validPostSorts.has(route.query.sort) &&
    route.query.sort !== 'latest'
  ) {
    query.sort = route.query.sort
  }

  router.push({ name: 'posts', query })
}

function handleDocumentPointerDown(event) {
  if (!accountMenuRef.value?.contains(event.target)) {
    closeMenu()
  }
}

function handleEscape(event) {
  if (event.key === 'Escape') {
    closeMenu()
  }
}

onMounted(() => {
  authStore.syncFromStorage()
  document.addEventListener('pointerdown', handleDocumentPointerDown)
  document.addEventListener('keydown', handleEscape)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointerDown)
  document.removeEventListener('keydown', handleEscape)
})
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner content-width">
      <RouterLink class="app-header__brand" to="/">Enterprise BBS</RouterLink>

      <form class="app-header__search search-box" role="search" @submit.prevent="searchPosts">
        <input
          v-model="searchKeyword"
          class="search-box__input"
          type="search"
          placeholder="按帖子标题搜索"
          aria-label="按帖子标题搜索"
        />
      </form>

      <div class="app-header__actions">
        <button class="post-button" type="button" @click="goToCreatePost">发帖</button>

        <div ref="accountMenuRef" class="account-menu-anchor">
          <button
            class="avatar-button"
            type="button"
            aria-label="用户菜单"
            :aria-expanded="isMenuOpen"
            @click="toggleMenu"
          >
            <img class="avatar-button__image" :src="avatarSrc" alt="" />
          </button>

          <div
            v-if="isMenuOpen && !authStore.isLoggedIn"
            class="account-menu account-menu--guest"
            role="menu"
          >
            <RouterLink class="account-menu__item" to="/login" role="menuitem" @click="closeMenu">
              登录
            </RouterLink>
            <RouterLink
              class="account-menu__item"
              to="/register"
              role="menuitem"
              @click="closeMenu"
            >
              创建账户
            </RouterLink>
          </div>

          <div v-else-if="isMenuOpen" class="account-menu account-menu--user" role="menu">
            <div class="account-menu__profile">
              <div class="account-menu__avatar" aria-hidden="true">
                <img class="account-menu__avatar-image" :src="avatarSrc" alt="" />
              </div>
              <div class="account-menu__profile-main">
                <strong class="account-menu__name">{{ authStore.displayName }}</strong>
                <div class="account-menu__links">
                  <RouterLink to="/profile" role="menuitem" @click="closeMenu">
                    个人资料
                  </RouterLink>
                  <span aria-hidden="true">|</span>
                  <RouterLink to="/profile/edit" role="menuitem" @click="closeMenu">
                    编辑资料
                  </RouterLink>
                </div>
              </div>
            </div>

            <RouterLink
              v-if="authStore.isAdmin"
              class="account-menu__item"
              to="/admin"
              role="menuitem"
              @click="closeMenu"
            >
              管理员后台
            </RouterLink>
            <button class="account-menu__item" type="button" role="menuitem" @click="signOut">
              退出登录
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
