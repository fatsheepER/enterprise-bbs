import { createRouter, createWebHistory } from 'vue-router'

import BoardDetailView from '../views/front/BoardDetailView.vue'
import BoardsOverviewView from '../views/front/BoardsOverviewView.vue'
import PostDetailView from '../views/front/PostDetailView.vue'
import PostsView from '../views/front/PostsView.vue'
import LoginView from '../views/LoginView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'

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
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: PlaceholderView,
      props: {
        title: '创建账户',
        description: '注册表单将在下一步接入 mock 用户数据。',
      },
    },
    {
      path: '/profile',
      name: 'profile',
      component: PlaceholderView,
      props: {
        title: '个人主页',
        description: '这里将展示当前用户资料和发帖记录。',
      },
    },
    {
      path: '/admin',
      name: 'admin',
      component: PlaceholderView,
      props: {
        title: '控制台',
        description: '管理员版块和帖子管理入口将在后续实现。',
      },
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
