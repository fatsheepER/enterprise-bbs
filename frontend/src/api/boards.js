import request from './request'

export function getBoards(params = {}) {
  return request.get('/boards', { params })
}

export function getBoard(id) {
  return request.get(`/boards/${id}`)
}
