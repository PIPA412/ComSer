<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属楼栋" prop="buildingId">
        <el-select v-model="queryParams.buildingId" placeholder="请选择楼栋" clearable @change="handleQuery">
          <el-option v-for="item in buildingOptions" :key="item.buildingId" :label="item.buildingName" :value="item.buildingId" />
        </el-select>
      </el-form-item>
      <el-form-item label="单元名称" prop="unitName">
        <el-input v-model="queryParams.unitName" placeholder="请输入单元名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:property:unit:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="unitList">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="单元名称" prop="unitName" />
      <el-table-column label="单元编号" prop="unitCode" />
      <el-table-column label="所属楼栋" prop="buildingId">
        <template #default="scope">{{ getBuildingName(scope.row.buildingId) }}</template>
      </el-table-column>
      <el-table-column label="楼层数" prop="floorCount" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:property:unit:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:property:unit:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="unitRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" filterable>
            <el-option v-for="item in buildingOptions" :key="item.buildingId" :label="item.buildingName" :value="item.buildingId" />
          </el-select>
        </el-form-item>
        <el-form-item label="单元名称" prop="unitName">
          <el-input v-model="form.unitName" placeholder="请输入单元名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="单元编号" prop="unitCode">
          <el-input v-model="form.unitCode" placeholder="请输入单元编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="楼层数">
          <el-input-number v-model="form.floorCount" :min="1" />
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

<script setup name="Unit">
import { listUnit, getUnit, addUnit, updateUnit, delUnit, getAllBuildings } from '@/api/com/property'

const { proxy } = getCurrentInstance()
const unitList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')
const buildingOptions = ref([])
const queryParams = ref({ pageNum: 1, pageSize: 10, buildingId: null, unitName: null })
const form = ref({})
const rules = ref({
  unitName: [{ required: true, message: '单元名称不能为空', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择所属楼栋', trigger: 'change' }]
})

// 加载楼栋选项
function loadBuildings() {
  getAllBuildings().then(res => { buildingOptions.value = res.data || [] })
}
// 根据buildingId查楼栋名称
function getBuildingName(buildingId) {
  const b = buildingOptions.value.find(i => i.buildingId === buildingId)
  return b ? b.buildingName : ''
}

function getList() {
  loading.value = true
  listUnit(queryParams.value).then(res => { unitList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '添加单元' }
function handleUpdate(row) {
  getUnit(row.unitId).then(res => { form.value = res.data; open.value = true; title.value = '修改单元' })
}
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该单元?').then(() =>
    delUnit(row.unitId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
  )
}
function submitForm() {
  unitRef.value?.validate(valid => {
    if (valid) {
      (form.value.unitId ? updateUnit : addUnit)(form.value)
        .then(() => { proxy.$modal.msgSuccess('操作成功'); open.value = false; getList() })
        .catch(() => {})
    }
  })
}
function cancel() { open.value = false; reset() }
function reset() { form.value = { status: '0', floorCount: 1 } }

loadBuildings()
getList()
</script>
