import { withPostDisplayFields } from './format'
import request from './request'

export async function getPosts(params = {}) {
  const posts = await request.get('/posts', { params })

  return posts.map(withPostDisplayFields)
}

export async function getPost(id) {
  const post = await request.get(`/posts/${id}`)

  return withPostDisplayFields(post)
}

export async function createPost(data) {
  const post = await request.post('/posts', data)

  return withPostDisplayFields(post)
}

export function deletePost(id) {
  return request.delete(`/posts/${id}`)
}
