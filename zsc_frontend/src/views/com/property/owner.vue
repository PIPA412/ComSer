<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="姓名" prop="ownerName">
        <el-input v-model="queryParams.ownerName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
      </el-form-item>
      <el-form-item label="住户类型" prop="ownerType">
        <el-select v-model="queryParams.ownerType" placeholder="住户类型" clearable>
          <el-option label="业主" value="业主" /><el-option label="租户" value="租户" /><el-option label="家属" value="家属" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:property:owner:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Upload" @click="handleCheckIn" v-hasPermi="['com:property:owner:add']">入住登记</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="ownerList">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="姓名" prop="ownerName" />
      <el-table-column label="性别" prop="sex" width="60">
        <template #default="scope">{{ scope.row.sex === '0' ? '男' : scope.row.sex === '1' ? '女' : '未知' }}</template>
      </el-table-column>
      <el-table-column label="手机号" prop="phone" width="120" />
      <el-table-column label="身份证号" prop="idCard" width="180">
        <template #default="scope">{{ scope.row.idCard ? scope.row.idCard.replace(/^(.{3})(.*)(.{4})$/, '$1****$3') : '' }}</template>
      </el-table-column>
      <el-table-column label="住户类型" prop="ownerType" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.ownerType === '业主' ? 'success' : scope.row.ownerType === '租户' ? 'warning' : 'info'">{{ scope.row.ownerType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="270">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:property:owner:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:property:owner:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="650px" append-to-body>
      <el-form ref="ownerRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="ownerName">
              <el-input v-model="form.ownerName" placeholder="请输入姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio label="0">男</el-radio><el-radio label="1">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="住户类型" prop="ownerType">
              <el-select v-model="form.ownerType" placeholder="请选择住户类型">
                <el-option label="业主" value="业主" /><el-option label="租户" value="租户" /><el-option label="家属" value="家属" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备用联系方式">
              <el-input v-model="form.backupContact" placeholder="备用联系方式" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" v-if="form.ownerId">
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

    <!-- 详情对话框 -->
    <el-dialog title="住户详情" v-model="detailOpen" width="750px" append-to-body>
      <el-descriptions :column="2" border v-if="detailOwner">
        <el-descriptions-item label="姓名">{{ detailOwner.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailOwner.sex === '0' ? '男' : detailOwner.sex === '1' ? '女' : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailOwner.phone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailOwner.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="住户类型">
          <el-tag :type="detailOwner.ownerType === '业主' ? 'success' : detailOwner.ownerType === '租户' ? 'warning' : 'info'">{{ detailOwner.ownerType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备用联系方式">{{ detailOwner.backupContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="sys_normal_disable" :value="detailOwner.status" />
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailOwner.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailOwner.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">关联房屋</el-divider>
      <el-table :data="detailRooms" v-loading="detailLoading" max-height="300">
        <el-table-column label="楼栋" prop="buildingName" />
        <el-table-column label="单元" prop="unitName" />
        <el-table-column label="房间号" prop="roomNumber" />
        <el-table-column label="面积(㎡)" prop="area" />
        <el-table-column label="关联类型" prop="relationType" />
        <el-table-column label="入住日期" prop="checkInDate" />
        <el-table-column label="是否当前" prop="isCurrent">
          <template #default="scope">{{ scope.row.isCurrent === '1' ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 入住登记对话框 -->
    <el-dialog title="住户入住登记" v-model="checkInOpen" width="700px" append-to-body>
      <el-form ref="checkInRef" :model="checkInForm" :rules="checkInRules" label-width="90px">
        <el-divider content-position="left">住户信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="ownerName">
              <el-input v-model="checkInForm.ownerName" placeholder="请输入姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="checkInForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="checkInForm.sex">
                <el-radio label="0">男</el-radio><el-radio label="1">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="checkInForm.idCard" placeholder="选填" maxlength="18" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="住户类型" prop="ownerType">
              <el-select v-model="checkInForm.ownerType" placeholder="请选择">
                <el-option label="业主" value="业主" /><el-option label="租户" value="租户" /><el-option label="家属" value="家属" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备用联系方式">
              <el-input v-model="checkInForm.backupContact" placeholder="选填" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">房屋信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="楼栋" prop="checkInBuildingId">
              <el-select v-model="checkInForm.checkInBuildingId" placeholder="请选择楼栋" @change="onCheckInBuildingChange" filterable>
                <el-option v-for="item in buildingOptions" :key="item.buildingId" :label="item.buildingName" :value="item.buildingId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单元" prop="checkInUnitId">
              <el-select v-model="checkInForm.checkInUnitId" placeholder="请选择单元" @change="onCheckInUnitChange" :disabled="!checkInForm.checkInBuildingId" filterable>
                <el-option v-for="item in checkInUnitOptions" :key="item.unitId" :label="item.unitName" :value="item.unitId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="房屋" prop="roomId">
              <el-select v-model="checkInForm.roomId" placeholder="请选择房屋" :disabled="!checkInForm.checkInUnitId" filterable>
                <el-option v-for="item in checkInRoomOptions" :key="item.roomId" :label="item.roomNumber" :value="item.roomId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联类型">
              <el-select v-model="checkInForm.relationType" placeholder="默认同住户类型">
                <el-option label="产权人" value="产权人" /><el-option label="租户" value="租户" /><el-option label="家属" value="家属" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住日期">
              <el-date-picker v-model="checkInForm.checkInDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitCheckIn">确 定</el-button>
        <el-button @click="checkInOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Owner">
import { listOwner, getOwner, addOwner, updateOwner, delOwner, getOwnerDetail, ownerCheckIn, getAllBuildings, getUnitByBuilding, getRoomByUnit } from '@/api/com/property'

const { proxy } = getCurrentInstance()
const ownerList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const checkInOpen = ref(false)
const loading = ref(true)
const detailLoading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')

// 通用数据
const buildingOptions = ref([])
const checkInUnitOptions = ref([])
const checkInRoomOptions = ref([])

// 查询参数
const queryParams = ref({ pageNum: 1, pageSize: 10, ownerName: null, phone: null, ownerType: null })

// 新增/编辑表单
const form = ref({})
const rules = ref({
  ownerName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  ownerType: [{ required: true, message: '请选择住户类型', trigger: 'change' }],
  idCard: [{ pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }]
})

// 详情
const detailOwner = ref(null)
const detailRooms = ref([])

// 入住登记表单
const checkInForm = ref({ sex: '0', checkInBuildingId: null, checkInUnitId: null, roomId: null })
const checkInRules = ref({
  ownerName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  ownerType: [{ required: true, message: '请选择住户类型', trigger: 'change' }],
  idCard: [{ pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }],
  checkInBuildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  checkInUnitId: [{ required: true, message: '请选择单元', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房屋', trigger: 'change' }]
})

// ======== 数据加载 ========
function loadBuildings() { getAllBuildings().then(res => { buildingOptions.value = res.data || [] }) }
function getList() {
  loading.value = true
  listOwner(queryParams.value).then(res => { ownerList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false })
}

// ======== 入住登记 - 级联选择 ========
function onCheckInBuildingChange(buildingId) {
  checkInForm.value.checkInUnitId = null
  checkInForm.value.roomId = null
  checkInUnitOptions.value = []
  checkInRoomOptions.value = []
  if (buildingId) {
    getUnitByBuilding(buildingId).then(res => { checkInUnitOptions.value = res.data || [] })
  }
}
function onCheckInUnitChange(unitId) {
  checkInForm.value.roomId = null
  checkInRoomOptions.value = []
  if (unitId) {
    getRoomByUnit(unitId).then(res => { checkInRoomOptions.value = res.data || [] })
  }
}

// ======== 事件 ========
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '添加住户' }
function handleUpdate(row) {
  getOwner(row.ownerId).then(res => { form.value = res.data; open.value = true; title.value = '修改住户' })
}
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该住户?').then(() =>
    delOwner(row.ownerId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
  )
}
function handleDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  detailOwner.value = row
  detailRooms.value = []
  getOwnerDetail(row.ownerId).then(res => {
    if (res.data && res.data.rooms) {
      detailRooms.value = res.data.rooms.map(r => ({
        buildingName: r.building ? r.building.buildingName : '',
        unitName: r.unit ? r.unit.unitName : '',
        roomNumber: r.room ? r.room.roomNumber : '',
        area: r.room ? r.room.area : '',
        relationType: r.relation ? r.relation.relationType : '',
        checkInDate: r.relation ? r.relation.checkInDate : '',
        isCurrent: r.relation ? r.relation.isCurrent : ''
      }))
    }
    detailLoading.value = false
  }).catch(() => { detailLoading.value = false })
}
function handleCheckIn() {
  checkInForm.value = { sex: '0', checkInBuildingId: null, checkInUnitId: null, roomId: null }
  checkInUnitOptions.value = []
  checkInRoomOptions.value = []
  checkInOpen.value = true
}
function submitForm() {
  ownerRef.value?.validate(valid => {
    if (valid) {
      (form.value.ownerId ? updateOwner : addOwner)(form.value)
        .then(() => { proxy.$modal.msgSuccess('操作成功'); open.value = false; getList() })
        .catch(() => {})
    }
  })
}
function submitCheckIn() {
  checkInRef.value?.validate(valid => {
    if (valid) {
      ownerCheckIn(checkInForm.value)
        .then(res => {
          proxy.$modal.msgSuccess('入住登记成功')
          checkInOpen.value = false
          getList()
        })
        .catch(() => {})
    }
  })
}
function cancel() { open.value = false; reset() }
function reset() { form.value = { sex: '0', status: '0' } }

loadBuildings()
getList()
</script>
