<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="楼栋名称" prop="buildingName">
        <el-input v-model="queryParams.buildingName" placeholder="请输入楼栋名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="楼栋状态" clearable>
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAddBuilding" v-hasPermi="['com:property:building:add']">新增楼栋</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 楼栋表格（可展开查看单元和门牌号） -->
    <el-table v-loading="loading" :data="buildingList" @selection-change="handleSelectionChange" @expand-change="onExpandChange" row-key="buildingId">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="expand">
        <template #default="scope">
          <div class="expand-container">
            <div class="expand-header">
              <span class="expand-title">{{ scope.row.buildingName }} — 单元及门牌号</span>
              <el-button type="primary" size="small" icon="Plus" @click="handleAddUnit(scope.row)" v-hasPermi="['com:property:unit:add']">新增单元</el-button>
            </div>
            <el-table :data="unitMap[scope.row.buildingId] || []" v-loading="unitLoading[scope.row.buildingId]" size="small" row-key="unitId">
              <el-table-column type="expand">
                <template #default="uScope">
                  <div class="room-list">
                    <span class="room-label">门牌号：</span>
                    <el-tag v-for="r in (roomMap[uScope.row.unitId] || [])" :key="r.roomId" size="small" style="margin:2px 4px">{{ r.roomNumber }}</el-tag>
                    <span v-if="!(roomMap[uScope.row.unitId] || []).length" style="color:#909399">暂无门牌号</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单元编号" prop="unitCode" />
              <el-table-column label="单元名称" prop="unitName" />
              <el-table-column label="楼层数" prop="floorCount" />
              <el-table-column label="状态" prop="status">
                <template #default="uScope"><dict-tag :options="sys_normal_disable" :value="uScope.row.status" /></template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template #default="uScope">
                  <el-button link type="primary" icon="Edit" size="small" @click="handleUpdateUnit(uScope.row, scope.row)" v-hasPermi="['com:property:unit:edit']">修改</el-button>
                  <el-button link type="primary" icon="Delete" size="small" @click="handleDeleteUnit(uScope.row)" v-hasPermi="['com:property:unit:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="楼栋编号" prop="buildingCode" />
      <el-table-column label="楼栋名称" prop="buildingName" />
      <el-table-column label="楼层数" prop="floorCount" />
      <el-table-column label="地址" prop="address" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdateBuilding(scope.row)" v-hasPermi="['com:property:building:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDeleteBuilding(scope.row)" v-hasPermi="['com:property:building:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 楼栋新增/编辑对话框 -->
    <el-dialog :title="buildingTitle" v-model="buildingOpen" width="650px" append-to-body>
      <el-form ref="buildingRef" :model="buildingForm" :rules="buildingRules" label-width="80px">
        <el-form-item label="楼栋名称" prop="buildingName">
          <el-input v-model="buildingForm.buildingName" placeholder="请输入楼栋名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="楼栋编号" prop="buildingCode">
          <el-input v-model="buildingForm.buildingCode" placeholder="请输入楼栋编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="楼层数" prop="floorCount">
          <el-input-number v-model="buildingForm.floorCount" :min="1" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="buildingForm.address" placeholder="请输入地址" maxlength="255" />
        </el-form-item>
        <!-- 新增楼栋时配置门牌号生成规则 -->
        <template v-if="!buildingForm.buildingId">
          <el-divider content-position="left">门牌号规则</el-divider>
          <el-form-item label="楼栋类型">
            <el-radio-group v-model="buildingForm.buildingType" @change="onBuildingTypeChange">
              <el-radio label="商品楼">商品楼</el-radio>
              <el-radio label="独栋别墅">独栋别墅</el-radio>
            </el-radio-group>
          </el-form-item>
          <template v-if="buildingForm.buildingType === '商品楼'">
            <el-form-item label="每层户数">
              <el-input-number v-model="buildingForm.roomsPerFloor" :min="1" :max="20" />
            </el-form-item>
          </template>
          <template v-if="buildingForm.buildingType === '独栋别墅'">
            <el-form-item label="门牌号">
              <el-input v-model="buildingForm.villaRoomNumber" placeholder="留空默认使用楼栋名称" maxlength="20" />
            </el-form-item>
          </template>
        </template>
        <el-form-item label="状态">
          <el-radio-group v-model="buildingForm.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="buildingForm.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitBuilding">确 定</el-button>
        <el-button @click="buildingOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 单元新增/编辑对话框 -->
    <el-dialog :title="unitTitle" v-model="unitOpen" width="550px" append-to-body>
      <el-form ref="unitRef" :model="unitForm" :rules="unitRules" label-width="100px">
        <el-form-item label="所属楼栋">
          <el-input :value="currentBuildingName" disabled />
        </el-form-item>
        <el-form-item label="单元名称" prop="unitName">
          <el-input v-model="unitForm.unitName" placeholder="请输入单元名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="单元编号" prop="unitCode">
          <el-input v-model="unitForm.unitCode" placeholder="请输入单元编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="楼层数" prop="floorCount">
          <el-input-number v-model="unitForm.floorCount" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="unitForm.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="unitForm.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitUnit">确 定</el-button>
        <el-button @click="unitOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Building">
import { listBuilding, getBuilding, addBuilding, addBuildingWithRooms, updateBuilding, delBuilding, listUnit, getUnit, addUnit, updateUnit, delUnit, getRoomByUnit } from '@/api/com/property'
import { reactive } from 'vue'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')

const queryRef = ref(null)
const buildingRef = ref(null)
const unitRef = ref(null)

const buildingList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const buildingTitle = ref('')
const buildingOpen = ref(false)
const queryParams = ref({ pageNum: 1, pageSize: 10, buildingName: null, status: null })

// 单元和门牌号缓存
const unitMap = reactive({})
const unitLoading = reactive({})
const roomMap = reactive({})

const unitOpen = ref(false)
const unitTitle = ref('')
const currentBuildingId = ref(null)
const currentBuildingName = ref('')

function onBuildingTypeChange() {}

// 楼栋表单
const buildingForm = ref({})
const buildingRules = ref({
  buildingName: [{ required: true, message: '楼栋名称不能为空', trigger: 'blur' }],
  floorCount: [{ required: true, message: '楼层数不能为空', trigger: 'blur' }]
})

// 单元表单
const unitForm = ref({})
const unitRules = ref({
  unitName: [{ required: true, message: '单元名称不能为空', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择所属楼栋', trigger: 'change' }]
})

// ========== 楼栋操作 ==========
function getList() {
  loading.value = true
  listBuilding(queryParams.value).then(res => {
    buildingList.value = res.rows; total.value = res.total; loading.value = false
  }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAddBuilding() {
  buildingForm.value = { status: '0', floorCount: 6, buildingType: '商品楼', roomsPerFloor: 5, villaRoomNumber: '' }
  buildingTitle.value = '添加楼栋'
  buildingOpen.value = true
}
function handleUpdateBuilding(row) {
  getBuilding(row.buildingId).then(res => {
    buildingForm.value = res.data
    buildingTitle.value = '修改楼栋'
    buildingOpen.value = true
  })
}
function handleDeleteBuilding(row) {
  proxy.$modal.confirm('确认删除该楼栋？其下的单元和门牌号也将删除。').then(() =>
    delBuilding(row.buildingId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
  )
}
function submitBuilding() {
  buildingRef.value?.validate(valid => {
    if (valid) {
      // 新增时自动生成门牌号
      if (!buildingForm.value.buildingId) {
        let roomNumbers = ''
        if (buildingForm.value.buildingType === '独栋别墅') {
          roomNumbers = buildingForm.value.villaRoomNumber || buildingForm.value.buildingName || '1'
        } else {
          const floors = buildingForm.value.floorCount || 1
          const perFloor = buildingForm.value.roomsPerFloor || 5
          const rooms = []
          for (let f = 1; f <= floors; f++) {
            for (let r = 1; r <= perFloor; r++) {
              rooms.push(`${f}${String(r).padStart(2, '0')}`)
            }
          }
          roomNumbers = rooms.join(',')
        }
        buildingForm.value.roomNumbers = roomNumbers
        buildingForm.value.unitName = '1单元'
        addBuildingWithRooms(buildingForm.value).then(res => {
          proxy.$modal.msgSuccess(`楼栋创建成功，自动生成${res.data.roomCount}个门牌号`)
          buildingOpen.value = false
          getList()
        }).catch(() => {})
      } else {
        (buildingForm.value.buildingId ? updateBuilding : addBuilding)(buildingForm.value)
          .then(() => { proxy.$modal.msgSuccess('操作成功'); buildingOpen.value = false; getList() })
          .catch(() => {})
      }
    }
  })
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.buildingId) }

// ========== 展开加载 ==========
function onExpandChange(row, expandedRows) {
  const isExpanded = expandedRows.some(r => r.buildingId === row.buildingId)
  if (isExpanded) loadUnits(row.buildingId)
}
function loadUnits(buildingId) {
  if (unitMap[buildingId] || unitLoading[buildingId]) return
  unitLoading[buildingId] = true
  listUnit({ buildingId, pageNum: 1, pageSize: 999 }).then(res => {
    const units = res.rows || []
    unitMap[buildingId] = units
    unitLoading[buildingId] = false
    // 预加载每个单元的门牌号
    units.forEach(u => { loadRooms(u.unitId) })
  }).catch(() => { unitLoading[buildingId] = false })
}
function loadRooms(unitId) {
  if (roomMap[unitId]) return
  getRoomByUnit(unitId).then(res => {
    roomMap[unitId] = res.data || []
  }).catch(() => {})
}

// ========== 单元操作 ==========
function handleAddUnit(building) {
  unitForm.value = { buildingId: building.buildingId, status: '0', floorCount: 1 }
  currentBuildingId.value = building.buildingId
  currentBuildingName.value = building.buildingName
  unitTitle.value = '添加单元'
  unitOpen.value = true
}
function handleUpdateUnit(row, building) {
  getUnit(row.unitId).then(res => {
    unitForm.value = res.data
    currentBuildingId.value = building ? building.buildingId : row.buildingId
    currentBuildingName.value = building ? building.buildingName : ''
    unitTitle.value = '修改单元'
    unitOpen.value = true
  })
}
function handleDeleteUnit(row) {
  proxy.$modal.confirm('确认删除该单元？其下的门牌号也将删除。').then(() =>
    delUnit(row.unitId).then(() => {
      proxy.$modal.msgSuccess('删除成功')
      const bId = row.buildingId
      delete unitMap[bId]
      loadUnits(bId)
    }).catch(() => {})
  )
}
function submitUnit() {
  unitRef.value?.validate(valid => {
    if (valid) {
      (unitForm.value.unitId ? updateUnit : addUnit)(unitForm.value).then(() => {
        proxy.$modal.msgSuccess('操作成功')
        unitOpen.value = false
        const bId = unitForm.value.buildingId
        delete unitMap[bId]
        loadUnits(bId)
      }).catch(() => {})
    }
  })
}

getList()
</script>

<style scoped>
.expand-container { padding: 8px 16px; background: #fafafa; }
.expand-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.expand-title { font-weight: bold; color: #303133; }
.room-list { padding: 4px 8px; }
.room-label { color: #606266; font-size: 13px; }
</style>
