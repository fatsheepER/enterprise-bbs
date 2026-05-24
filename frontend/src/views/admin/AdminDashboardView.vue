<script setup>
import { computed, onMounted, ref, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getAdminDashboardStats, getAdminPosts, getAdminReplies } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const stats = ref({
  userCount: 0,
  boardCount: 0,
  disabledBoardCount: 0,
  postCount: 0,
  hiddenPostCount: 0,
  replyCount: 0,
  hiddenReplyCount: 0,
  totalBoardCount: 0,
  totalPostCount: 0,
  totalReplyCount: 0,
})
const latestPost = ref(null)
const latestReply = ref(null)

watchEffect(() => {
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
})

onMounted(async () => {
  if (!authStore.isAdmin) {
    return
  }

  const [dashboardStats, postRows, replyRows] = await Promise.all([
    getAdminDashboardStats(),
    getAdminPosts({ status: 1 }),
    getAdminReplies({ status: 1 }),
  ])

  stats.value = dashboardStats
  latestPost.value = postRows[0] ?? null
  latestReply.value = replyRows[0] ?? null
})

const overviewRows = computed(() => [
  {
    title: '版块',
    description: '查看全部板块并停用不需要展示的板块。',
    totalLabel: '全部版块',
    totalValue: stats.value.totalBoardCount,
    flaggedLabel: '已停用',
    flaggedValue: stats.value.disabledBoardCount,
    to: '/admin/boards',
    action: '管理版块',
  },
  {
    title: '帖子',
    description: '检查全部帖子并隐藏不适合展示的帖子。',
    totalLabel: '全部帖子',
    totalValue: stats.value.totalPostCount,
    flaggedLabel: '已隐藏',
    flaggedValue: stats.value.hiddenPostCount,
    to: '/admin/posts',
    action: '管理帖子',
  },
  {
    title: '回复',
    description: '查看全部回复并隐藏不适合展示的回复。',
    totalLabel: '全部回复',
    totalValue: stats.value.totalReplyCount,
    flaggedLabel: '已隐藏',
    flaggedValue: stats.value.hiddenReplyCount,
    to: '/admin/replies',
    action: '管理回复',
  },
])

const siteStats = computed(() => [
  {
    label: '用户',
    value: stats.value.userCount,
  },
  {
    label: '版块',
    value: stats.value.boardCount,
  },
  {
    label: '帖子',
    value: stats.value.postCount,
  },
  {
    label: '回复',
    value: stats.value.replyCount,
  },
])

const latestRows = computed(() => [
  latestPost.value
    ? {
        type: '最新帖子',
        title: latestPost.value.contentPreview,
        meta: `${latestPost.value.title} · ${latestPost.value.boardName} · ${latestPost.value.authorName}`,
        time: latestPost.value.updatedAt,
        to: `/posts/${latestPost.value.id}`,
      }
    : null,
  latestReply.value
    ? {
        type: '最新回复',
        title: latestReply.value.contentPreview,
        meta: `${latestReply.value.postTitle || `帖子 #${latestReply.value.postId}`} · ${latestReply.value.authorName}`,
        time: latestReply.value.updatedAt,
        to: latestReply.value.href,
      }
    : null,
].filter(Boolean))
</script>

<template>
  <section v-if="authStore.isAdmin" class="admin-dashboard">
    <header class="admin-dashboard__header">
      <div>
        <h1 class="admin-dashboard__title">管理员控制台</h1>
      </div>
    </header>

    <section class="admin-dashboard__stats" aria-label="站点统计">
      <article class="admin-dashboard__panel admin-dashboard__panel--stats">
        <div class="admin-dashboard__panel-header">
          <h2 class="admin-dashboard__panel-title">站点统计</h2>
          <p class="admin-dashboard__panel-note">统计用户以及当前正常展示的版块、帖子和回复。</p>
        </div>

        <dl class="admin-site-stats">
          <div v-for="item in siteStats" :key="item.label" class="admin-site-stats__item">
            <dt>{{ item.label }}</dt>
            <dd>{{ item.value }}</dd>
          </div>
        </dl>
      </article>
    </section>

    <section class="admin-dashboard__latest" aria-label="最新内容">
      <article
        v-for="row in latestRows"
        :key="row.type"
        class="admin-latest-row"
      >
        <div class="admin-latest-row__main">
          <span class="admin-latest-row__type">{{ row.type }}</span>
          <h2 class="admin-latest-row__title">
            <RouterLink :to="row.to">{{ row.title }}</RouterLink>
          </h2>
          <p class="admin-latest-row__meta">{{ row.meta }}</p>
        </div>
        <time class="admin-latest-row__time">{{ row.time }}</time>
      </article>
    </section>

    <section class="admin-dashboard__overview" aria-label="主要管理入口">
      <article v-for="row in overviewRows" :key="row.title" class="admin-overview-row">
        <div class="admin-overview-row__main">
          <h2 class="admin-overview-row__title">{{ row.title }}</h2>
          <p class="admin-overview-row__description">{{ row.description }}</p>
        </div>

        <dl class="admin-overview-row__stats">
          <div class="admin-overview-row__stat">
            <dt>{{ row.totalLabel }}</dt>
            <dd>{{ row.totalValue }}</dd>
          </div>
          <div class="admin-overview-row__stat admin-overview-row__stat--attention">
            <dt>{{ row.flaggedLabel }}</dt>
            <dd>{{ row.flaggedValue }}</dd>
          </div>
        </dl>

        <RouterLink class="admin-overview-row__action" :to="row.to">
          {{ row.action }}
        </RouterLink>
      </article>
    </section>
  </section>
</template>
