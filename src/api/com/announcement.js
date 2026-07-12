import request from '@/utils/request'

// 公告CRUD
export function listAnnouncement(query) {
  return request({ url: '/com/announcement/list', method: 'get', params: query })
}
export function getAnnouncement(announcementId) {
  return request({ url: '/com/announcement/' + announcementId, method: 'get' })
}
export function addAnnouncement(data) {
  return request({ url: '/com/announcement', method: 'post', data })
}
export function updateAnnouncement(data) {
  return request({ url: '/com/announcement', method: 'put', data })
}
export function delAnnouncement(announcementIds) {
  return request({ url: '/com/announcement/' + announcementIds, method: 'delete' })
}

// 业务操作
export function publishAnnouncement(announcementId) {
  return request({ url: '/com/announcement/publish/' + announcementId, method: 'put' })
}
export function revokeAnnouncement(announcementId) {
  return request({ url: '/com/announcement/revoke/' + announcementId, method: 'put' })
}

// 首页已发布公告（所有登录用户可见）
export function listPublishedAnnouncement() {
  return request({ url: '/com/announcement/published', method: 'get' })
}

// 阅读记录
export function listAnnouncementRead(query) {
  return request({ url: '/com/announcement/read/list', method: 'get', params: query })
}
