<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { updateAdminPostStatus } from '@/api/admin'
import { getPost } from '@/api/posts'
import { createReply, getPostReplies } from '@/api/replies'
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
let hideConfirmationTimer = null

function formatDisplayDate(dateTime) {
  return dateTime?.slice(0, 10) ?? ''
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
  hidePostError.value = ''

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

const visibleReplyCount = computed(() => replies.value.length)
const replyPlaceholder = computed(() => `回复： ${replyTarget.value?.contentPreview ?? ''}`)
const canSubmit = computed(() => draft.value.trim().length > 0)

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
      <span class="board-badge" :style="{ '--board-color': post.boardColorHex }">
        {{ post.boardName }}
      </span>
    </header>

    <article class="post-block post-block--main">
      <div class="post-block__header">
        <div class="post-block__avatar" aria-hidden="true">
          <img class="post-block__avatar-image" :src="avatarSrc(post.authorAvatar)" alt="" />
        </div>

        <div class="post-block__meta-row">
          <div class="post-block__author-group">
            <strong class="post-block__author">{{ post.authorName }}</strong>
            <span v-if="post.authorRole === 'ADMIN'" class="post-block__admin-badge"> 管理员 </span>
          </div>

          <dl class="post-block__stats" aria-label="帖子数据">
            <div class="post-block__stat">
              <dt>回复</dt>
              <dd>{{ visibleReplyCount }}</dd>
            </div>
            <div class="post-block__stat">
              <dt>浏览</dt>
              <dd>{{ post.viewCount }}</dd>
            </div>
            <div class="post-block__author-group">
              <span class="post-block__date">{{ formatDisplayDate(post.createdAt) }}</span>
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
          <div class="post-block__avatar" aria-hidden="true">
            <img class="post-block__avatar-image" :src="avatarSrc(reply.authorAvatar)" alt="" />
          </div>

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
              <span class="post-block__reply-id">#{{ reply.id }}</span>
              <span class="post-block__date">{{ formatDisplayDate(reply.createdAt) }}</span>
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
