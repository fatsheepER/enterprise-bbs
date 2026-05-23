import { createRouter, createWebHistory } from 'vue-router'

import BoardsOverviewView from '../views/front/BoardsOverviewView.vue'
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
  ],
})

export default router
