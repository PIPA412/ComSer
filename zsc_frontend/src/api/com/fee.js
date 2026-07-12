import request from '@/utils/request'

// 费用项目
export function listFeeItem(query) {
  return request({ url: '/com/fee/item/list', method: 'get', params: query })
}
export function getFeeItem(itemId) {
  return request({ url: '/com/fee/item/' + itemId, method: 'get' })
}
export function addFeeItem(data) {
  return request({ url: '/com/fee/item', method: 'post', data })
}
export function updateFeeItem(data) {
  return request({ url: '/com/fee/item', method: 'put', data })
}
export function delFeeItem(itemIds) {
  return request({ url: '/com/fee/item/' + itemIds, method: 'delete' })
}

// 账单管理
export function listFeeBill(query) {
  return request({ url: '/com/fee/bill/list', method: 'get', params: query })
}
export function getFeeBill(billId) {
  return request({ url: '/com/fee/bill/' + billId, method: 'get' })
}
export function addFeeBill(data) {
  return request({ url: '/com/fee/bill', method: 'post', data })
}
export function delFeeBill(billIds) {
  return request({ url: '/com/fee/bill/' + billIds, method: 'delete' })
}

// 缴费记录
export function listFeePayment(query) {
  return request({ url: '/com/fee/payment/list', method: 'get', params: query })
}
export function addFeePayment(data) {
  return request({ url: '/com/fee/payment', method: 'post', data })
}
