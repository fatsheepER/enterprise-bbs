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
    authorName: author?.nickname || author?.username || '未知用户',
    authorAvatar: author?.avatar ?? '',
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
