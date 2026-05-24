import { withReplyDisplayFields } from './format'
import request from './request'

export async function getPostReplies(postId) {
  const replies = await request.get(`/posts/${postId}/replies`)

  return replies.map(withReplyDisplayFields)
}

export async function createReply(postId, data) {
  const reply = await request.post(`/posts/${postId}/replies`, data)

  return withReplyDisplayFields(reply)
}

export function deleteReply(id) {
  return request.delete(`/replies/${id}`)
}
