<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getBoard } from '@/api/boards'
import { getPosts } from '@/api/posts'
import boardIcon from '@/assets/board-placeholder.svg'
import PostSortSelect from '@/components/PostSortSelect.vue'
import PostTable from '@/components/PostTable.vue'

const route = useRoute()
const router = useRouter()
const board = ref(null)
const posts = ref([])
const validSorts = new Set(['latest', 'views', 'replies', 'title'])

const boardId = computed(() => Number(route.params.id))
const sort = computed(() => (validSorts.has(route.query.sort) ? route.query.sort : 'latest'))

async function loadBoard() {
  try {
    const [boardDetail, postList] = await Promise.all([
      getBoard(boardId.value),
      getPosts({ boardId: boardId.value, sort: sort.value }),
    ])

    board.value = boardDetail
    posts.value = postList
  } catch {
    board.value = null
    posts.value = []
  }
}

function updateSort(nextSort) {
  const query = { ...route.query }

  if (nextSort === 'latest') {
    delete query.sort
  } else {
    query.sort = nextSort
  }

  router.push({
    name: 'board-detail',
    params: { id: route.params.id },
    query,
  })
}

watch([boardId, sort], loadBoard, { immediate: true })
</script>

<template>
  <section v-if="board" class="board-detail">
    <article class="board-hero" :style="{ '--board-color': board.colorHex }">
      <div class="board-hero__icon" aria-hidden="true">
        <img class="board-hero__icon-image" :src="boardIcon" alt="" />
      </div>

      <div class="board-hero__copy">
        <h1 class="board-hero__title">{{ board.name }}</h1>
        <p class="board-hero__description">{{ board.description }}</p>
      </div>
    </article>

    <section class="posts-overview board-detail__posts-header">
      <div class="posts-overview__toolbar">
        <nav class="overview-tabs posts-overview__tabs" :aria-label="`${board.name} 帖子导航`">
          <RouterLink
            class="overview-tabs__link"
            :to="{ name: 'board-detail', params: { id: board.id } }"
          >
            全部帖子
          </RouterLink>
        </nav>

        <PostSortSelect :model-value="sort" @update:model-value="updateSort" />
      </div>
    </section>

    <PostTable :posts="posts" :aria-label="`${board.name}帖子列表`" empty-text="这个版块暂无帖子" />
  </section>

  <section v-else class="board-detail-empty">
    <h1 class="board-detail-empty__title">版块不存在</h1>
    <p class="board-detail-empty__message">这个版块可能已停用或被删除。</p>
    <RouterLink class="overview-tabs__link board-detail-empty__link" to="/"
      >返回版块总览</RouterLink
    >
  </section>
</template>
