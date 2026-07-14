<template>
  <div class="portal-container">
    <!-- ==================== 顶栏 ==================== -->
    <div class="portal-header">
      <h2 class="portal-title">
        <el-icon size="24" color="#409eff"><Shop /></el-icon>
        周边商家
      </h2>
      <p class="portal-desc">为您推荐附近优质商家，轻松预约各类服务</p>
    </div>

    <!-- ==================== 搜索 / 筛选栏 ==================== -->
    <div class="portal-toolbar">
      <el-form :model="queryParams" :inline="true" size="large">
        <el-form-item label="服务类型">
          <el-select
            v-model="queryParams.serviceType"
            placeholder="全部分类"
            clearable
            style="width:200px"
            @change="handleFilter"
          >
            <el-option label="全部分类" value="" />
            <el-option
              v-for="t in serviceTypes"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleFilter">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="portal-stats">
        共 <strong>{{ total }}</strong> 家商家
      </div>
    </div>

    <!-- ==================== 加载状态 ==================== -->
    <div v-if="loading" class="portal-loading">
      <el-skeleton :rows="0" animated>
        <template #template>
          <el-row :gutter="20">
            <el-col v-for="i in 6" :key="i" :xs="24" :sm="12" :md="8" :lg="6">
              <el-card shadow="never" style="margin-bottom:20px">
                <el-skeleton-item variant="text" style="width:60%;height:24px;margin-bottom:12px" />
                <el-skeleton-item variant="text" style="width:40%;height:18px;margin-bottom:8px" />
                <el-skeleton-item variant="text" style="width:80%;height:18px;margin-bottom:8px" />
                <el-skeleton-item variant="text" style="width:50%;height:18px" />
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>
    </div>

    <!-- ==================== 空状态 ==================== -->
    <el-empty v-else-if="!loading && providerList.length === 0" description="暂无相关商家" />

    <!-- ==================== 商家卡片列表 ==================== -->
    <div v-else class="portal-grid">
      <el-row :gutter="20">
        <el-col
          v-for="item in providerList"
          :key="item.providerId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card
            shadow="hover"
            class="provider-card"
            @click="handleDetail(item.providerId)"
          >
            <!-- 类型标签 -->
            <div class="card-type-tag">
              <el-tag size="small" effect="plain" round>{{ item.serviceType || '综合' }}</el-tag>
            </div>
            <!-- 名称 -->
            <h3 class="card-name">{{ item.providerName }}</h3>
            <!-- 评分 + 项目数 -->
            <div class="card-meta">
              <span class="card-rating">
                <el-rate
                  v-model="item._ratingDisplay"
                  disabled
                  show-score
                  score-template="{value}"
                  size="small"
                  :colors="ratingColors"
                />
              </span>
              <span class="card-items">
                <el-icon size="14"><List /></el-icon>
                {{ item.serviceItemCount }} 项服务
              </span>
            </div>
            <!-- 地址 -->
            <div class="card-address" :title="item.address">
              <el-icon size="14"><Location /></el-icon>
              {{ item.address || '地址未填写' }}
            </div>
            <!-- 电话 -->
            <div class="card-phone">
              <el-icon size="14"><Phone /></el-icon>
              {{ item.contactPhone || '暂无' }}
            </div>
            <!-- 优惠信息 -->
            <div v-if="item.remark" class="card-promo">
              <el-icon size="14"><Coin /></el-icon>
              {{ item.remark }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- ==================== 分页 ==================== -->
    <div v-if="total > 0" class="portal-pagination">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="getList"
        @current-change="getList"
      />
    </div>

    <!-- ==================== 商家详情弹窗 ==================== -->
    <el-dialog
      v-model="detailVisible"
      :title="detailData?.providerName || '商家详情'"
      width="720px"
      top="5vh"
      destroy-on-close
    >
      <template v-if="detailLoading">
        <el-skeleton :rows="6" animated />
      </template>
      <template v-else-if="detailData">
        <!-- 基本信息 -->
        <div class="detail-header">
          <div class="detail-info">
            <h3>{{ detailData.providerName }}</h3>
            <el-tag size="small" effect="plain" round style="margin-left:8px">
              {{ detailData.serviceType || '综合' }}
            </el-tag>
            <div class="detail-rating">
              <el-rate
                :model-value="detailData.avgRating || 0"
                disabled
                show-score
                score-template="{value}"
                :colors="ratingColors"
              />
              <span v-if="!detailData.avgRating" style="color:#999;font-size:13px;margin-left:8px">暂无评价</span>
            </div>
          </div>
        </div>
        <el-divider />
        <!-- 联系方式 -->
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="联系人" :span="1">
            {{ detailData.contactName || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话" :span="1">
            <a :href="'tel:' + detailData.contactPhone" style="color:#409eff">
              {{ detailData.contactPhone || '未填写' }}
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            {{ detailData.address || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="服务描述" :span="2">
            {{ detailData.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="入驻时间" :span="1">
            {{ detailData.settleDate || '未记录' }}
          </el-descriptions-item>
          <el-descriptions-item label="优惠信息" :span="1">
            <el-tag v-if="detailData.remark" type="danger" effect="plain">{{ detailData.remark }}</el-tag>
            <span v-else style="color:#999">暂无</span>
          </el-descriptions-item>
        </el-descriptions>
        <!-- 服务项目列表 -->
        <h4 style="margin:20px 0 12px">
          <el-icon size="18"><List /></el-icon>
          服务项目（{{ detailData.items?.length || 0 }}）
        </h4>
        <el-table
          v-if="detailData.items && detailData.items.length > 0"
          :data="detailData.items"
          border
          stripe
          size="small"
        >
          <el-table-column label="项目名称" prop="itemName" min-width="140" />
          <el-table-column label="价格" prop="price" width="100">
            <template #default="scope">
              <span style="color:#f56c6c;font-weight:bold">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="时长" prop="duration" width="80">
            <template #default="scope">{{ scope.row.duration }} 分钟</template>
          </el-table-column>
          <el-table-column label="预约方式" prop="bookingMethod" width="100" />
          <el-table-column label="服务详情" prop="serviceDetail" min-width="180" show-overflow-tooltip />
        </el-table>
        <el-empty v-else description="暂无服务项目" :image-size="80" />
      </template>
      <template v-else>
        <el-empty description="未找到商家信息" :image-size="80" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ConveniencePortal">
import { listPortalProvider, getPortalProvider } from '@/api/com/portal'
import { Shop, Location, Phone, List, Coin } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// ======================== 响应式数据 ========================
const loading = ref(true)
const providerList = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 12,
  serviceType: null
})

// 服务类型选项（从后台数据中动态提取）
const serviceTypes = ref([])

// 评分颜色
const ratingColors = ref(['#f56c6c', '#e6a23c', '#67c23a'])

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

// ======================== 获取列表 ========================
function getList() {
  loading.value = true
  listPortalProvider(queryParams.value).then(res => {
    // 为每项补充评分显示用的绑定值
    const list = (res.rows || []).map(item => ({
      ...item,
      _ratingDisplay: item.avgRating || 0
    }))
    providerList.value = list
    total.value = res.total || 0

    // 从返回数据中提取所有服务类型，用于下拉筛选
    const types = new Set()
    list.forEach(item => {
      if (item.serviceType) types.add(item.serviceType)
    })
    serviceTypes.value = Array.from(types).sort()
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleFilter() {
  queryParams.value.pageNum = 1
  getList()
}

function handleReset() {
  queryParams.value.serviceType = null
  handleFilter()
}

// ======================== 查看详情 ========================
function handleDetail(providerId) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  getPortalProvider(providerId).then(res => {
    detailData.value = res.data
    detailLoading.value = false
  }).catch(() => {
    detailLoading.value = false
  })
}

// ======================== 初始化 ========================
getList()
</script>

<style scoped>
.portal-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.portal-header {
  margin-bottom: 28px;
}
.portal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
}
.portal-desc {
  color: #909399;
  font-size: 14px;
  margin: 0 0 0 32px;
}

.portal-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.portal-stats {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}
.portal-stats strong {
  color: #409eff;
  font-size: 16px;
}

.portal-loading {
  padding: 20px 0;
}

/* ========== 卡片 ========== */
.portal-grid {
  min-height: 300px;
}
.provider-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 10px;
  position: relative;
}
.provider-card:hover {
  transform: translateY(-4px);
}
.card-type-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}
.card-name {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-right: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  gap: 8px;
  flex-wrap: wrap;
}
.card-rating {
  display: inline-flex;
  align-items: center;
}
.card-items {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
}
.card-address,
.card-phone,
.card-promo {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-promo {
  color: #f56c6c;
  margin-top: 6px;
  font-weight: 500;
}

/* ========== 分页 ========== */
.portal-pagination {
  display: flex;
  justify-content: center;
  padding: 30px 0 10px;
}

/* ========== 详情弹窗 ========== */
.detail-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 0;
}
.detail-info h3 {
  display: inline;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}
.detail-rating {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

/* ========== 响应式微调 ========== */
@media (max-width: 768px) {
  .portal-container {
    padding: 12px;
  }
  .portal-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
