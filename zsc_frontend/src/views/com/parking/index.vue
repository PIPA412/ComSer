<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="车位管理" name="spot">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddSpot" v-hasPermi="['com:parking:spot:add']">新增车位</el-button></el-col></el-row>
        <el-table v-loading="loading1" :data="spotList">
          <el-table-column label="车位编号" prop="spotCode" /><el-table-column label="位置" prop="location" /><el-table-column label="类型" prop="spotType" /><el-table-column label="状态" prop="status" /><el-table-column label="月租费" prop="monthlyFee" />
          <el-table-column label="操作" width="180"><template #default="scope"><el-button link type="primary" icon="Edit" @click="handleUpdateSpot(scope.row)" v-hasPermi="['com:parking:spot:edit']">修改</el-button><el-button link type="primary" icon="Delete" @click="handleDeleteSpot(scope.row)" v-hasPermi="['com:parking:spot:remove']">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="车辆管理" name="vehicle">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddVehicle" v-hasPermi="['com:parking:vehicle:add']">新增车辆</el-button></el-col></el-row>
        <el-table v-loading="loading2" :data="vehicleList">
          <el-table-column label="车牌号" prop="plateNumber" /><el-table-column label="品牌" prop="brand" /><el-table-column label="类型" prop="vehicleType" /><el-table-column label="颜色" prop="color" />
          <el-table-column label="操作" width="180"><template #default="scope"><el-button link type="primary" icon="Edit" @click="handleUpdateVehicle(scope.row)" v-hasPermi="['com:parking:vehicle:edit']">修改</el-button><el-button link type="primary" icon="Delete" @click="handleDeleteVehicle(scope.row)" v-hasPermi="['com:parking:vehicle:remove']">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="ParkingManagement">
import { listSpot, listVehicle } from '@/api/com/parking'
const activeTab = ref('spot'); const spotList = ref([]); const vehicleList = ref([]); const loading1 = ref(true); const loading2 = ref(true)
listSpot({ pageNum: 1, pageSize: 100 }).then(r => { spotList.value = r.rows; loading1.value = false }).catch(() => { loading1.value = false })
listVehicle({ pageNum: 1, pageSize: 100 }).then(r => { vehicleList.value = r.rows; loading2.value = false }).catch(() => { loading2.value = false })
function handleAddSpot() { /* TODO */ }
function handleAddVehicle() { /* TODO */ }
function handleUpdateSpot(row) { /* TODO */ }
function handleDeleteSpot(row) { /* TODO */ }
function handleUpdateVehicle(row) { /* TODO */ }
function handleDeleteVehicle(row) { /* TODO */ }
</script>
