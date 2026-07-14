import request from '@/utils/request'

export function listActivity(query) {
  return request({ url: '/com/activity/list', method: 'get', params: query })
}
export function getActivity(activityId) {
  return request({ url: '/com/activity/' + activityId, method: 'get' })
}
export function addActivity(data) {
  return request({ url: '/com/activity', method: 'post', data })
}
export function updateActivity(data) {
  return request({ url: '/com/activity', method: 'put', data })
}
export function delActivity(activityIds) {
  return request({ url: '/com/activity/' + activityIds, method: 'delete' })
}

// 活动报名
export function listPublished(query) {
  return request({ url: '/com/activity/published', method: 'get', params: query })
}
export function listSignup(query) {
  return request({ url: '/com/activity/signup/list', method: 'get', params: query })
}
export function addSignup(data) {
  return request({ url: '/com/activity/signup', method: 'post', data })
}
export function cancelSignup(activityId) {
  return request({ url: '/com/activity/signup/' + activityId, method: 'delete' })
}
export function mySignups() {
  return request({ url: '/com/activity/my-signups', method: 'get' })
}
export function signin(signupId) {
  return request({ url: '/com/activity/signin/' + signupId, method: 'put' })
}
export function batchApproveSignup(ids) {
  return request({ url: '/com/activity/signup/approve', method: 'put', data: { ids } })
}
export function batchRejectSignup(ids, reason) {
  return request({ url: '/com/activity/signup/reject', method: 'put', data: { ids, reason } })
}
export function exportSignupUrl(activityId) {
  return import.meta.env.VITE_APP_BASE_API + '/com/activity/signup/export/' + activityId
}
export function checkinActivity(data) {
  return request({ url: '/com/activity/checkin', method: 'post', data })
}
export function markAbsent(signupId) {
  return request({ url: '/com/activity/signup/absent/' + signupId, method: 'put' })
}
export function saveReview(data) {
  return request({ url: '/com/activity/review', method: 'put', data })
}
export function activityStats() {
  return request({ url: '/com/activity/stats', method: 'get' })
}
