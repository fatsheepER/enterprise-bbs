import { createRouter, createWebHistory } from 'vue-router'

import AdminBoardsView from '@/views/admin/AdminBoardsView.vue'
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import AdminPostsView from '@/views/admin/AdminPostsView.vue'
import AdminRepliesView from '@/views/admin/AdminRepliesView.vue'
import BoardDetailView from '@/views/front/BoardDetailView.vue'
import BoardsOverviewView from '@/views/front/BoardsOverviewView.vue'
import CreatePostView from '@/views/front/CreatePostView.vue'
import PostDetailView from '@/views/front/PostDetailView.vue'
import PostsView from '@/views/front/PostsView.vue'
import LoginView from '@/views/front/LoginView.vue'
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
      component: AdminDashboardView,
    },
    {
      path: '/admin/boards',
      name: 'admin-boards',
      component: AdminBoardsView,
    },
    {
      path: '/admin/posts',
      name: 'admin-posts',
      component: AdminPostsView,
    },
    {
      path: '/admin/replies',
      name: 'admin-replies',
      component: AdminRepliesView,
    },
    {
      path: '/boards/:id',
      name: 'board-detail',
      component: BoardDetailView,
    },
    {
      path: '/posts/:id',
      name: 'post-detail',
      component: PostDetailView,
    },
  ],
})

export default router
