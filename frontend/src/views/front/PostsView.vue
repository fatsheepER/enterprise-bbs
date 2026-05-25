<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getPosts } from '@/api/posts'
import PostSortSelect from '@/components/PostSortSelect.vue'
import PostTable from '@/components/PostTable.vue'

const route = useRoute()
const router = useRouter()
const posts = ref([])
const validSorts = new Set(['latest', 'views', 'replies', 'title'])
const keyword = computed(() => String(route.query.keyword || '').trim())
const sort = computed(() => (validSorts.has(route.query.sort) ? route.query.sort : 'latest'))
const emptyText = computed(() =>
  keyword.value ? `没有找到标题包含“${keyword.value}”的帖子` : '暂无帖子',
)

async function loadPosts() {
  posts.value = await getPosts({
    keyword: keyword.value || undefined,
    sort: sort.value,
  })
}

function updateSort(nextSort) {
  const query = { ...route.query }

  if (nextSort === 'latest') {
    delete query.sort
  } else {
    query.sort = nextSort
  }

  router.push({ name: 'posts', query })
}

watch([keyword, sort], loadPosts, { immediate: true })
</script>

<template>
  <section class="posts-overview">
    <h1 class="overview-heading">总览</h1>

    <div class="posts-overview__toolbar">
      <nav class="overview-tabs posts-overview__tabs" aria-label="总览页面导航">
        <RouterLink class="overview-tabs__link" to="/">板块</RouterLink>
        <RouterLink class="overview-tabs__link" to="/posts">全部帖子</RouterLink>
      </nav>

      <PostSortSelect :model-value="sort" @update:model-value="updateSort" />
    </div>
  </section>

  <PostTable :posts="posts" aria-label="全部帖子列表" :empty-text="emptyText" />
</template>
