<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { getBoard } from '@/api/boards'
import { getPosts } from '@/api/posts'
import boardIcon from '@/assets/board-placeholder.svg'
import PostTable from '@/components/PostTable.vue'

const route = useRoute()
const board = ref(null)
const posts = ref([])

const boardId = computed(() => Number(route.params.id))

async function loadBoard() {
  try {
    const [boardDetail, postList] = await Promise.all([
      getBoard(boardId.value),
      getPosts({ boardId: boardId.value }),
    ])

    board.value = boardDetail
    posts.value = postList
  } catch {
    board.value = null
    posts.value = []
  }
}

onMounted(loadBoard)
watch(boardId, loadBoard)
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

        <button class="posts-sort-button" type="button" aria-label="排序">
          最近回复
          <span aria-hidden="true">↓</span>
        </button>
      </div>
    </section>

    <PostTable :posts="posts" :aria-label="`${board.name}帖子列表`" empty-text="这个版块暂无帖子" />
  </section>

  <section v-else class="board-detail-empty">
    <h1 class="board-detail-empty__title">版块不存在</h1>
    <p class="board-detail-empty__message">这个版块可能已停用或被删除。</p>
    <RouterLink class="overview-tabs__link board-detail-empty__link" to="/">返回版块总览</RouterLink>
  </section>
</template>
