import request from '@/utils/request'

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
export function assignRepair(data) {
  return request({ url: '/com/repair/assign', method: 'put', data })
}
export function finishRepair(data) {
  return request({ url: '/com/repair/finish', method: 'put', data })
}

// 维修记录
export function listRepairRecord(query) {
  return request({ url: '/com/repair/record/list', method: 'get', params: query })
}
export function addRepairRecord(data) {
  return request({ url: '/com/repair/record', method: 'post', data })
}
