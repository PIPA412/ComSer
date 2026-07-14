import request from '@/utils/request'

// ==================== 楼栋管理 ====================
export function listBuilding(query) {
  return request({ url: '/com/property/building/list', method: 'get', params: query })
}
export function getBuilding(buildingId) {
  return request({ url: '/com/property/building/' + buildingId, method: 'get' })
}
export function addBuilding(data) {
  return request({ url: '/com/property/building', method: 'post', data })
}
export function updateBuilding(data) {
  return request({ url: '/com/property/building', method: 'put', data })
}
export function delBuilding(buildingIds) {
  return request({ url: '/com/property/building/' + buildingIds, method: 'delete' })
}

// ==================== 单元管理 ====================
export function listUnit(query) {
  return request({ url: '/com/property/unit/list', method: 'get', params: query })
}
export function getUnit(unitId) {
  return request({ url: '/com/property/unit/' + unitId, method: 'get' })
}
export function addUnit(data) {
  return request({ url: '/com/property/unit', method: 'post', data })
}
export function updateUnit(data) {
  return request({ url: '/com/property/unit', method: 'put', data })
}
export function delUnit(unitIds) {
  return request({ url: '/com/property/unit/' + unitIds, method: 'delete' })
}

// ==================== 房屋管理 ====================
export function listRoom(query) {
  return request({ url: '/com/property/room/list', method: 'get', params: query })
}
export function getRoom(roomId) {
  return request({ url: '/com/property/room/' + roomId, method: 'get' })
}
export function addRoom(data) {
  return request({ url: '/com/property/room', method: 'post', data })
}
export function updateRoom(data) {
  return request({ url: '/com/property/room', method: 'put', data })
}
export function delRoom(roomIds) {
  return request({ url: '/com/property/room/' + roomIds, method: 'delete' })
}

// ==================== 业主管理 ====================
export function listOwner(query) {
  return request({ url: '/com/property/owner/list', method: 'get', params: query })
}
export function getOwner(ownerId) {
  return request({ url: '/com/property/owner/' + ownerId, method: 'get' })
}
export function addOwner(data) {
  return request({ url: '/com/property/owner', method: 'post', data })
}
export function updateOwner(data) {
  return request({ url: '/com/property/owner', method: 'put', data })
}
export function delOwner(ownerIds) {
  return request({ url: '/com/property/owner/' + ownerIds, method: 'delete' })
}

// ==================== 业主房屋关联 ====================
export function listOwnerRoom(query) {
  return request({ url: '/com/property/ownerroom/list', method: 'get', params: query })
}
export function addOwnerRoom(data) {
  return request({ url: '/com/property/ownerroom', method: 'post', data })
}
export function updateOwnerRoom(data) {
  return request({ url: '/com/property/ownerroom', method: 'put', data })
}
export function delOwnerRoom(ids) {
  return request({ url: '/com/property/ownerroom/' + ids, method: 'delete' })
}

// ==================== 下拉数据 ====================
export function getAllBuildings() {
  return request({ url: '/com/property/building/all', method: 'get' })
}
export function getUnitByBuilding(buildingId) {
  return request({ url: '/com/property/unit/byBuilding/' + buildingId, method: 'get' })
}
export function getRoomByUnit(unitId) {
  return request({ url: '/com/property/room/byUnit/' + unitId, method: 'get' })
}

// ==================== 详情查询（含关联数据） ====================
export function getOwnerDetail(ownerId) {
  return request({ url: '/com/property/owner/detail/' + ownerId, method: 'get' })
}
export function getRoomDetail(roomId) {
  return request({ url: '/com/property/room/detail/' + roomId, method: 'get' })
}

// ==================== 入住登记 ====================
export function ownerCheckIn(data) {
  return request({ url: '/com/property/owner/checkIn', method: 'post', data })
}

// ==================== 绑定额外API ====================
export function bindOwnerRoom(data) {
  return request({ url: '/com/property/ownerroom/bind', method: 'post', data })
}

// ==================== 房产概览 ====================
export function getPropertyTree() {
  return request({ url: '/com/property/tree', method: 'get' })
}
export function getPropertyStatistics() {
  return request({ url: '/com/property/statistics', method: 'get' })
}
