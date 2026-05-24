import { createRouter, createWebHistory } from 'vue-router'

import BoardDetailView from '@/views/front/BoardDetailView.vue'
import BoardsOverviewView from '@/views/front/BoardsOverviewView.vue'
import CreatePostView from '@/views/front/CreatePostView.vue'
import PostDetailView from '@/views/front/PostDetailView.vue'
import PostsView from '@/views/front/PostsView.vue'
import LoginView from '@/views/front/LoginView.vue'
import PlaceholderView from '@/views/front/PlaceholderView.vue'
import ProfileView from '@/views/front/ProfileView.vue'
import ProfileEditView from '@/views/front/ProfileEditView.vue'
import RegisterView from '@/views/front/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }

    if (to.hash) {
      return {
        el: to.hash,
        top: 0,
      }
    }

    return { top: 0 }
  },
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
      path: '/create-post',
      name: 'create-post',
      component: CreatePostView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
    },
    {
      path: '/profile/edit',
      name: 'profile-edit',
      component: ProfileEditView,
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
