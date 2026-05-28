<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getPosts } from '@/api/posts'
import { deleteReply } from '@/api/replies'
import PostTable from '@/components/PostTable.vue'
import eyeIcon from '@/assets/eye.svg'
import gearIcon from '@/assets/gear.svg'
import personPlaceholder from '@/assets/person-placeholder.svg'
import { getUserProfileById, getUserReplies, getUserRepliesById } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const activeTab = ref('posts')
const profileUser = ref(null)
const userPosts = ref([])
const userReplyList = ref([])
const profileLoadError = ref('')
const confirmingDeleteReplyIds = ref(new Set())
const deletingReplyIds = ref(new Set())
const deleteReplyErrors = ref({})
const deleteReplyTimers = new Map()
let loadSequence = 0

const isCurrentProfileRoute = computed(() => route.name === 'profile')
const targetUserId = computed(() => {
  if (isCurrentProfileRoute.value) {
    return authStore.currentUser?.id ?? null
  }

  const routeUserId = Number(route.params.id)

  return Number.isFinite(routeUserId) && routeUserId > 0 ? routeUserId : null
})
const isOwnProfile = computed(
  () => Boolean(authStore.currentUser?.id) && profileUser.value?.id === authStore.currentUser.id,
)
const displayName = computed(
  () => profileUser.value?.nickname || profileUser.value?.username || '用户',
)
const displaySubject = computed(() => (isOwnProfile.value ? '你' : 'TA'))
const avatarSrc = computed(() => profileUser.value?.avatar || personPlaceholder)
const isProfileAdmin = computed(() => profileUser.value?.role === 'ADMIN')

watch([targetUserId, isCurrentProfileRoute], loadProfile, { immediate: true })
onBeforeUnmount(clearAllReplyDeleteConfirmations)

async function loadProfile() {
  const sequence = ++loadSequence

  clearAllReplyDeleteConfirmations()
  deleteReplyErrors.value = {}
  userPosts.value = []
  userReplyList.value = []
  profileLoadError.value = ''

  if (isCurrentProfileRoute.value && !authStore.isLoggedIn) {
    profileUser.value = null
    router.replace({
      path: '/login',
      query: { redirect: route.fullPath },
    })
    return
  }

  if (!targetUserId.value) {
    profileUser.value = null
    profileLoadError.value = '用户不存在'
    return
  }

  try {
    const profilePromise = isOwnTargetUser()
      ? Promise.resolve(authStore.currentUser)
      : getUserProfileById(targetUserId.value)
    const repliesPromise = isOwnTargetUser()
      ? getUserReplies()
      : getUserRepliesById(targetUserId.value)
    const [loadedUser, postList, replyList] = await Promise.all([
      profilePromise,
      getPosts({ userId: targetUserId.value }),
      repliesPromise,
    ])

    if (sequence !== loadSequence) {
      return
    }

    profileUser.value = loadedUser
    userPosts.value = postList
    userReplyList.value = replyList
  } catch (error) {
    if (sequence !== loadSequence) {
      return
    }

    profileUser.value = null
    userPosts.value = []
    userReplyList.value = []
    profileLoadError.value = error.message || '用户资料加载失败'
  }
}

function isOwnTargetUser() {
  return Boolean(authStore.currentUser?.id) && targetUserId.value === authStore.currentUser.id
}

function formatDisplayDate(dateTime) {
  return dateTime?.slice(0, 10) ?? ''
}

function hasReplyDeleteConfirmation(replyId) {
  return confirmingDeleteReplyIds.value.has(replyId)
}

function isDeletingReply(replyId) {
  return deletingReplyIds.value.has(replyId)
}

function clearReplyDeleteConfirmation(replyId) {
  const timer = deleteReplyTimers.get(replyId)

  if (timer !== undefined) {
    window.clearTimeout(timer)
    deleteReplyTimers.delete(replyId)
  }

  if (confirmingDeleteReplyIds.value.has(replyId)) {
    const nextIds = new Set(confirmingDeleteReplyIds.value)

    nextIds.delete(replyId)
    confirmingDeleteReplyIds.value = nextIds
  }
}

function clearAllReplyDeleteConfirmations() {
  deleteReplyTimers.forEach((timer) => window.clearTimeout(timer))
  deleteReplyTimers.clear()
  confirmingDeleteReplyIds.value = new Set()
}

function clearReplyDeleteError(replyId) {
  if (!deleteReplyErrors.value[replyId]) {
    return
  }

  const nextErrors = { ...deleteReplyErrors.value }

  delete nextErrors[replyId]
  deleteReplyErrors.value = nextErrors
}

async function deleteOwnReply(replyId) {
  if (!isOwnProfile.value || isDeletingReply(replyId)) {
    return
  }

  clearReplyDeleteError(replyId)

  if (!hasReplyDeleteConfirmation(replyId)) {
    const nextIds = new Set(confirmingDeleteReplyIds.value)

    nextIds.add(replyId)
    confirmingDeleteReplyIds.value = nextIds
    deleteReplyTimers.set(
      replyId,
      window.setTimeout(() => clearReplyDeleteConfirmation(replyId), 5000),
    )
    return
  }

  clearReplyDeleteConfirmation(replyId)
  deletingReplyIds.value = new Set(deletingReplyIds.value).add(replyId)

  try {
    await deleteReply(replyId)
    userReplyList.value = userReplyList.value.filter((reply) => reply.id !== replyId)
  } catch (error) {
    deleteReplyErrors.value = {
      ...deleteReplyErrors.value,
      [replyId]: error.message || '删除回复失败',
    }
  } finally {
    const nextIds = new Set(deletingReplyIds.value)

    nextIds.delete(replyId)
    deletingReplyIds.value = nextIds
  }
}
</script>

