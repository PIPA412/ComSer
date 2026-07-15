import request from '@/utils/request'

// ==================== 报修单 ====================
export function listRepair(query) {
  return request({ url: '/com/repair/list', method: 'get', params: query })
}
export function getRepair(repairId) {
  return request({ url: '/com/repair/' + repairId, method: 'get' })
}
export function addRepair(data) {
  return request({ url: '/com/repair', method: 'post', data })
}
export function updateRepair(data) {
  return request({ url: '/com/repair', method: 'put', data })
}
export function delRepair(repairIds) {
  return request({ url: '/com/repair/' + repairIds, method: 'delete' })
}

// ==================== 状态操作 ====================
export function updateRepairStatus(repairId, status) {
  return request({ url: '/com/repair/status/' + repairId, method: 'put', data: { status } })
}
export function assignRepair(data) {
  return request({ url: '/com/repair/assign', method: 'put', data })
}
export function finishRepair(data) {
  return request({ url: '/com/repair/finish', method: 'put', data })
}
export function rateRepair(data) {
  return request({ url: '/com/repair/rate', method: 'put', data })
}
export function cancelRepair(repairId) {
  return request({ url: '/com/repair/cancel/' + repairId, method: 'put' })
}

// ==================== 列表查询 ====================
export function myListRepair(query) {
  return request({ url: '/com/repair/myList', method: 'get', params: query })
}
export function pendingList(query) {
  return request({ url: '/com/repair/pending', method: 'get', params: query })
}

// ==================== 维修记录 ====================
export function listRepairRecord(query) {
  return request({ url: '/com/repair/record/list', method: 'get', params: query })
}
export function addRepairRecord(data) {
  return request({ url: '/com/repair/record', method: 'post', data })
}

// ==================== 统计 ====================
export function getRepairStatistics() {
  return request({ url: '/com/repair/statistics', method: 'get' })
}
