<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import arrowUpIcon from '../../assets/arrowshape.up.svg'
import messageIcon from '../../assets/message.svg'
import personPlaceholder from '../../assets/person-placeholder.svg'
import { postDetail, postReplies } from '../../mock/forumViewModels'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const postId = computed(() => Number(route.params.id))
const post = computed(() => postDetail(postId.value))
const replies = ref([])
const draft = ref('')
const replyTarget = ref(null)
const nextReplyId = ref(1)

function formatDisplayDate(dateTime) {
  return dateTime?.slice(0, 10) ?? ''
}

function formatTimestamp(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function previewText(content = '', maxLength = 72) {
  const normalized = content.replace(/\s+/g, ' ').trim()

  return normalized.length > maxLength ? `${normalized.slice(0, maxLength)}...` : normalized
}

function loadReplies() {
  const page = postReplies(postId.value)
  replies.value = page.list
  nextReplyId.value = Math.max(0, ...page.list.map((reply) => reply.id)) + 1
}

watch(postId, loadReplies, { immediate: true })

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
}

function closeReplyComposer() {
  replyTarget.value = null
  draft.value = ''
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

function submitReply() {
  if (!canSubmit.value || !post.value || !replyTarget.value) {
    return
  }

  if (!requireLogin()) {
    return
  }

  const currentUser = authStore.currentUser
  const createdAt = formatTimestamp()
  const parentReply = replyTarget.value.type === 'reply' ? replyTarget.value : null
  const newReply = {
    id: nextReplyId.value,
    postId: post.value.id,
    userId: currentUser.id,
    authorName: currentUser.nickname || currentUser.username || '当前用户',
    authorAvatar: currentUser.avatar || '',
    parentReplyId: parentReply?.id ?? null,
    parentReply: parentReply
      ? {
          id: parentReply.id,
          authorName: parentReply.authorName,
          contentPreview: parentReply.contentPreview,
        }
      : null,
    content: draft.value.trim(),
    contentPreview: previewText(draft.value),
    status: 1,
    createdAt,
    updatedAt: createdAt,
  }

  replies.value = [...replies.value, newReply]
  nextReplyId.value += 1
  closeReplyComposer()
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
          <img class="post-block__avatar-image" :src="personPlaceholder" alt="" />
        </div>

        <div class="post-block__meta-row">
          <div class="post-block__author-group">
            <strong class="post-block__author">{{ post.authorName }}</strong>
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
            <img class="post-block__avatar-image" :src="personPlaceholder" alt="" />
          </div>

          <div class="post-block__meta-row">
            <div class="post-block__author-group">
              <strong class="post-block__author">{{ reply.authorName }}</strong>
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
          <span class="reply-composer__count">{{ draft.length }} / 2000</span>
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
