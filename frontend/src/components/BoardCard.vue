<script setup>
defineProps({
  board: {
    type: Object,
    required: true,
  },
  posts: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <article class="board-card" :style="{ '--board-color': board.colorHex }">
    <div class="board-card__content">
      <div class="board-card__title-row">
        <h2 class="board-card__title">
          <RouterLink
            class="board-card__title-link"
            :to="{ name: 'board-detail', params: { id: board.id } }"
          >
            {{ board.name }}
          </RouterLink>
        </h2>
      </div>

      <p class="board-card__description">{{ board.description }}</p>
    </div>

    <div class="board-card__posts" aria-label="最新帖子">
      <div v-for="post in posts" :key="post.id" class="board-card__post">
        <p class="board-card__post-title">
          <RouterLink
            class="board-card__post-link"
            :to="{ name: 'post-detail', params: { id: post.id } }"
          >
            {{ post.title }}
          </RouterLink>
        </p>
        <span class="board-card__post-time">{{ post.relativeTime }}</span>
      </div>
    </div>
  </article>
</template>
