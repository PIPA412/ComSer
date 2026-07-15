import request from '@/utils/request'

// 服务商
export function listProvider(query) {
  return request({ url: '/com/convenience/provider/list', method: 'get', params: query })
}
export function getProvider(providerId) {
  return request({ url: '/com/convenience/provider/' + providerId, method: 'get' })
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

// 服务项目
export function listServiceItem(query) {
  return request({ url: '/com/convenience/item/list', method: 'get', params: query })
}
export function getServiceItem(itemId) {
  return request({ url: '/com/convenience/item/' + itemId, method: 'get' })
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

// 服务订单
export function listServiceOrder(query) {
  return request({ url: '/com/convenience/order/list', method: 'get', params: query })
}
export function getServiceOrder(orderId) {
  return request({ url: '/com/convenience/order/' + orderId, method: 'get' })
}
export function addServiceOrder(data) {
  return request({ url: '/com/convenience/order', method: 'post', data })
}
