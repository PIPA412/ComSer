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
// 记录阅读（用户点击公告详情时调用）
export function recordRead(announcementId) {
  return request({ url: '/com/announcement/' + announcementId + '/read', method: 'post' })
}
// 获取阅读次数
export function getReadCount(announcementId) {
  return request({ url: '/com/announcement/read/count/' + announcementId, method: 'get' })
}
// 批量获取阅读次数
export function batchReadCount(ids) {
  return request({ url: '/com/announcement/read/batch-count', method: 'get', params: { ids } })
}

// ==================== 推送范围查询（用于精确推送） ====================
// 获取楼栋-单元层级结构（用于级联选择器）
export function getScopeBuildings() {
  return request({ url: '/com/announcement/scope/buildings', method: 'get' })
}
// 根据楼栋查询目标业主/住户
export function getScopeOwners(params) {
  return request({ url: '/com/announcement/scope/owners', method: 'get', params })
}
// 获取各楼栋推送受众统计
export function getScopeStatistics() {
  return request({ url: '/com/announcement/scope/statistics', method: 'get' })
}
