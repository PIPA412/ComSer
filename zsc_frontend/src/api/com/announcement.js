import request from '@/utils/request'

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
export function publishAnnouncement(announcementId) {
  return request({ url: '/com/announcement/publish/' + announcementId, method: 'put' })
}

// 阅读记录
export function listAnnouncementRead(query) {
  return request({ url: '/com/announcement/read/list', method: 'get', params: query })
}
