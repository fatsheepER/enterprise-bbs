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

export function visiblePosts() {
  return posts
    .filter((post) => post.status === 1)
    .toSorted((left, right) => new Date(right.updatedAt) - new Date(left.updatedAt))
    .map((post) => {
      const board = boardById.get(post.boardId)
      const author = userById.get(post.userId)

      return {
        ...post,
        boardName: board?.name ?? '未知版块',
        boardColorHex: board?.colorHex ?? '#007aff',
        authorName: author?.nickname || author?.username || '未知用户',
        authorAvatar: author?.avatar ?? '',
        replyCount: activeReplyCount(post.id),
        relativeTime: formatRelativeTime(post.updatedAt),
      }
    })
}
