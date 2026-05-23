<script setup>
import BoardCard from '../../components/BoardCard.vue'
import OverviewTabs from '../../components/OverviewTabs.vue'
import { boards, posts } from '../../mock/forum'

function latestPostsForBoard(boardId) {
  return posts.filter((post) => post.boardId === boardId).slice(0, 3)
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
