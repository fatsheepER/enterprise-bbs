<script setup>
import OverviewTabs from '../../components/OverviewTabs.vue'
import PostListItem from '../../components/PostListItem.vue'
import { visiblePosts } from '../../mock/forumViewModels'

const posts = visiblePosts()
</script>

<template>
  <OverviewTabs />

  <section class="post-list" aria-label="全部帖子列表">
    <PostListItem v-for="post in posts" :key="post.id" :post="post" />
  </section>
</template>
