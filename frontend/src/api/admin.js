import { withPostDisplayFields, withReplyDisplayFields } from './format'
import request from './request'

export async function getAdminDashboardStats() {
  const stats = await request.get('/admin/dashboard/stats')

  return {
    ...stats,
    totalBoardCount: stats.boardCount + stats.disabledBoardCount,
    totalPostCount: stats.postCount + stats.hiddenPostCount,
    totalReplyCount: stats.replyCount + stats.hiddenReplyCount,
  }
}

export function getAdminBoards(params = {}) {
  return request.get('/admin/boards', { params })
}

export function createAdminBoard(data) {
  return request.post('/admin/boards', data)
}

export function updateAdminBoard(id, data) {
  return request.put(`/admin/boards/${id}`, data)
}

export function deleteAdminBoard(id) {
  return request.delete(`/admin/boards/${id}`)
}

export async function getAdminPosts(params = {}) {
  const posts = await request.get('/admin/posts', { params })

  return posts.map(withPostDisplayFields)
}

export async function updateAdminPostStatus(id, status) {
  const post = await request.put(`/admin/posts/${id}/status`, { status })

  return withPostDisplayFields(post)
}

export async function getAdminReplies(params = {}) {
  const replies = await request.get('/admin/replies', { params })

  return replies.map(withReplyDisplayFields)
}

export async function updateAdminReplyStatus(id, status) {
  const reply = await request.put(`/admin/replies/${id}/status`, { status })

  return withReplyDisplayFields(reply)
}
