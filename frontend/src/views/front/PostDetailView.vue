<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { updateAdminPostStatus, updateAdminReplyStatus } from '@/api/admin'
import { deletePost, getPost } from '@/api/posts'
import { createReply, deleteReply, getPostReplies } from '@/api/replies'
import arrowUpIcon from '@/assets/arrowshape.up.svg'
import messageIcon from '@/assets/message.svg'
import personPlaceholder from '@/assets/person-placeholder.svg'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const postId = computed(() => Number(route.params.id))
const post = ref(null)
const replies = ref([])
const draft = ref('')
const replyTarget = ref(null)
const submitError = ref('')
const hideConfirmationActive = ref(false)
const hidingPost = ref(false)
const hidePostError = ref('')
const deleteConfirmationActive = ref(false)
const deletingPost = ref(false)
const deletePostError = ref('')
const confirmingReplyIds = ref(new Set())
const hidingReplyIds = ref(new Set())
const hideReplyErrors = ref({})
const confirmingDeleteReplyIds = ref(new Set())
const deletingReplyIds = ref(new Set())
const deleteReplyErrors = ref({})
let hideConfirmationTimer = null
let deleteConfirmationTimer = null
const hideReplyTimers = new Map()
const deleteReplyTimers = new Map()

function formatDisplayDate(dateTime) {
  return dateTime?.slice(0, 10) ?? ''
}

function formatDisplayTime(dateTime) {
  return dateTime?.slice(11, 16) ?? ''
}

function avatarSrc(avatar) {
  return avatar || personPlaceholder
}

function previewText(content = '', maxLength = 72) {
  const normalized = content.replace(/\s+/g, ' ').trim()

  return normalized.length > maxLength ? `${normalized.slice(0, maxLength)}...` : normalized
}

async function loadPostDetail() {
  clearHideConfirmation()
  clearDeleteConfirmation()
  clearAllReplyHideConfirmations()
  clearAllReplyDeleteConfirmations()
  hidePostError.value = ''
  deletePostError.value = ''
  hideReplyErrors.value = {}
  deleteReplyErrors.value = {}

  try {
    const [postDetail, replyList] = await Promise.all([
      getPost(postId.value),
      getPostReplies(postId.value),
    ])

    post.value = postDetail
    replies.value = replyList
  } catch {
    post.value = null
    replies.value = []
  }
}

watch(postId, loadPostDetail, { immediate: true })
onBeforeUnmount(clearHideConfirmation)
onBeforeUnmount(clearDeleteConfirmation)
onBeforeUnmount(clearAllReplyHideConfirmations)
onBeforeUnmount(clearAllReplyDeleteConfirmations)

const visibleReplyCount = computed(() => replies.value.length)
const replyPlaceholder = computed(() => `回复： ${replyTarget.value?.contentPreview ?? ''}`)
const canSubmit = computed(() => draft.value.trim().length > 0)
const canDeletePost = computed(
  () => !authStore.isAdmin && post.value?.userId === authStore.currentUser?.id,
)

function canDeleteReply(reply) {
  return !authStore.isAdmin && reply.userId === authStore.currentUser?.id
}

function requireLogin() {
  if (authStore.isLoggedIn) {
    return true
  }

  router.push({
    path: '/login',
    query: { redirect: route.fullPath },
  })

  return false
}

function openReplyComposer(target) {
  if (!requireLogin()) {
    return
  }

  replyTarget.value = {
    id: target.id,
    type: target.type,
    authorName: target.authorName,
    contentPreview: previewText(target.content),
  }
  draft.value = ''
  submitError.value = ''
}

function closeReplyComposer() {
  replyTarget.value = null
  draft.value = ''
  submitError.value = ''
}

