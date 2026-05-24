import { boards } from './boards'
import { posts } from './posts'
import { replies } from './replies'
import { users } from './users'

const mockNow = new Date('2026-05-23 17:00:00').getTime()

function byId(items) {
  return new Map(items.map((item) => [item.id, item]))
}

const boardById = byId(boards)
const userById = byId(users)

function formatTimestamp(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function activeReplyCount(postId) {
  return replies.filter((reply) => reply.postId === postId && reply.status === 1).length
}

function formatRelativeTime(dateTime) {
  const time = new Date(dateTime).getTime()
  const diffHours = Math.max(1, Math.round((mockNow - time) / 1000 / 60 / 60))

  if (diffHours < 24) {
    return `${diffHours}h`
  }

  return `${Math.round(diffHours / 24)}d`
}

function contentPreview(content) {
  return content.length > 96 ? `${content.slice(0, 96)}...` : content
}

function matchesOptionalId(value, expected) {
  return expected == null || value === Number(expected)
}

function withPostMeta(post) {
  const board = boardById.get(post.boardId)
  const author = userById.get(post.userId)

  return {
    ...post,
    boardName: board?.name ?? '未知版块',
    boardColorHex: board?.colorHex ?? '#007aff',
    authorName: author?.nickname || author?.username || post.authorName || '未知用户',
    authorAvatar: author?.avatar ?? post.authorAvatar ?? '',
    authorRole: author?.role ?? post.authorRole ?? 'USER',
    contentPreview: contentPreview(post.content),
    replyCount: activeReplyCount(post.id),
    relativeTime: formatRelativeTime(post.updatedAt),
  }
}

function withReplyMeta(reply) {
  const author = userById.get(reply.userId)
  const parentReply = reply.parentReplyId
    ? replies.find((item) => item.id === reply.parentReplyId && item.status === 1)
    : null
  const parentAuthor = parentReply ? userById.get(parentReply.userId) : null

  return {
    ...reply,
    authorName: author?.nickname || author?.username || '未知用户',
    authorAvatar: author?.avatar ?? '',
    authorRole: author?.role ?? reply.authorRole ?? 'USER',
    contentPreview: contentPreview(reply.content),
    parentReply: parentReply
      ? {
          id: parentReply.id,
          authorName: parentAuthor?.nickname || parentAuthor?.username || '未知用户',
          contentPreview: contentPreview(parentReply.content),
        }
      : null,
  }
}

function withUserReplyMeta(reply) {
  const post = posts.find((item) => item.id === reply.postId && item.status === 1)
  const board = post ? boardById.get(post.boardId) : null
  const author = userById.get(reply.userId)
  const parentReply = reply.parentReplyId
    ? replies.find((item) => item.id === reply.parentReplyId && item.status === 1)
    : null
  const parentAuthor = parentReply ? userById.get(parentReply.userId) : null
  const postAuthor = post ? userById.get(post.userId) : null

  return {
    ...reply,
    authorName: author?.nickname || author?.username || '未知用户',
    authorAvatar: author?.avatar ?? '',
    authorRole: author?.role ?? reply.authorRole ?? 'USER',
    contentPreview: contentPreview(reply.content),
    postTitle: post?.title ?? '未知帖子',
    postContentPreview: contentPreview(post?.content ?? ''),
    boardId: post?.boardId ?? null,
    boardName: board?.name ?? '未知版块',
    boardColorHex: board?.colorHex ?? '#007aff',
    reference: parentReply
      ? {
          type: 'reply',
          id: parentReply.id,
          authorName: parentAuthor?.nickname || parentAuthor?.username || '未知用户',
          contentPreview: contentPreview(parentReply.content),
          href: `/post/${reply.postId}#reply-${parentReply.id}`,
        }
      : {
          type: 'post',
          id: post?.id ?? reply.postId,
          authorName: postAuthor?.nickname || postAuthor?.username || '未知用户',
          contentPreview: contentPreview(post?.content ?? ''),
          href: `/post/${post?.id ?? reply.postId}`,
        },
    relativeTime: formatRelativeTime(reply.updatedAt),
  }
}

export function visibleBoards() {
  return boards
    .filter((board) => board.status === 1)
    .toSorted((left, right) => left.sortOrder - right.sortOrder || left.id - right.id)
    .map((board) => {
      const boardPosts = visiblePosts().filter((post) => post.boardId === board.id)

      return {
        ...board,
        postCount: boardPosts.length,
        replyCount: boardPosts.reduce((total, post) => total + post.replyCount, 0),
        latestPost: boardPosts[0] ?? null,
      }
    })
}

export function visiblePosts({ boardId, userId } = {}) {
  return posts
    .filter(
      (post) =>
        post.status === 1 &&
        matchesOptionalId(post.boardId, boardId) &&
        matchesOptionalId(post.userId, userId),
    )
    .toSorted((left, right) => new Date(right.updatedAt) - new Date(left.updatedAt))
    .map(withPostMeta)
}

export function postDetail(postId) {
  const post = posts.find((item) => item.id === Number(postId) && item.status === 1)

  return post ? withPostMeta(post) : null
}

export function createPost({ boardId, title, content, user }) {
  if (!user?.id) {
    throw new Error('请先登录')
  }

  const normalizedBoardId = Number(boardId)
  const board = boards.find((item) => item.id === normalizedBoardId && item.status === 1)

  if (!board) {
    throw new Error('请选择有效版块')
  }

  const normalizedTitle = title.trim()
  const normalizedContent = content.trim()

  if (!normalizedTitle) {
    throw new Error('标题不能为空')
  }

  if (!normalizedContent) {
    throw new Error('内容不能为空')
  }

  const now = formatTimestamp()
  const post = {
    id: Math.max(0, ...posts.map((item) => item.id)) + 1,
    boardId: normalizedBoardId,
    userId: user.id,
    authorName: user.nickname || user.username || '当前用户',
    authorAvatar: user.avatar || '',
    authorRole: user.role || 'USER',
    title: normalizedTitle,
    content: normalizedContent,
    status: 1,
    viewCount: 0,
    createdAt: now,
    updatedAt: now,
  }

  posts.push(post)

  return withPostMeta(post)
}

export function postReplies(postId, { page = 1, pageSize = 20 } = {}) {
  const list = replies
    .filter((reply) => reply.postId === Number(postId) && reply.status === 1)
    .toSorted((left, right) => new Date(left.createdAt) - new Date(right.createdAt))
    .map(withReplyMeta)
  const start = (page - 1) * pageSize
  const pageList = list.slice(start, start + pageSize)
  const totalPages = Math.ceil(list.length / pageSize)

  return {
    list: pageList,
    total: list.length,
    page,
    pageSize,
    totalPages,
    hasNext: page < totalPages,
    hasPrevious: page > 1,
  }
}

export function userReplies(userId, { page = 1, pageSize = 10 } = {}) {
  const list = replies
    .filter((reply) => {
      const post = posts.find((item) => item.id === reply.postId)

      return reply.status === 1 && reply.userId === Number(userId) && post?.status === 1
    })
    .toSorted((left, right) => new Date(right.updatedAt) - new Date(left.updatedAt))
    .map(withUserReplyMeta)
  const start = (page - 1) * pageSize
  const pageList = list.slice(start, start + pageSize)
  const totalPages = Math.ceil(list.length / pageSize)

  return {
    list: pageList,
    total: list.length,
    page,
    pageSize,
    totalPages,
    hasNext: page < totalPages,
    hasPrevious: page > 1,
  }
}
