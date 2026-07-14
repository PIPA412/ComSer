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
          <el-option label="户主" value="户主" /><el-option label="家属" value="家属" /><el-option label="租客" value="租客" />
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
          <el-tag :type="scope.row.ownerType === '户主' ? 'success' : scope.row.ownerType === '租客' ? 'warning' : 'info'">{{ scope.row.ownerType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="280">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:property:owner:edit']">修改</el-button>
          <el-button link type="danger" icon="Remove" @click="handleCheckOut(scope.row)" v-hasPermi="['com:property:owner:checkOut']">搬离</el-button>
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
                <el-option label="户主" value="户主" /><el-option label="家属" value="家属" /><el-option label="租客" value="租客" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备用联系方式">
              <el-input v-model="form.backupContact" placeholder="备用联系方式" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
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
    <el-dialog title="居民详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border v-if="detailOwner">
        <el-descriptions-item label="姓名">{{ detailOwner.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailOwner.sex === '0' ? '男' : detailOwner.sex === '1' ? '女' : '未知' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailOwner.phone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailOwner.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="住户类型">
          <el-tag :type="detailOwner.ownerType === '户主' ? 'success' : detailOwner.ownerType === '租客' ? 'warning' : 'info'">{{ detailOwner.ownerType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备用联系方式">{{ detailOwner.backupContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="居住信息" :span="2">
          <template v-if="detailRooms.length > 0">
            <el-tag v-for="room in detailRooms" :key="room.roomId" type="primary" size="small" style="margin:2px 4px">
              {{ room.buildingName }} {{ room.roomNumber }}
              <template v-if="room.isCurrent === '1'">（在住）</template>
            </el-tag>
          </template>
          <span v-else style="color:#909399">暂无</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailOwner.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailOwner.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">{{ detailRooms.length > 0 ? detailRooms[0].buildingName + ' ' + detailRooms[0].roomNumber : '' }} 同住居民</el-divider>
      <div v-if="detailRooms.length > 0">
        <div v-for="room in detailRooms" :key="room.roomId" style="margin-bottom:12px">
          <el-tag type="success" size="small">{{ room.buildingName }} {{ room.roomNumber }}</el-tag>
          <span style="color:#909399;font-size:12px;margin-left:8px">{{ room.isCurrent === '1' ? '在住' : '已搬离' }} · {{ room.relationType }}</span>
          <el-table :data="allResidents[room.roomId] || []" size="small" style="margin-top:4px" v-loading="coResidentLoading">
            <el-table-column label="姓名" prop="ownerName" width="120" />
            <el-table-column label="手机号" prop="phone" width="150" />
            <el-table-column label="类型" prop="ownerType" width="70" />
          </el-table>
          <span v-if="!allResidents[room.roomId] || allResidents[room.roomId].length === 0" style="color:#909399;font-size:12px">加载中...</span>
        </div>
      </div>
      <div v-else style="color:#909399">暂无关联房屋</div>
    </el-dialog>

    <!-- 入住登记对话框（简化版） -->
    <el-dialog title="居民入住登记" v-model="checkInOpen" width="650px" append-to-body>
      <el-form ref="checkInRef" :model="checkInForm" :rules="checkInRules" label-width="90px">
        <el-divider content-position="left">居民信息</el-divider>
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
              <el-input v-model="checkInForm.idCard" placeholder="请输入身份证号" maxlength="18" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="住户类型" prop="ownerType">
              <el-select v-model="checkInForm.ownerType" placeholder="请选择">
                <el-option label="户主" value="户主" /><el-option label="家属" value="家属" /><el-option label="租客" value="租客" />
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
          <el-col :span="12">
            <el-form-item label="楼栋" prop="checkInBuildingId">
              <el-select v-model="checkInForm.checkInBuildingId" placeholder="请选择楼栋" @change="onCheckInBuildingChange" filterable style="width:100%">
                <el-option v-for="item in buildingOptions" :key="item.buildingId" :label="item.buildingName" :value="item.buildingId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门牌号" prop="roomId">
              <el-select v-model="checkInForm.roomId" placeholder="请选择门牌号" :disabled="!checkInForm.checkInBuildingId" filterable style="width:100%">
                <el-option v-for="item in checkInRoomOptions" :key="item.roomId" :label="item.roomNumber" :value="item.roomId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
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
import { listOwner, getOwner, addOwner, updateOwner, getOwnerDetail, ownerCheckIn, ownerCheckOut, getAllBuildings, getRoomByBuilding, getRoomResidents } from '@/api/com/property'

const { proxy } = getCurrentInstance()
const queryRef = ref(null)
const ownerRef = ref(null)
const checkInRef = ref(null)
const ownerList = ref([])
const open = ref(false)
const detailOpen = ref(false)
const checkInOpen = ref(false)
const loading = ref(true)
const detailLoading = ref(false)
const coResidentLoading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const title = ref('')

// 下拉数据
const buildingOptions = ref([])
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
const allResidents = reactive({})

// 入住登记表单（简化版：无单元、无单独身份类型字段）
const checkInForm = ref({ sex: '0', checkInBuildingId: null, roomId: null })
const checkInRules = ref({
  ownerName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  ownerType: [{ required: true, message: '请选择住户类型', trigger: 'change' }],
  idCard: [
    { required: true, message: '身份证号不能为空', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  checkInBuildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择门牌号', trigger: 'change' }]
})

// ======== 数据加载 ========
function loadBuildings() {
  getAllBuildings().then(res => { buildingOptions.value = res.data || [] }).catch(() => {
    proxy.$modal.msgError('加载楼栋列表失败')
  })
}
function getList() {
  loading.value = true
  listOwner(queryParams.value).then(res => { ownerList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false })
}

// ======== 入住登记 - 楼栋→门牌号级联 ========
function onCheckInBuildingChange(val) {
  const buildingId = Array.isArray(val) ? val[0] : val
  checkInForm.value.checkInBuildingId = buildingId
  checkInForm.value.roomId = null
  checkInRoomOptions.value = []
  if (buildingId) {
    getRoomByBuilding(buildingId).then(res => { checkInRoomOptions.value = res.data || [] }).catch(() => {
      proxy.$modal.msgError('加载门牌号失败')
    })
  }
}

// ======== 事件 ========
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '添加居民' }
function handleUpdate(row) {
  getOwner(row.ownerId).then(res => { form.value = res.data; open.value = true; title.value = '修改居民' })
}
function handleDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  detailOwner.value = row
  detailRooms.value = []
  Object.keys(allResidents).forEach(k => delete allResidents[k])
  getOwnerDetail(row.ownerId).then(res => {
    if (res.data && res.data.rooms) {
      detailRooms.value = res.data.rooms.map(r => ({
        ownerRoomId: r.relation ? r.relation.id : null,
        roomId: r.room ? r.room.roomId : null,
        buildingName: r.building ? r.building.buildingName : '',
        roomNumber: r.room ? r.room.roomNumber : '',
        relationType: r.relation ? r.relation.relationType : '',
        checkInDate: r.relation ? r.relation.checkInDate : '',
        checkOutDate: r.relation ? r.relation.checkOutDate : '',
        isCurrent: r.relation ? r.relation.isCurrent : ''
      }))
      // 加载每个房间的全部在住居民
      detailRooms.value.forEach(r => {
        if (r.roomId) {
          coResidentLoading.value = true
          getRoomResidents(r.roomId).then(re => {
            allResidents[r.roomId] = re.data || []
            coResidentLoading.value = false
          }).catch(() => { coResidentLoading.value = false })
        }
      })
    }
    detailLoading.value = false
  }).catch(() => { detailLoading.value = false })
}

function handleCheckIn() {
  checkInForm.value = { sex: '0', checkInBuildingId: null, roomId: null }
  checkInRoomOptions.value = []
  loadBuildings()  // 每次打开入住登记时重新加载楼栋列表
  checkInOpen.value = true
}

// ======== 搬离（列表按钮） ========
function handleCheckOut(row) {
  getOwnerDetail(row.ownerId).then(res => {
    const currentRooms = (res.data?.rooms || []).filter(r => r.relation && r.relation.isCurrent === '1')
    if (currentRooms.length === 0) return
    const room = currentRooms[0]
    const buildingName = room.building ? room.building.buildingName : ''
    const roomNumber = room.room ? room.room.roomNumber : ''
    proxy.$modal.confirm(`确认将【${row.ownerName}】从【${buildingName} ${roomNumber}】办理搬离？`)
      .then(() => {
        ownerCheckOut({ ownerRoomId: room.relation.id, checkOutDate: null }).then(() => {
          proxy.$modal.msgSuccess('搬离成功')
          getList()
        }).catch(() => {})
      })
  }).catch(() => {})
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
      const form = checkInForm.value
      // 修复 Element Plus 在非路由组件中可能将值包装为数组的问题
      const buildingId = Array.isArray(form.checkInBuildingId) ? form.checkInBuildingId[0] : form.checkInBuildingId
      const roomId = Array.isArray(form.roomId) ? form.roomId[0] : form.roomId
      const data = {
        ownerName: form.ownerName,
        phone: form.phone,
        sex: form.sex,
        idCard: form.idCard,
        backupContact: form.backupContact,
        ownerType: form.ownerType,
        relationType: form.ownerType,
        roomId: roomId,
        checkInDate: form.checkInDate
      }
      ownerCheckIn(data)
        .then(() => { proxy.$modal.msgSuccess('入住登记成功'); checkInOpen.value = false; getList() })
        .catch(() => {})
    }
  })
}
function cancel() { open.value = false; reset() }
function reset() { form.value = { sex: '0', status: '0' } }

loadBuildings()
getList()
</script>

<style scoped>
.coresident-list { padding: 4px 8px; }
.coresident-label { color: #606266; font-size: 13px; font-weight: bold; }
</style>
