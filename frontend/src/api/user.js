import request from './request'

export function registerUser(data) {
  return request.post('/user/register', data)
}

export function loginUser(data) {
  return request.post('/user/login', data)
}

export function getUserProfile() {
  return request.get('/user/profile')
}

export function getUserProfileById(id) {
  return request.get(`/user/profile/${id}`)
}

export function updateUserProfile(data) {
  return request.put('/user/profile', data)
}

export function uploadUserAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData)
}

export function updateUserPassword(data) {
  return request.put('/user/password', data)
}

export function getUserReplies() {
  return request.get('/user/replies')
}

export function getUserRepliesById(id) {
  return request.get(`/user/${id}/replies`)
}
