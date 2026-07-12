import request from '@/utils/request'

// 访客CRUD
export function listVisitor(query) {
  return request({ url: '/com/visitor/list', method: 'get', params: query })
}
export function getVisitor(visitorId) {
  return request({ url: '/com/visitor/' + visitorId, method: 'get' })
}
export function addVisitor(data) {
  return request({ url: '/com/visitor', method: 'post', data })
}
export function updateVisitor(data) {
  return request({ url: '/com/visitor', method: 'put', data })
}
export function delVisitor(visitorIds) {
  return request({ url: '/com/visitor/' + visitorIds, method: 'delete' })
}

// 业务操作
export function approveVisitor(visitorId) {
  return request({ url: '/com/visitor/approve/' + visitorId, method: 'put' })
}
export function rejectVisitor(visitorId) {
  return request({ url: '/com/visitor/reject/' + visitorId, method: 'put' })
}
export function checkoutVisitor(visitorId) {
  return request({ url: '/com/visitor/checkout/' + visitorId, method: 'put' })
}

// 通行记录
export function listVisitorRecord(query) {
  return request({ url: '/com/visitor/record/list', method: 'get', params: query })
}
