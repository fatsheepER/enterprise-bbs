<script setup>
defineProps({
  posts: {
    type: Array,
    required: true,
  },
  ariaLabel: {
    type: String,
    default: '帖子列表',
  },
  emptyText: {
    type: String,
    default: '暂无帖子',
  },
})

function postPreview(post) {
  return post.contentPreview || post.content || ''
}

function postActivity(post) {
  return post.relativeTime || post.updatedAt
}
</script>

<template>
  <section class="posts-table-wrap" :aria-label="ariaLabel">
    <table class="posts-table">
      <thead>
        <tr>
          <th class="posts-table__heading posts-table__heading--post" scope="col">帖子</th>
          <th class="posts-table__heading" scope="col">回复数</th>
          <th class="posts-table__heading" scope="col">浏览数</th>
          <th class="posts-table__heading" scope="col">最近回复</th>
        </tr>
      </thead>

      <tbody>
        <template v-if="posts.length === 0">
          <tr>
            <td class="posts-table__empty" colspan="4">{{ emptyText }}</td>
          </tr>
        </template>

        <template v-else>
          <tr v-for="post in posts" :key="post.id" class="posts-table__row">
            <td class="posts-table__post-cell">
              <h2 class="posts-table__title">
                <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }">
                  {{ post.title }}
                </RouterLink>
              </h2>
              <p class="posts-table__excerpt">{{ postPreview(post) }}</p>
              <RouterLink
                class="board-badge posts-table__badge"
                :to="{ name: 'board-detail', params: { id: post.boardId } }"
                :style="{ '--board-color': post.boardColorHex }"
              >
                {{ post.boardName }}
              </RouterLink>
            </td>

            <td class="posts-table__stat" aria-label="回复数">{{ post.replyCount }}</td>
            <td class="posts-table__stat" aria-label="浏览数">{{ post.viewCount }}</td>
            <td class="posts-table__stat" aria-label="最近回复">{{ postActivity(post) }}</td>
          </tr>
        </template>
      </tbody>
    </table>
  </section>
</template>
