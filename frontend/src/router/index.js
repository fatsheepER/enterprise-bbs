import { createRouter, createWebHistory } from 'vue-router'

import BoardDetailView from '../views/front/BoardDetailView.vue'
import BoardsOverviewView from '../views/front/BoardsOverviewView.vue'
import PostDetailView from '../views/front/PostDetailView.vue'
import PostsView from '../views/front/PostsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'boards-overview',
      component: BoardsOverviewView,
    },
    {
      path: '/posts',
      name: 'posts',
      component: PostsView,
    },
    {
      path: '/board/:id',
      name: 'board-detail',
      component: BoardDetailView,
    },
    {
      path: '/post/:id',
      name: 'post-detail',
      component: PostDetailView,
    },
  ],
})

export default router
