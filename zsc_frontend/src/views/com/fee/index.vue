<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="费用项目" name="item">
        <fee-item />
      </el-tab-pane>
      <el-tab-pane label="账单管理" name="bill">
        <fee-bill />
      </el-tab-pane>
      <el-tab-pane label="缴费记录" name="payment">
        <fee-payment />
      </el-tab-pane>
      <el-tab-pane label="费用统计" name="statistics">
        <div style="padding:10px;">
          <!-- 顶部统计卡片 -->
          <el-row :gutter="20" style="margin-bottom:20px;">
            <el-col :span="6">
              <el-card><div style="text-align:center">
                <p style="color:#999;margin:0;">本月应收</p>
                <p style="font-size:24px;color:#409eff;font-weight:bold;margin:5px 0;">¥{{ stats.totalAmount || 0 }}</p>
                <p style="font-size:12px;color:#999;margin:0;">共 {{ stats.totalCount }} 笔</p>
              </div></el-card>
            </el-col>
            <el-col :span="6">
              <el-card><div style="text-align:center">
                <p style="color:#999;margin:0;">本月已收</p>
                <p style="font-size:24px;color:#67c23a;font-weight:bold;margin:5px 0;">¥{{ stats.paidAmount || 0 }}</p>
                <p style="font-size:12px;color:#999;margin:0;">{{ stats.paidCount }} 笔已缴</p>
              </div></el-card>
            </el-col>
            <el-col :span="6">
              <el-card><div style="text-align:center">
                <p style="color:#999;margin:0;">本月欠费</p>
                <p style="font-size:24px;color:#f56c6c;font-weight:bold;margin:5px 0;">¥{{ stats.unpaidAmount || 0 }}</p>
                <p style="font-size:12px;color:#999;margin:0;">{{ stats.unpaidCount }} 笔未缴</p>
              </div></el-card>
            </el-col>
            <el-col :span="6">
              <el-card><div style="text-align:center">
                <p style="color:#999;margin:0;">本月收缴率</p>
                <p style="font-size:24px;color:#e6a23c;font-weight:bold;margin:5px 0;">{{ stats.rate || 0 }}%</p>
                <el-progress :percentage="Number(stats.rate || 0)" :stroke-width="6" :show-text="false" />
              </div></el-card>
            </el-col>
          </el-row>

          <!-- 按费项统计 -->
          <el-card style="margin-bottom:20px;">
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span>📊 按费项统计（{{ stats.period || currentPeriod }}）</span>
                <el-radio-group v-model="periodSelect" size="small" @change="loadStats">
                  <el-radio-button label="当月">当月</el-radio-button>
                  <el-radio-button label="2026-07">2026-07</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <el-table :data="stats.itemStats || []" border>
              <el-table-column label="费用项目" prop="itemName" />
              <el-table-column label="笔数" prop="count" width="80" align="center"/>
              <el-table-column label="应收金额" prop="total" width="120">
                <template #default="s">¥{{ s.row.total || 0 }}</template>
              </el-table-column>
              <el-table-column label="已收金额" prop="paid" width="120">
                <template #default="s"><span style="color:#67c23a;">¥{{ s.row.paid || 0 }}</span></template>
              </el-table-column>
              <el-table-column label="欠费金额" width="120">
                <template #default="s"><span style="color:#f56c6c;">¥{{ s.row.unpaid || 0 }}</span></template>
              </el-table-column>
              <el-table-column label="收缴率" width="180">
                <template #default="s">
                  <el-progress :percentage="Number(s.row.rate || 0)" :status="s.row.rate >= 80 ? 'success' : (s.row.rate >= 50 ? '' : 'exception')" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- 欠费排行 -->
          <el-card>
            <template #header>
              <span>📋 欠费排行（TOP 10）</span>
            </template>
            <el-table :data="stats.overdueRank || []" border>
              <el-table-column label="排名" type="index" width="60" align="center"/>
              <el-table-column label="账单编号" prop="billNo" width="220"/>
              <el-table-column label="业主" prop="ownerName" width="100"/>
              <el-table-column label="房屋" prop="roomNumber" width="100"/>
              <el-table-column label="欠费金额" width="120">
                <template #default="s"><span style="color:#f56c6c;font-weight:bold;">¥{{ s.row.amount || 0 }}</span></template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!stats.overdueRank || stats.overdueRank.length === 0" description="暂无欠费记录" />
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="FeeManagement">
import FeeItem from './item.vue'
import FeeBill from './bill.vue'
import FeePayment from './payment.vue'
import request from '@/utils/request'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref(route.query.tab || 'item')
const currentPeriod = new Date().toISOString().slice(0, 7)
const periodSelect = ref('当月')
const stats = ref({})

function loadStats() {
  const period = periodSelect.value === '当月' ? currentPeriod : periodSelect.value
  request.get('/com/fee/statistics', { params: { period } }).then(res => {
    stats.value = res.data || {}
  })
}

// 默认加载当月统计
loadStats()
</script>