<template>
  <section v-if="profileUser" class="profile-page">
    <article class="profile-hero">
      <RouterLink
        v-if="isOwnProfile"
        class="profile-hero__settings"
        to="/profile/edit"
        aria-label="设置"
      >
        <img class="profile-hero__settings-icon" :src="gearIcon" alt="" />
      </RouterLink>

      <div class="profile-hero__avatar" aria-hidden="true">
        <img class="profile-hero__avatar-image" :src="avatarSrc" alt="" />
      </div>

      <div class="profile-hero__body">
        <div class="profile-hero__title-row">
          <h1 class="profile-hero__name">{{ displayName }}</h1>
          <span v-if="isProfileAdmin" class="profile-hero__badge">管理员</span>
        </div>

        <p class="profile-hero__bio">
          {{ profileUser.bio || '这个用户暂未填写个人简介。' }}
        </p>

        <dl class="profile-hero__meta">
          <div class="profile-hero__meta-item">
            <dt>用户名</dt>
            <dd>{{ profileUser.username }}</dd>
          </div>
          <div class="profile-hero__meta-item">
            <dt>电子邮箱</dt>
            <dd>{{ profileUser.email || '未填写' }}</dd>
          </div>
        </dl>
      </div>
    </article>

    <section class="profile-activity">
      <div class="posts-overview__toolbar">
        <nav class="overview-tabs posts-overview__tabs" aria-label="个人活动导航">
          <button
            class="overview-tabs__link profile-activity__tab"
            :class="{ 'profile-activity__tab--active': activeTab === 'posts' }"
            type="button"
            @click="activeTab = 'posts'"
          >
            发帖
          </button>
          <button
            class="overview-tabs__link profile-activity__tab"
            :class="{ 'profile-activity__tab--active': activeTab === 'replies' }"
            type="button"
            @click="activeTab = 'replies'"
          >
            回复
          </button>
        </nav>
      </div>

      <PostTable
        v-if="activeTab === 'posts'"
        :posts="userPosts"
        :aria-label="`${displayName}的发帖列表`"
        :empty-text="`${displaySubject}还没有发表过帖子`"
      />

      <section v-else class="profile-replies" :aria-label="`${displayName}的回复列表`">
        <template v-if="userReplyList.length === 0">
          <article class="profile-empty">
            <h2 class="profile-empty__title">暂无回复</h2>
            <p class="profile-empty__message">{{ displaySubject }}发表的回复会显示在这里。</p>
          </article>
        </template>

        <template v-else>
          <article
            v-for="reply in userReplyList"
            :key="reply.id"
            class="post-block post-block--reply profile-reply"
          >
            <div class="post-block__header">
              <div class="post-block__avatar" aria-hidden="true">
                <img
                  class="post-block__avatar-image"
                  :src="reply.authorAvatar || personPlaceholder"
                  alt=""
                />
              </div>

              <div class="post-block__meta-row">
                <div class="post-block__author-group">
                  <strong class="post-block__author">{{ reply.authorName }}</strong>
                  <RouterLink
                    class="board-badge"
                    :to="{ name: 'board-detail', params: { id: reply.boardId } }"
                    :style="{ '--board-color': reply.boardColorHex }"
                  >
                    {{ reply.boardName }}
                  </RouterLink>
                </div>

                <div class="post-block__time-group">
                  <span class="post-block__reply-id">#{{ reply.id }}</span>
                  <span class="post-block__date">{{ formatDisplayDate(reply.createdAt) }}</span>
                </div>
              </div>
            </div>

            <div class="post-block__main">
              <aside class="parent-reply profile-reply__reference">
                <h2 class="profile-reply__post-title">
                  <RouterLink :to="{ name: 'post-detail', params: { id: reply.postId } }">
                    {{ reply.postTitle }}
                  </RouterLink>
                </h2>
                <p v-if="reply.reference.type === 'reply'" class="parent-reply__meta">
                  引用回复
                  <RouterLink :to="reply.reference.href">{{
                    reply.reference.authorName
                  }}</RouterLink>
                </p>
                <p class="parent-reply__content">{{ reply.reference.contentPreview }}</p>
              </aside>

              <p class="post-block__content">{{ reply.content }}</p>

              <div class="post-block__actions">
                <span
                  v-if="deleteReplyErrors[reply.id]"
                  class="profile-reply__delete-error"
                  role="alert"
                >
                  {{ deleteReplyErrors[reply.id] }}
                </span>
                <button
                  v-if="isOwnProfile"
                  class="profile-reply__delete-button"
                  :class="{
                    'profile-reply__delete-button--confirm':
                      hasReplyDeleteConfirmation(reply.id),
                    'profile-reply__delete-button--deleting': isDeletingReply(reply.id),
                  }"
                  type="button"
                  :disabled="isDeletingReply(reply.id)"
                  @click="deleteOwnReply(reply.id)"
                >
                  {{
                    isDeletingReply(reply.id)
                      ? '删除中...'
                      : hasReplyDeleteConfirmation(reply.id)
                        ? '确定'
                        : '删除'
                  }}
                </button>
                <RouterLink
                  class="reply-button profile-reply__view-button"
                  :to="`/posts/${reply.postId}#reply-${reply.id}`"
                  :aria-label="`查看回复 #${reply.id}`"
                >
                  <img class="reply-button__icon" :src="eyeIcon" alt="" />
                </RouterLink>
              </div>
            </div>
          </article>
        </template>
      </section>
    </section>
  </section>

  <section v-else-if="profileLoadError" class="profile-page">
    <article class="profile-empty">
      <h1 class="profile-empty__title">无法显示用户资料</h1>
      <p class="profile-empty__message">{{ profileLoadError }}</p>
    </article>
  </section>
</template>
