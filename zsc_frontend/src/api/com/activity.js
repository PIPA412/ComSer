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
