import request from '@/utils/request'

// 获取所有面板数据（一次性返回）
export function getDashboardAll(params) {
  return request({ url: '/com/dashboard/all', method: 'get', params })
}

// 人口概览
export function getDashboardPopulation(buildingId) {
  return request({ url: '/com/dashboard/population', method: 'get', params: { buildingId } })
}

// 房屋概览
export function getDashboardRoom(buildingId) {
  return request({ url: '/com/dashboard/room', method: 'get', params: { buildingId } })
}

// 工单概览
export function getDashboardRepair(buildingId) {
  return request({ url: '/com/dashboard/repair', method: 'get', params: { buildingId } })
}

// 收费概览
export function getDashboardFee(buildingId) {
  return request({ url: '/com/dashboard/fee', method: 'get', params: { buildingId } })
}

// 投诉概览
export function getDashboardComplaint(buildingId) {
  return request({ url: '/com/dashboard/complaint', method: 'get', params: { buildingId } })
}

// 活动概览
export function getDashboardActivity(buildingId) {
  return request({ url: '/com/dashboard/activity', method: 'get', params: { buildingId } })
}

// 楼栋列表
export function getDashboardBuildings() {
  return request({ url: '/com/dashboard/buildings', method: 'get' })
}

// ========== 预警管理 ==========

/** 获取待处理预警列表（驾驶舱首页用） */
export function getPendingAlerts() {
  return request({ url: '/com/dashboard/alert/record/pending', method: 'get' })
}

/** 预警规则列表 */
export function listAlertRules(params) {
  return request({ url: '/com/dashboard/alert/rule/list', method: 'get', params })
}

/** 获取所有启用的预警规则（简略列表） */
export function getAllAlertRules() {
  return request({ url: '/com/dashboard/alert/rule/all', method: 'get' })
}

/** 获取单条预警规则 */
export function getAlertRule(ruleId) {
  return request({ url: `/com/dashboard/alert/rule/${ruleId}`, method: 'get' })
}

/** 新增预警规则 */
export function addAlertRule(data) {
  return request({ url: '/com/dashboard/alert/rule', method: 'post', data })
}

/** 修改预警规则 */
export function updateAlertRule(data) {
  return request({ url: '/com/dashboard/alert/rule', method: 'put', data })
}

/** 删除预警规则 */
export function delAlertRule(ruleIds) {
  return request({ url: `/com/dashboard/alert/rule/${ruleIds}`, method: 'delete' })
}

/** 预警记录列表 */
export function listAlertRecords(params) {
  return request({ url: '/com/dashboard/alert/record/list', method: 'get', params })
}

/** 处理预警记录 */
export function handleAlertRecord(data) {
  return request({ url: '/com/dashboard/alert/record/handle', method: 'put', data })
}
