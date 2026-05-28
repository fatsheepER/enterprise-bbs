<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { deleteAdminPost, getAdminPosts, updateAdminPostStatus } from '@/api/admin'
import AdminTable from '@/components/AdminTable.vue'
import { useAuthStore } from '@/stores/auth'

const pageSize = 10

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const idQuery = ref('')
const keywordQuery = ref('')
const posts = ref([])
const activePage = ref(1)
const hiddenPage = ref(1)
const deletingPostId = ref(null)
let latestPostsRequestId = 0

const columns = [
  { key: 'id', label: 'ID', className: 'admin-posts-table__id', align: 'center' },
  { key: 'title', label: '标题', className: 'admin-posts-table__title' },
  { key: 'boardName', label: '版块', className: 'admin-posts-table__board' },
  { key: 'replyCount', label: '回复', className: 'admin-posts-table__count', align: 'center' },
  { key: 'latestReplyTime', label: '最新回复时间', className: 'admin-posts-table__time' },
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
  const value = String(idQuery.value).trim()

  return value ? Number(value) : null
})

const postFilters = computed(() => ({
  id: normalizedIdQuery.value,
  keyword: keywordQuery.value,
}))

async function loadPosts() {
  if (!authStore.isAdmin) {
    return
  }

  const requestId = ++latestPostsRequestId
  const { id, keyword } = postFilters.value

  const nextPosts = await getAdminPosts({
    id,
    keyword,
  })

  if (requestId === latestPostsRequestId) {
    posts.value = nextPosts
  }
}

onMounted(loadPosts)
watch([idQuery, keywordQuery], loadPosts)

const activePosts = computed(() => posts.value.filter((post) => post.status === 1))
const hiddenPosts = computed(() => posts.value.filter((post) => post.status === 0))

const activeTotalPages = computed(() => totalPages(activePosts.value))
const hiddenTotalPages = computed(() => totalPages(hiddenPosts.value))

const pagedActivePosts = computed(() => pageItems(activePosts.value, activePage.value))
const pagedHiddenPosts = computed(() => pageItems(hiddenPosts.value, hiddenPage.value))

function totalPages(rows) {
  return Math.max(1, Math.ceil(rows.length / pageSize))
}

function pageItems(rows, page) {
  const safePage = Math.min(Math.max(page, 1), totalPages(rows))
  const start = (safePage - 1) * pageSize

  return rows.slice(start, start + pageSize)
}

function splitDateTime(dateTime) {
  if (!dateTime) {
    return {
      date: '暂无回复',
      time: '',
    }
  }

  const [date, time = ''] = dateTime.split(' ')

  return {
    date,
    time,
  }
}

function isPostInHiddenBoard(post) {
  return post.boardStatus === 0
}

async function setPostStatus(post, status) {
  await updateAdminPostStatus(post.id, status)
  await loadPosts()
  activePage.value = Math.min(activePage.value, activeTotalPages.value)
  hiddenPage.value = Math.min(hiddenPage.value, hiddenTotalPages.value)
}

