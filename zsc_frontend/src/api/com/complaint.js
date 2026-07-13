import request from '@/utils/request'

export function listComplaint(query) {
  return request({ url: '/com/complaint/list', method: 'get', params: query })
}
export function getComplaint(complaintId) {
  return request({ url: '/com/complaint/' + complaintId, method: 'get' })
}
export function addComplaint(data) {
  return request({ url: '/com/complaint', method: 'post', data })
}
export function delComplaint(complaintIds) {
  return request({ url: '/com/complaint/' + complaintIds, method: 'delete' })
}
export function acceptComplaint(data) {
  return request({ url: '/com/complaint/accept', method: 'put', data })
}
export function finishComplaint(data) {
  return request({ url: '/com/complaint/finish', method: 'put', data })
}

// 满意度评价
export function rateComplaint(data) {
  return request({ url: '/com/complaint/rate', method: 'put', data })
}

// 当前用户的投诉/建议
export function listMyComplaint(query) {
  return request({ url: '/com/complaint/my', method: 'get', params: query })
}

// 处理反馈
export function listComplaintFeedback(query) {
  return request({ url: '/com/complaint/feedback/list', method: 'get', params: query })
}
export function addComplaintFeedback(data) {
  return request({ url: '/com/complaint/feedback', method: 'post', data })
}
