import request from '@/utils/request'

// ==================== 居民端 - 周边商家展示 ====================

/**
 * 查询服务商列表（居民端）
 * @param {object} query - { serviceType, pageNum, pageSize }
 */
export function listPortalProvider(query) {
  return request({ url: '/com/convenience/portal/provider/list', method: 'get', params: query })
}

/**
 * 查询服务商详情（居民端）
 * @param {number} providerId - 服务商ID
 */
export function getPortalProvider(providerId) {
  return request({ url: '/com/convenience/portal/provider/' + providerId, method: 'get' })
}
