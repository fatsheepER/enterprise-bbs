<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AdminTable from '@/components/AdminTable.vue'
import { adminReplies, updateAdminReplyStatus } from '@/mock/forumViewModels'
import { useAuthStore } from '@/stores/auth'

const pageSize = 10

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const idQuery = ref('')
const keywordQuery = ref('')
const refreshKey = ref(0)
const activePage = ref(1)
const hiddenPage = ref(1)

const columns = [
  { key: 'id', label: 'ID', className: 'admin-replies-table__id', align: 'center' },
  { key: 'contentPreview', label: '内容节选', className: 'admin-replies-table__content' },
  { key: 'authorName', label: '用户名', className: 'admin-replies-table__author' },
  { key: 'createdAt', label: '回复时间', className: 'admin-replies-table__time' },
]

watch(
  () => [authStore.isLoggedIn, authStore.isAdmin, route.fullPath],
  () => {
    if (!authStore.isLoggedIn) {
      router.replace({
        path: '/login',
        query: { redirect: route.fullPath },
      })

      return
    }

    if (!authStore.isAdmin) {
      router.replace('/')
    }
  },
  { immediate: true },
)

watch([idQuery, keywordQuery], () => {
  activePage.value = 1
  hiddenPage.value = 1
})

const normalizedIdQuery = computed(() => {
  const value = idQuery.value.trim()

  return value ? Number(value) : null
})

const replyFilters = computed(() => ({
  id: normalizedIdQuery.value,
  keyword: keywordQuery.value,
  refreshKey: refreshKey.value,
}))

const filteredReplies = computed(() => {
  const { id, keyword } = replyFilters.value

  return adminReplies({
    id,
    keyword,
  })
})

const activeReplies = computed(() => filteredReplies.value.filter((reply) => reply.status === 1))
const hiddenReplies = computed(() => filteredReplies.value.filter((reply) => reply.status === 0))

const activeTotalPages = computed(() => totalPages(activeReplies.value))
const hiddenTotalPages = computed(() => totalPages(hiddenReplies.value))

const pagedActiveReplies = computed(() => pageItems(activeReplies.value, activePage.value))
const pagedHiddenReplies = computed(() => pageItems(hiddenReplies.value, hiddenPage.value))

function totalPages(rows) {
  return Math.max(1, Math.ceil(rows.length / pageSize))
}

function pageItems(rows, page) {
  const safePage = Math.min(Math.max(page, 1), totalPages(rows))
  const start = (safePage - 1) * pageSize

  return rows.slice(start, start + pageSize)
}

function splitDateTime(dateTime) {
  const [date, time = ''] = dateTime.split(' ')

  return {
    date,
    time,
  }
}

function setReplyStatus(reply, status) {
  updateAdminReplyStatus(reply.id, status)
  refreshKey.value += 1
  activePage.value = Math.min(activePage.value, activeTotalPages.value)
  hiddenPage.value = Math.min(hiddenPage.value, hiddenTotalPages.value)
}

function previousActivePage() {
  activePage.value = Math.max(1, activePage.value - 1)
}

function nextActivePage() {
  activePage.value = Math.min(activeTotalPages.value, activePage.value + 1)
}

function previousHiddenPage() {
  hiddenPage.value = Math.max(1, hiddenPage.value - 1)
}

function nextHiddenPage() {
  hiddenPage.value = Math.min(hiddenTotalPages.value, hiddenPage.value + 1)
}
</script>

<template>
  <section v-if="authStore.isAdmin" class="admin-replies-page">
    <header class="admin-replies-page__header">
      <div>
        <RouterLink class="admin-replies-page__back" to="/admin">管理员控制台</RouterLink>
        <h1 class="admin-replies-page__title">回复管理</h1>
      </div>
    </header>

    <form class="admin-replies-filter" @submit.prevent>
      <label class="admin-replies-filter__field">
        <span>ID</span>
        <input v-model="idQuery" type="number" min="1" placeholder="精确搜索" />
      </label>

      <label class="admin-replies-filter__field">
        <span>内容或用户名</span>
        <input v-model="keywordQuery" type="search" placeholder="按内容或用户名搜索" />
      </label>
    </form>

    <section class="admin-replies-section" aria-labelledby="active-replies-title">
      <div class="admin-replies-section__header">
        <h2 id="active-replies-title" class="admin-replies-section__title">正常回复</h2>
        <span class="admin-replies-section__count">{{ activeReplies.length }}</span>
      </div>

      <AdminTable
        :columns="columns"
        :rows="pagedActiveReplies"
        aria-label="正常回复列表"
        empty-text="暂无正常回复"
      >
        <template #cell-contentPreview="{ row, value }">
          <RouterLink class="admin-replies-content admin-replies-content-link" :to="row.href">
            {{ value }}
          </RouterLink>
        </template>

        <template #cell-createdAt="{ value }">
          <time class="admin-replies-time">
            <span>{{ splitDateTime(value).date }}</span>
            <span>{{ splitDateTime(value).time }}</span>
          </time>
        </template>

        <template #actions="{ row }">
          <button
            class="admin-table__action admin-table__action--danger"
            type="button"
            @click="setReplyStatus(row, 0)"
          >
            隐藏
          </button>
        </template>
      </AdminTable>

      <nav class="admin-replies-pagination" aria-label="正常回复分页">
        <button type="button" :disabled="activePage === 1" @click="previousActivePage">
          上一页
        </button>
        <span>{{ activePage }} / {{ activeTotalPages }}</span>
        <button
          type="button"
          :disabled="activePage === activeTotalPages"
          @click="nextActivePage"
        >
          下一页
        </button>
      </nav>
    </section>

    <section class="admin-replies-section" aria-labelledby="hidden-replies-title">
      <div class="admin-replies-section__header">
        <h2 id="hidden-replies-title" class="admin-replies-section__title">隐藏回复</h2>
        <span class="admin-replies-section__count admin-replies-section__count--hidden">
          {{ hiddenReplies.length }}
        </span>
      </div>

      <AdminTable
        :columns="columns"
        :rows="pagedHiddenReplies"
        aria-label="隐藏回复列表"
        empty-text="暂无隐藏回复"
      >
        <template #cell-contentPreview="{ value }">
          <span class="admin-replies-content">{{ value }}</span>
        </template>

        <template #cell-createdAt="{ value }">
          <time class="admin-replies-time">
            <span>{{ splitDateTime(value).date }}</span>
            <span>{{ splitDateTime(value).time }}</span>
          </time>
        </template>

        <template #actions="{ row }">
          <button class="admin-table__action" type="button" @click="setReplyStatus(row, 1)">
            恢复
          </button>
        </template>
      </AdminTable>

      <nav class="admin-replies-pagination" aria-label="隐藏回复分页">
        <button type="button" :disabled="hiddenPage === 1" @click="previousHiddenPage">
          上一页
        </button>
        <span>{{ hiddenPage }} / {{ hiddenTotalPages }}</span>
        <button
          type="button"
          :disabled="hiddenPage === hiddenTotalPages"
          @click="nextHiddenPage"
        >
          下一页
        </button>
      </nav>
    </section>
  </section>
</template>
