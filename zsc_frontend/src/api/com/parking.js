import request from '@/utils/request'

// 车位管理
export function listSpot(query) {
  return request({ url: '/com/parking/spot/list', method: 'get', params: query })
}
export function getSpot(spotId) {
  return request({ url: '/com/parking/spot/' + spotId, method: 'get' })
}
export function addSpot(data) {
  return request({ url: '/com/parking/spot', method: 'post', data })
}
export function updateSpot(data) {
  return request({ url: '/com/parking/spot', method: 'put', data })
}
export function delSpot(spotIds) {
  return request({ url: '/com/parking/spot/' + spotIds, method: 'delete' })
}

// 车辆管理
export function listVehicle(query) {
  return request({ url: '/com/parking/vehicle/list', method: 'get', params: query })
}
export function getVehicle(vehicleId) {
  return request({ url: '/com/parking/vehicle/' + vehicleId, method: 'get' })
}
export function addVehicle(data) {
  return request({ url: '/com/parking/vehicle', method: 'post', data })
}
export function updateVehicle(data) {
  return request({ url: '/com/parking/vehicle', method: 'put', data })
}
export function delVehicle(vehicleIds) {
  return request({ url: '/com/parking/vehicle/' + vehicleIds, method: 'delete' })
}

// 停车记录
export function listParkingRecord(query) {
  return request({ url: '/com/parking/record/list', method: 'get', params: query })
}
export function addParkingRecord(data) {
  return request({ url: '/com/parking/record', method: 'post', data })
}