async function deleteHiddenPost(post) {
  const confirmed = window.confirm(
    `确定要永久删除帖子“${post.title}”吗？该帖子下所有回复也会一并删除。`,
  )

  if (!confirmed) {
    return
  }

  try {
    deletingPostId.value = post.id
    await deleteAdminPost(post.id)
    await loadPosts()
    activePage.value = Math.min(activePage.value, activeTotalPages.value)
    hiddenPage.value = Math.min(hiddenPage.value, hiddenTotalPages.value)
  } catch (error) {
    window.alert(error.message || '删除帖子失败')
  } finally {
    deletingPostId.value = null
  }
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
  <section v-if="authStore.isAdmin" class="admin-posts-page">
    <header class="admin-posts-page__header">
      <div>
        <RouterLink class="admin-posts-page__back" to="/admin">管理员控制台</RouterLink>
        <h1 class="admin-posts-page__title">帖子管理</h1>
      </div>
    </header>

    <form class="admin-posts-filter" @submit.prevent>
      <label class="admin-posts-filter__field">
        <span>ID</span>
        <input v-model="idQuery" type="number" min="1" placeholder="精确搜索" />
      </label>

      <label class="admin-posts-filter__field">
        <span>标题</span>
        <input v-model="keywordQuery" type="search" placeholder="按帖子标题搜索" />
      </label>
    </form>

    <section class="admin-posts-section" aria-labelledby="active-posts-title">
      <div class="admin-posts-section__header">
        <h2 id="active-posts-title" class="admin-posts-section__title">正常帖子</h2>
        <span class="admin-posts-section__count">{{ activePosts.length }}</span>
      </div>

      <AdminTable
        :columns="columns"
        :rows="pagedActivePosts"
        aria-label="正常帖子列表"
        empty-text="暂无正常帖子"
      >
        <template #cell-title="{ row, value }">
          <RouterLink
            class="admin-posts-title-link"
            :class="{ 'admin-posts-title-link--in-hidden-board': isPostInHiddenBoard(row) }"
            :to="{ name: 'post-detail', params: { id: row.id } }"
          >
            {{ value }}
          </RouterLink>
        </template>

        <template #cell-boardName="{ row, value }">
          <RouterLink
            class="board-badge admin-posts-board-badge"
            :to="{ name: 'board-detail', params: { id: row.boardId } }"
            :style="{ '--board-color': row.boardColorHex }"
          >
            {{ value }}
          </RouterLink>
        </template>

        <template #cell-latestReplyTime="{ value }">
          <time class="admin-posts-time">
            <span>{{ splitDateTime(value).date }}</span>
            <span v-if="splitDateTime(value).time">{{ splitDateTime(value).time }}</span>
          </time>
        </template>

        <template #actions="{ row }">
          <button
            class="admin-table__action admin-table__action--danger"
            type="button"
            @click="setPostStatus(row, 0)"
          >
            隐藏
          </button>
        </template>
      </AdminTable>

      <nav class="admin-posts-pagination" aria-label="正常帖子分页">
        <button type="button" :disabled="activePage === 1" @click="previousActivePage">
          上一页
        </button>
        <span>{{ activePage }} / {{ activeTotalPages }}</span>
        <button type="button" :disabled="activePage === activeTotalPages" @click="nextActivePage">
          下一页
        </button>
      </nav>
    </section>

    <section class="admin-posts-section" aria-labelledby="hidden-posts-title">
      <div class="admin-posts-section__header">
        <h2 id="hidden-posts-title" class="admin-posts-section__title">隐藏帖子</h2>
        <span class="admin-posts-section__count admin-posts-section__count--hidden">
          {{ hiddenPosts.length }}
        </span>
      </div>

      <AdminTable
        :columns="columns"
        :rows="pagedHiddenPosts"
        aria-label="隐藏帖子列表"
        empty-text="暂无隐藏帖子"
      >
        <template #cell-title="{ row, value }">
          <RouterLink
            class="admin-posts-title-link"
            :class="{ 'admin-posts-title-link--in-hidden-board': isPostInHiddenBoard(row) }"
            :to="{ name: 'post-detail', params: { id: row.id } }"
          >
            {{ value }}
          </RouterLink>
        </template>

        <template #cell-boardName="{ row, value }">
          <RouterLink
            class="board-badge admin-posts-board-badge"
            :to="{ name: 'board-detail', params: { id: row.boardId } }"
            :style="{ '--board-color': row.boardColorHex }"
          >
            {{ value }}
          </RouterLink>
        </template>

        <template #cell-latestReplyTime="{ value }">
          <time class="admin-posts-time">
            <span>{{ splitDateTime(value).date }}</span>
            <span v-if="splitDateTime(value).time">{{ splitDateTime(value).time }}</span>
          </time>
        </template>

        <template #actions="{ row }">
          <div class="admin-table__actions">
            <button class="admin-table__action" type="button" @click="setPostStatus(row, 1)">
              恢复
            </button>
            <button
              class="admin-table__action admin-table__action--danger"
              type="button"
              :disabled="deletingPostId === row.id"
              @click="deleteHiddenPost(row)"
            >
              {{ deletingPostId === row.id ? '删除中' : '删除' }}
            </button>
          </div>
        </template>
      </AdminTable>

      <nav class="admin-posts-pagination" aria-label="隐藏帖子分页">
        <button type="button" :disabled="hiddenPage === 1" @click="previousHiddenPage">
          上一页
        </button>
        <span>{{ hiddenPage }} / {{ hiddenTotalPages }}</span>
        <button type="button" :disabled="hiddenPage === hiddenTotalPages" @click="nextHiddenPage">
          下一页
        </button>
      </nav>
    </section>
  </section>
</template>
