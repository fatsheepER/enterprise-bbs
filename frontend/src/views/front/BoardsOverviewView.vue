<script setup>
import { onMounted, ref } from 'vue'

import { getBoards } from '@/api/boards'
import { getPosts } from '@/api/posts'
import BoardCard from '@/components/BoardCard.vue'
import OverviewTabs from '@/components/OverviewTabs.vue'

const boards = ref([])
const posts = ref([])

onMounted(async () => {
  const [boardList, postList] = await Promise.all([getBoards(), getPosts()])

  boards.value = boardList
  posts.value = postList
})

function latestPostsForBoard(boardId) {
  return posts.value.filter((post) => post.boardId === boardId).slice(0, 3)
}
</script>

<template>
  <OverviewTabs />

  <section class="board-list" aria-label="版块列表">
    <BoardCard
      v-for="board in boards"
      :key="board.id"
      :board="board"
      :posts="latestPostsForBoard(board.id)"
    />
  </section>
</template>
