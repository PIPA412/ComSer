<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="服务商" name="provider">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddProvider" v-hasPermi="['com:convenience:provider:add']">新增服务商</el-button></el-col></el-row>
        <el-table v-loading="loading1" :data="providerList">
          <el-table-column label="名称" prop="providerName" /><el-table-column label="服务类型" prop="serviceType" /><el-table-column label="联系人" prop="contactName" /><el-table-column label="电话" prop="contactPhone" />
          <el-table-column label="操作" width="180"><template #default="scope"><el-button link type="primary" icon="Edit" @click="handleUpdateProvider(scope.row)" v-hasPermi="['com:convenience:provider:edit']">修改</el-button><el-button link type="primary" icon="Delete" @click="handleDeleteProvider(scope.row)" v-hasPermi="['com:convenience:provider:remove']">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="服务项目" name="item">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddItem" v-hasPermi="['com:convenience:item:add']">新增项目</el-button></el-col></el-row>
        <el-table v-loading="loading2" :data="itemList">
          <el-table-column label="名称" prop="itemName" /><el-table-column label="价格(元)" prop="price" /><el-table-column label="时长(分钟)" prop="duration" /><el-table-column label="预约方式" prop="bookingMethod" />
          <el-table-column label="操作" width="180"><template #default="scope"><el-button link type="primary" icon="Edit" @click="handleUpdateItem(scope.row)" v-hasPermi="['com:convenience:item:edit']">修改</el-button><el-button link type="primary" icon="Delete" @click="handleDeleteItem(scope.row)" v-hasPermi="['com:convenience:item:remove']">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="服务订单" name="order">
        <el-table v-loading="loading3" :data="orderList">
          <el-table-column label="订单编号" prop="orderNo" /><el-table-column label="金额" prop="amount" /><el-table-column label="预约时间" prop="bookingTime" /><el-table-column label="状态" prop="status" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="ConvenienceManagement">
import { listProvider, listServiceItem, listServiceOrder } from '@/api/com/convenience'
const activeTab = ref('provider'); const providerList = ref([]); const itemList = ref([]); const orderList = ref([]); const loading1 = ref(true); const loading2 = ref(true); const loading3 = ref(true)
listProvider({ pageNum: 1, pageSize: 100 }).then(r => { providerList.value = r.rows; loading1.value = false }).catch(() => { loading1.value = false })
listServiceItem({ pageNum: 1, pageSize: 100 }).then(r => { itemList.value = r.rows; loading2.value = false }).catch(() => { loading2.value = false })
listServiceOrder({ pageNum: 1, pageSize: 100 }).then(r => { orderList.value = r.rows; loading3.value = false }).catch(() => { loading3.value = false })
function handleAddProvider() { /* TODO */ }
function handleAddItem() { /* TODO */ }
function handleUpdateProvider(row) { /* TODO */ }
function handleDeleteProvider(row) { /* TODO */ }
function handleUpdateItem(row) { /* TODO */ }
function handleDeleteItem(row) { /* TODO */ }
</script>
