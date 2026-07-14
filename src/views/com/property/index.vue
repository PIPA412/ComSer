<template>
  <div class="property-container">
    <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
      <el-tab-pane label="楼栋管理" name="building">
        <Building v-if="loadedTabs.building" />
      </el-tab-pane>
      <el-tab-pane label="居民管理" name="owner">
        <Owner v-if="loadedTabs.owner" />
      </el-tab-pane>
      <el-tab-pane label="变更记录" name="changeLog">
        <ChangeLog v-if="loadedTabs.changeLog" />
      </el-tab-pane>
      <el-tab-pane label="统计看板" name="statistics">
        <Statistics v-if="loadedTabs.statistics" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="PropertyIndex">
import Building from './building.vue'
import Owner from './owner.vue'
import ChangeLog from './changeLog.vue'
import Statistics from './statistics.vue'

const activeTab = ref('building')

const loadedTabs = reactive({
  building: true,
  owner: false,
  changeLog: false,
  statistics: false
})

function onTabChange(name) {
  loadedTabs[name] = true
}
</script>

<style scoped>
.property-container { padding: 0; }
</style>
