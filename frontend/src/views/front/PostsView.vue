<script setup>
import PostTable from '../../components/PostTable.vue'
import { visiblePosts } from '../../mock/forumViewModels'

const posts = visiblePosts()
</script>

<template>
  <section class="posts-overview">
    <h1 class="overview-heading">总览</h1>

    <div class="posts-overview__toolbar">
      <nav class="overview-tabs posts-overview__tabs" aria-label="总览页面导航">
        <RouterLink class="overview-tabs__link" to="/">板块</RouterLink>
        <RouterLink class="overview-tabs__link" to="/posts">全部帖子</RouterLink>
      </nav>

      <button class="posts-sort-button" type="button" aria-label="排序">
        最近回复
        <span aria-hidden="true">↓</span>
      </button>
    </div>
  </section>

  <PostTable :posts="posts" aria-label="全部帖子列表" />
</template>
