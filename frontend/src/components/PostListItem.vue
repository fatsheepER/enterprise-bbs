<script setup>
defineProps({
  post: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <article class="post-list-item" :style="{ '--board-color': post.boardColorHex }">
    <span class="post-list-item__color" aria-hidden="true"></span>

    <div>
      <span class="post-list-item__board">{{ post.boardName }}</span>
      <h2 class="post-list-item__title">{{ post.title }}</h2>
      <p class="post-list-item__meta">
        {{ post.authorName }} · {{ post.replyCount }} 条回复 · {{ post.viewCount }} 次浏览
      </p>
    </div>

    <div class="post-list-item__stats" aria-label="帖子统计">
      <span class="post-list-item__time">{{ post.relativeTime }}</span>
      <span>{{ post.updatedAt }}</span>
    </div>
  </article>
</template>
