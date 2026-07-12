<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属楼栋" prop="searchBuildingId">
        <el-select v-model="queryParams.searchBuildingId" placeholder="请选择楼栋" clearable @change="handleBuildingChange">
          <el-option v-for="item in buildingOptions" :key="item.buildingId" :label="item.buildingName" :value="item.buildingId" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属单元" prop="unitId">
        <el-select v-model="queryParams.unitId" placeholder="请选择单元" clearable>
          <el-option v-for="item in unitOptions" :key="item.unitId" :label="item.unitName" :value="item.unitId" />
        </el-select>
      </el-form-item>
      <el-form-item label="房间号" prop="roomNumber">
        <el-input v-model="queryParams.roomNumber" placeholder="请输入房间号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="房屋类型" prop="roomType">
        <el-select v-model="queryParams.roomType" placeholder="房屋类型" clearable>
          <el-option label="住宅" value="住宅" /><el-option label="商铺" value="商铺" /><el-option label="办公" value="办公" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:property:room:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="roomList">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="房间号" prop="roomNumber" />
      <el-table-column label="所属楼栋" prop="unitId">
        <template #default="scope">{{ getBuildingNameByUnit(scope.row.unitId) }}</template>
      </el-table-column>
      <el-table-column label="所属单元" prop="unitId">
        <template #default="scope">{{ getUnitName(scope.row.unitId) }}</template>
      </el-table-column>
      <el-table-column label="房屋类型" prop="roomType" />
      <el-table-column label="面积(㎡)" prop="area" />
      <el-table-column label="使用状态" prop="useStatus" />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:property:room:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:property:room:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="roomRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属楼栋" prop="formBuildingId">
          <el-select v-model="form.formBuildingId" placeholder="请选择楼栋" @change="onFormBuildingChange" filterable>
            <el-option v-for="item in buildingOptions" :key="item.buildingId" :label="item.buildingName" :value="item.buildingId" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属单元" prop="unitId">
          <el-select v-model="form.unitId" placeholder="请选择单元" :disabled="!form.formBuildingId" filterable>
            <el-option v-for="item in formUnitOptions" :key="item.unitId" :label="item.unitName" :value="item.unitId" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="请输入房间号" maxlength="20" />
        </el-form-item>
        <el-form-item label="房屋类型">
          <el-select v-model="form.roomType" placeholder="请选择房屋类型">
            <el-option label="住宅" value="住宅" /><el-option label="商铺" value="商铺" /><el-option label="办公" value="办公" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="area">
          <el-input-number v-model="form.area" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="使用状态">
          <el-select v-model="form.useStatus" placeholder="请选择使用状态">
            <el-option label="空置" value="空置" /><el-option label="自住" value="自住" /><el-option label="出租" value="出租" /><el-option label="未入住" value="未入住" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Room">
import { listRoom, getRoom, addRoom, updateRoom, delRoom, getAllBuildings, getUnitByBuilding } from '@/api/com/property'

const { proxy } = getCurrentInstance()
const roomList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const buildingOptions = ref([])
const unitOptions = ref([])           // 搜索栏用的单元选项
const formUnitOptions = ref([])       // 表单弹窗用的单元选项
const queryParams = ref({ pageNum: 1, pageSize: 10, roomNumber: null, unitId: null, roomType: null, searchBuildingId: null })
const form = ref({})
const rules = ref({
  roomNumber: [{ required: true, message: '房间号不能为空', trigger: 'blur' }],
  unitId: [{ required: true, message: '请选择所属单元', trigger: 'change' }],
  formBuildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }]
})

function loadBuildings() { getAllBuildings().then(res => { buildingOptions.value = res.data || [] }) }

// 搜索栏：楼栋变化时加载单元
function handleBuildingChange(buildingId) {
  queryParams.value.unitId = null
  if (buildingId) {
    getUnitByBuilding(buildingId).then(res => { unitOptions.value = res.data || [] })
  } else {
    unitOptions.value = []
  }
  handleQuery()
}
// 表单：楼栋变化时加载单元
function onFormBuildingChange(buildingId) {
  form.value.unitId = null
  if (buildingId) {
    getUnitByBuilding(buildingId).then(res => { formUnitOptions.value = res.data || [] })
  } else {
    formUnitOptions.value = []
  }
}

function getUnitName(unitId) {
  const all = [...unitOptions.value, ...formUnitOptions.value]
  const u = all.find(i => i.unitId === unitId)
  return u ? u.unitName : ''
}
function getBuildingNameByUnit(unitId) {
  const all = [...unitOptions.value, ...formUnitOptions.value]
  const u = all.find(i => i.unitId === unitId)
  if (u) {
    const b = buildingOptions.value.find(i => i.buildingId === u.buildingId)
    return b ? b.buildingName : ''
  }
  return ''
}

function getList() {
  loading.value = true
  listRoom(queryParams.value).then(res => { roomList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); unitOptions.value = []; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '添加房屋' }
function handleUpdate(row) {
  getRoom(row.roomId).then(res => {
    const data = res.data
    // 如果有unitId，加载级联：先查unit拿到buildingId，再加载单元列表
    if (data.unitId) {
      import('@/api/com/property').then(api => {
        api.getUnit(data.unitId).then(unitRes => {
          const unit = unitRes.data
          if (unit) {
            data.formBuildingId = unit.buildingId
            onFormBuildingChange(unit.buildingId)
          }
          form.value = data
          open.value = true
          title.value = '修改房屋'
        })
      })
    } else {
      form.value = data
      open.value = true
      title.value = '修改房屋'
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除该房屋?').then(() =>
    delRoom(row.roomId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
  )
}
function submitForm() {
  roomRef.value?.validate(valid => {
    if (valid) {
      // 移除 formBuildingId（仅前端用的临时字段）
      const submitData = { ...form.value }
      delete submitData.formBuildingId
      ;(submitData.roomId ? updateRoom : addRoom)(submitData)
        .then(() => { proxy.$modal.msgSuccess('操作成功'); open.value = false; getList() })
        .catch(() => {})
    }
  })
}
function cancel() { open.value = false; reset() }
function reset() { form.value = { status: '0', formBuildingId: null, unitId: null } }

loadBuildings()
getList()
</script>
