import request from '@/utils/request'

// ==================== 服务商管理 ====================
export function listProvider(query) {
  return request({ url: '/com/convenience/provider/list', method: 'get', params: query })
}
export function getProvider(providerId) {
  return request({ url: '/com/convenience/provider/' + providerId, method: 'get' })
}
export function getAllProvider() {
  return request({ url: '/com/convenience/provider/all', method: 'get' })
}
export function addProvider(data) {
  return request({ url: '/com/convenience/provider', method: 'post', data })
}
export function updateProvider(data) {
  return request({ url: '/com/convenience/provider', method: 'put', data })
}
export function delProvider(providerIds) {
  return request({ url: '/com/convenience/provider/' + providerIds, method: 'delete' })
}

// ==================== 服务项目管理 ====================
export function listServiceItem(query) {
  return request({ url: '/com/convenience/item/list', method: 'get', params: query })
}
export function getServiceItem(itemId) {
  return request({ url: '/com/convenience/item/' + itemId, method: 'get' })
}
export function getItemByProvider(providerId) {
  return request({ url: '/com/convenience/item/byProvider/' + providerId, method: 'get' })
}
export function addServiceItem(data) {
  return request({ url: '/com/convenience/item', method: 'post', data })
}
export function updateServiceItem(data) {
  return request({ url: '/com/convenience/item', method: 'put', data })
}
export function delServiceItem(itemIds) {
  return request({ url: '/com/convenience/item/' + itemIds, method: 'delete' })
}

// ==================== 预约订单管理 ====================
export function listServiceOrder(query) {
  return request({ url: '/com/convenience/order/list', method: 'get', params: query })
}
export function getServiceOrder(orderId) {
  return request({ url: '/com/convenience/order/' + orderId, method: 'get' })
}
export function addServiceOrder(data) {
  return request({ url: '/com/convenience/order', method: 'post', data })
}
export function delServiceOrder(orderIds) {
  return request({ url: '/com/convenience/order/' + orderIds, method: 'delete' })
}
export function acceptServiceOrder(data) {
  return request({ url: '/com/convenience/order/accept', method: 'put', data })
}
export function completeServiceOrder(data) {
  return request({ url: '/com/convenience/order/complete', method: 'put', data })
}
export function cancelServiceOrder(data) {
  return request({ url: '/com/convenience/order/cancel', method: 'put', data })
}

// ==================== 服务评价管理 ====================
export function listServiceReview(query) {
  return request({ url: '/com/convenience/review/list', method: 'get', params: query })
}
export function getServiceReview(reviewId) {
  return request({ url: '/com/convenience/review/' + reviewId, method: 'get' })
}
export function addServiceReview(data) {
  return request({ url: '/com/convenience/review', method: 'post', data })
}
export function updateServiceReview(data) {
  return request({ url: '/com/convenience/review', method: 'put', data })
}
export function delServiceReview(reviewIds) {
  return request({ url: '/com/convenience/review/' + reviewIds, method: 'delete' })
}