function openPostReplyComposer() {
  if (!post.value) {
    return
  }

  openReplyComposer({
    id: post.value.id,
    type: 'post',
    authorName: post.value.authorName,
    content: post.value.content,
  })
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function scrollToReply(replyId) {
  document.getElementById(`reply-${replyId}`)?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
}

function clearHideConfirmation() {
  if (hideConfirmationTimer !== null) {
    window.clearTimeout(hideConfirmationTimer)
    hideConfirmationTimer = null
  }

  hideConfirmationActive.value = false
}

async function hidePost() {
  if (!post.value || hidingPost.value) {
    return
  }

  hidePostError.value = ''

  if (!hideConfirmationActive.value) {
    hideConfirmationActive.value = true
    hideConfirmationTimer = window.setTimeout(clearHideConfirmation, 5000)
    return
  }

  clearHideConfirmation()
  hidingPost.value = true

  try {
    const boardId = post.value.boardId

    await updateAdminPostStatus(post.value.id, 0)
    await router.push({ name: 'board-detail', params: { id: boardId } })
  } catch (error) {
    hidePostError.value = error.message || '隐藏帖子失败'
  } finally {
    hidingPost.value = false
  }
}

function clearDeleteConfirmation() {
  if (deleteConfirmationTimer !== null) {
    window.clearTimeout(deleteConfirmationTimer)
    deleteConfirmationTimer = null
  }

  deleteConfirmationActive.value = false
}

async function deleteOwnPost() {
  if (!post.value || deletingPost.value) {
    return
  }

  deletePostError.value = ''

  if (!deleteConfirmationActive.value) {
    deleteConfirmationActive.value = true
    deleteConfirmationTimer = window.setTimeout(clearDeleteConfirmation, 5000)
    return
  }

  clearDeleteConfirmation()
  deletingPost.value = true

  try {
    const boardId = post.value.boardId

    await deletePost(post.value.id)
    await router.push({ name: 'board-detail', params: { id: boardId } })
  } catch (error) {
    deletePostError.value = error.message || '删除帖子失败'
  } finally {
    deletingPost.value = false
  }
}

function hasReplyHideConfirmation(replyId) {
  return confirmingReplyIds.value.has(replyId)
}

function isHidingReply(replyId) {
  return hidingReplyIds.value.has(replyId)
}

function clearReplyHideConfirmation(replyId) {
  const timer = hideReplyTimers.get(replyId)

  if (timer !== undefined) {
    window.clearTimeout(timer)
    hideReplyTimers.delete(replyId)
  }

  if (confirmingReplyIds.value.has(replyId)) {
    const nextIds = new Set(confirmingReplyIds.value)

    nextIds.delete(replyId)
    confirmingReplyIds.value = nextIds
  }
}

function clearAllReplyHideConfirmations() {
  hideReplyTimers.forEach((timer) => window.clearTimeout(timer))
  hideReplyTimers.clear()
  confirmingReplyIds.value = new Set()
}

function clearReplyHideError(replyId) {
  if (!hideReplyErrors.value[replyId]) {
    return
  }

  const nextErrors = { ...hideReplyErrors.value }

  delete nextErrors[replyId]
  hideReplyErrors.value = nextErrors
}

function removeReplyFromList(replyId) {
  replies.value = replies.value
    .filter((reply) => reply.id !== replyId)
    .map((reply) => {
      if (reply.parentReply?.id !== replyId) {
        return reply
      }

      return {
        ...reply,
        parentReply: {
          ...reply.parentReply,
          authorName: '已移除',
          contentPreview: '该评论已移除',
        },
      }
    })

  if (replyTarget.value?.type === 'reply' && replyTarget.value.id === replyId) {
    closeReplyComposer()
  }
}

async function hideReply(replyId) {
  if (isHidingReply(replyId)) {
    return
  }

  clearReplyHideError(replyId)

  if (!hasReplyHideConfirmation(replyId)) {
    const nextIds = new Set(confirmingReplyIds.value)

    nextIds.add(replyId)
    confirmingReplyIds.value = nextIds
    hideReplyTimers.set(
      replyId,
      window.setTimeout(() => clearReplyHideConfirmation(replyId), 5000),
    )
    return
  }

  clearReplyHideConfirmation(replyId)
  hidingReplyIds.value = new Set(hidingReplyIds.value).add(replyId)

  try {
    await updateAdminReplyStatus(replyId, 0)
    removeReplyFromList(replyId)
  } catch (error) {
    hideReplyErrors.value = {
      ...hideReplyErrors.value,
      [replyId]: error.message || '隐藏回复失败',
    }
  } finally {
    const nextIds = new Set(hidingReplyIds.value)

    nextIds.delete(replyId)
    hidingReplyIds.value = nextIds
  }
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
  if (isDeletingReply(replyId)) {
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
    removeReplyFromList(replyId)
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

async function submitReply() {
  if (!canSubmit.value || !post.value || !replyTarget.value) {
    return
  }

  if (!requireLogin()) {
    return
  }

  try {
    const parentReplyId = replyTarget.value.type === 'reply' ? replyTarget.value.id : null
    const newReply = await createReply(post.value.id, {
      parentReplyId,
      content: draft.value.trim(),
    })

    replies.value = [...replies.value, newReply]
    closeReplyComposer()
  } catch (error) {
    submitError.value = error.message || '回复失败'
  }
}
</script>

<template>
  <section v-if="post" class="post-detail">
    <header class="post-detail__header">
      <h1 class="post-detail__title">{{ post.title }}</h1>
      <RouterLink
        class="board-badge"
        :to="{ name: 'board-detail', params: { id: post.boardId } }"
        :style="{ '--board-color': post.boardColorHex }"
      >
        {{ post.boardName }}
      </RouterLink>
    </header>

    <article class="post-block post-block--main">
      <div class="post-block__header">
        <RouterLink
          class="post-block__avatar"
          :to="{ name: 'profile-detail', params: { id: post.userId } }"
          :aria-label="`查看 ${post.authorName} 的个人主页`"
        >
          <img class="post-block__avatar-image" :src="avatarSrc(post.authorAvatar)" alt="" />
        </RouterLink>

        <div class="post-block__meta-row">
          <div class="post-block__author-group">
            <strong class="post-block__author">{{ post.authorName }}</strong>
            <span v-if="post.authorRole === 'ADMIN'" class="post-block__admin-badge"> 管理员 </span>
          </div>

          <dl class="post-block__stats" aria-label="帖子数据">
            <div class="post-block__timestamp">
              <span class="post-block__date">{{ formatDisplayDate(post.createdAt) }}</span>
              <span class="post-block__time">{{ formatDisplayTime(post.createdAt) }}</span>
            </div>
            <div class="post-block__stat">
              <dt>回复</dt>
              <dd>{{ visibleReplyCount }}</dd>
            </div>
            <div class="post-block__stat">
              <dt>浏览</dt>
              <dd>{{ post.viewCount }}</dd>
            </div>
          </dl>
        </div>
      </div>

      <div class="post-block__main">
        <p class="post-block__content">{{ post.content }}</p>

        <div class="post-block__actions">
          <button
            class="reply-button"
            type="button"
            aria-label="回复帖子"
            @click="openPostReplyComposer"
          >
            <img class="reply-button__icon" :src="messageIcon" alt="" />
          </button>
        </div>
      </div>
    </article>

    <section class="reply-list" aria-label="回复列表">
      <article
        v-for="reply in replies"
        :id="`reply-${reply.id}`"
        :key="reply.id"
        class="post-block post-block--reply"
      >
        <div class="post-block__header">
          <RouterLink
            class="post-block__avatar"
            :to="{ name: 'profile-detail', params: { id: reply.userId } }"
            :aria-label="`查看 ${reply.authorName} 的个人主页`"
          >
            <img class="post-block__avatar-image" :src="avatarSrc(reply.authorAvatar)" alt="" />
          </RouterLink>

          <div class="post-block__meta-row">
            <div class="post-block__author-group">
              <strong class="post-block__author">{{ reply.authorName }}</strong>
              <span v-if="reply.authorRole === 'ADMIN'" class="post-block__admin-badge">
                管理员
              </span>
              <span
                v-if="reply.userId === post.userId"
                class="post-block__op-badge"
                aria-label="原帖作者"
              >
                OP
              </span>
            </div>

            <div class="post-block__time-group">
              <div class="post-block__timestamp">
                <span class="post-block__date">{{ formatDisplayDate(reply.createdAt) }}</span>
                <span class="post-block__time">{{ formatDisplayTime(reply.createdAt) }}</span>
              </div>
              <span class="post-block__reply-id">#{{ reply.id }}</span>
            </div>
          </div>
        </div>

        <div class="post-block__main">
          <aside v-if="reply.parentReply" class="parent-reply">
            <p class="parent-reply__meta">
              Written by
              <span>{{ reply.parentReply.authorName }}</span>
              in
              <a
                :href="`#reply-${reply.parentReply.id}`"
                @click.prevent="scrollToReply(reply.parentReply.id)"
              >
                #{{ reply.parentReply.id }}
              </a>
            </p>
            <p class="parent-reply__content">{{ reply.parentReply.contentPreview }}</p>
          </aside>

          <p class="post-block__content">{{ reply.content }}</p>

          <div class="post-block__actions">
            <span
              v-if="hideReplyErrors[reply.id]"
              class="post-detail__reply-moderation-error"
              role="alert"
            >
              {{ hideReplyErrors[reply.id] }}
            </span>
            <span
              v-if="deleteReplyErrors[reply.id]"
              class="post-detail__reply-moderation-error"
              role="alert"
            >
              {{ deleteReplyErrors[reply.id] }}
            </span>
            <button
              v-if="authStore.isAdmin"
              class="post-detail__reply-moderation-button"
              :class="{
                'post-detail__reply-moderation-button--confirm': hasReplyHideConfirmation(reply.id),
              }"
              type="button"
              :disabled="isHidingReply(reply.id)"
              @click="hideReply(reply.id)"
            >
              {{
                isHidingReply(reply.id)
                  ? '隐藏中...'
                  : hasReplyHideConfirmation(reply.id)
                    ? '确定'
                    : '隐藏'
              }}
            </button>
            <button
              v-if="canDeleteReply(reply)"
              class="post-detail__reply-moderation-button post-detail__reply-moderation-button--delete"
              :class="{
                'post-detail__reply-moderation-button--confirm':
                  hasReplyDeleteConfirmation(reply.id),
                'post-detail__reply-moderation-button--deleting': isDeletingReply(reply.id),
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
            <button
              class="reply-button"
              type="button"
              :aria-label="`回复 #${reply.id}`"
              @click="
                openReplyComposer({
                  id: reply.id,
                  type: 'reply',
                  authorName: reply.authorName,
                  content: reply.content,
                })
              "
            >
              <img class="reply-button__icon" :src="messageIcon" alt="" />
            </button>
          </div>
        </div>
      </article>
    </section>

    <div class="post-detail__floating-actions" aria-label="页面快捷操作">
      <div class="post-detail__floating-primary-actions">
        <button
          class="post-detail__floating-button"
          type="button"
          aria-label="回到顶部"
          @click="scrollToTop"
        >
          <img class="post-detail__floating-icon" :src="arrowUpIcon" alt="" />
        </button>
        <button
          class="post-detail__floating-button"
          type="button"
          aria-label="回复帖子"
          @click="openPostReplyComposer"
        >
          <img class="post-detail__floating-icon" :src="messageIcon" alt="" />
        </button>
      </div>

      <button
        v-if="authStore.isAdmin"
        class="post-detail__moderation-button"
        :class="{ 'post-detail__moderation-button--confirm': hideConfirmationActive }"
        type="button"
        :disabled="hidingPost"
        @click="hidePost"
      >
        {{ hidingPost ? '隐藏中...' : hideConfirmationActive ? '确定隐藏' : '隐藏帖子' }}
      </button>
      <p v-if="hidePostError" class="post-detail__moderation-error" role="alert">
        {{ hidePostError }}
      </p>
      <button
        v-if="canDeletePost"
        class="post-detail__moderation-button post-detail__moderation-button--delete"
        :class="{
          'post-detail__moderation-button--confirm': deleteConfirmationActive,
          'post-detail__moderation-button--deleting': deletingPost,
        }"
        type="button"
        :disabled="deletingPost"
        @click="deleteOwnPost"
      >
        {{ deletingPost ? '删除中...' : deleteConfirmationActive ? '确定删除' : '删除帖子' }}
      </button>
      <p v-if="deletePostError" class="post-detail__moderation-error" role="alert">
        {{ deletePostError }}
      </p>
    </div>

    <form
      v-if="replyTarget"
      class="reply-composer"
      autocomplete="off"
      @submit.prevent="submitReply"
    >
      <div class="reply-composer__inner">
        <textarea
          v-model="draft"
          class="reply-composer__input"
          :placeholder="replyPlaceholder"
          autocomplete="off"
          autocapitalize="sentences"
          autocorrect="off"
          maxlength="2000"
          name="bbs-reply-content"
          rows="4"
          spellcheck="false"
          autofocus
        ></textarea>

        <div class="reply-composer__footer">
          <span class="reply-composer__count">
            {{ submitError || `${draft.length} / 2000` }}
          </span>
          <div class="reply-composer__actions">
            <button class="reply-composer__cancel" type="button" @click="closeReplyComposer">
              取消
            </button>
            <button class="reply-composer__submit" type="submit" :disabled="!canSubmit">
              回复
            </button>
          </div>
        </div>
      </div>
    </form>
  </section>

  <section v-else class="post-detail-empty">
    <h1 class="post-detail-empty__title">帖子不存在</h1>
    <p class="post-detail-empty__message">这个帖子可能已隐藏或被删除。</p>
    <RouterLink class="overview-tabs__link post-detail-empty__link" to="/posts">
      返回全部帖子
    </RouterLink>
  </section>
</template>
