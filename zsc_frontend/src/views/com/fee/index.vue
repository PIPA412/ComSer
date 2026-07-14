<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="费用项目" name="item">
        <fee-item />
      </el-tab-pane>
      <el-tab-pane label="账单管理" name="bill">
        <fee-bill />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup name="FeeManagement">
import FeeItem from './item.vue'
import FeeBill from './bill.vue'
const activeTab = ref('item')
</script>
