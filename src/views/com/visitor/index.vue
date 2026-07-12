<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="访客姓名" prop="visitorName">
        <el-input v-model="queryParams.visitorName" placeholder="请输入访客姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="visitorPhone">
        <el-input v-model="queryParams.visitorPhone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="访客状态" clearable>
          <el-option label="待审批" value="待审批" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已拒绝" value="已拒绝" />
          <el-option label="已签离" value="已签离" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:visitor:add']">新增访客</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="visitorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="访客姓名" prop="visitorName" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" prop="visitorPhone" width="120" />
      <el-table-column label="被访房屋" prop="roomId" width="100" />
      <el-table-column label="来访事由" prop="reason" :show-overflow-tooltip="true" />
      <el-table-column label="预计到访" prop="expectedTime" width="160" />
      <el-table-column label="到访时间" prop="arrivalTime" width="160" />
      <el-table-column label="签离时间" prop="leaveTime" width="160" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="320" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:visitor:edit']">修改</el-button>
          <el-button v-if="scope.row.status === '待审批'" link type="success" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['com:visitor:approve']">通过</el-button>
          <el-button v-if="scope.row.status === '待审批'" link type="danger" icon="Close" @click="handleReject(scope.row)" v-hasPermi="['com:visitor:approve']">拒绝</el-button>
          <el-button v-if="scope.row.status === '已通过'" link type="success" icon="CircleCheck" @click="handleCheckout(scope.row)" v-hasPermi="['com:visitor:checkout']">签离</el-button>
          <el-button link type="primary" icon="View" @click="handleViewRecords(scope.row)" v-hasPermi="['com:visitor:record:list']">记录</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:visitor:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="visitorRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="访客姓名" prop="visitorName">
              <el-input v-model="form.visitorName" placeholder="请输入访客姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="visitorPhone">
              <el-input v-model="form.visitorPhone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="被访房屋" prop="roomId">
              <el-input-number v-model="form.roomId" :min="1" placeholder="请输入房屋ID" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计到访" prop="expectedTime">
              <el-date-picker v-model="form.expectedTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="来访事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入来访事由" maxlength="200" />
        </el-form-item>
        <el-form-item label="状态" v-if="form.visitorId">
          <el-radio-group v-model="form.status">
            <el-radio label="待审批">待审批</el-radio>
            <el-radio label="已通过">已通过</el-radio>
            <el-radio label="已拒绝">已拒绝</el-radio>
            <el-radio label="已签离">已签离</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 通行记录子表 -->
    <el-dialog title="通行记录" v-model="recordOpen" width="800px" append-to-body>
      <el-table v-loading="recordLoading" :data="recordList">
        <el-table-column label="通行类型" prop="passType" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.passType === '入园' ? 'success' : 'warning'">{{ scope.row.passType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通行时间" prop="passTime" width="180" />
        <el-table-column label="门禁设备" prop="gateDevice" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
      </el-table>
      <pagination v-show="recordTotal > 0" :total="recordTotal" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" @pagination="getRecordList" />
    </el-dialog>
  </div>
</template>

<script setup name="VisitorManagement">
import { listVisitor, getVisitor, addVisitor, updateVisitor, delVisitor, approveVisitor, rejectVisitor, checkoutVisitor, listVisitorRecord } from '@/api/com/visitor'
import { getCurrentInstance, ref, reactive, toRefs } from 'vue'

const { proxy } = getCurrentInstance()

const visitorList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, visitorName: undefined, visitorPhone: undefined, status: undefined },
  rules: {
    visitorName: [{ required: true, message: '访客姓名不能为空', trigger: 'blur' }],
    visitorPhone: [
      { required: true, message: '手机号不能为空', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    expectedTime: [
      { required: true, message: '预计到访时间不能为空', trigger: 'change' },
      { validator: validateExpectedTime, trigger: 'change' }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 自定义校验：预计到访时间不能早于当前时间
function validateExpectedTime(rule, value, callback) {
  if (value && new Date(value).getTime() < Date.now()) {
    callback(new Error('预计到访时间不能早于当前时间'))
  } else {
    callback()
  }
}

// 通行记录
const recordOpen = ref(false)
const recordLoading = ref(false)
const recordList = ref([])
const recordTotal = ref(0)
const currentVisitorId = ref(null)
const recordQuery = ref({ pageNum: 1, pageSize: 10 })

function statusTagType(status) {
  const map = { '待审批': 'warning', '已通过': 'success', '已拒绝': 'danger', '已签离': 'info' }
  return map[status] || 'info'
}

function getList() {
  loading.value = true
  listVisitor(queryParams.value).then(response => {
    visitorList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.visitorId)
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增访客'
}

function handleUpdate(row) {
  getVisitor(row.visitorId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改访客'
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除访客"' + row.visitorName + '"？').then(function() {
    return delVisitor(row.visitorId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleApprove(row) {
  proxy.$modal.confirm('确认审批通过该访客？').then(() => {
    approveVisitor(row.visitorId).then(() => {
      proxy.$modal.msgSuccess('审批通过')
      getList()
    })
  })
}

function handleReject(row) {
  proxy.$modal.confirm('确认拒绝该访客申请？').then(() => {
    rejectVisitor(row.visitorId).then(() => {
      proxy.$modal.msgSuccess('已拒绝')
      getList()
    })
  })
}

function handleCheckout(row) {
  proxy.$modal.confirm('确认签离该访客？').then(() => {
    checkoutVisitor(row.visitorId).then(() => {
      proxy.$modal.msgSuccess('已签离')
      getList()
    })
  })
}

function submitForm() {
  proxy.$refs.visitorRef.validate(valid => {
    if (valid) {
      if (form.value.visitorId != undefined) {
        updateVisitor(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        }).catch(() => {})
      } else {
        addVisitor(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        }).catch(() => {})
      }
    }
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    visitorId: undefined,
    visitorName: undefined,
    visitorPhone: undefined,
    roomId: undefined,
    expectedTime: undefined,
    reason: undefined,
    status: '待审批',
    remark: undefined
  }
  proxy.resetForm('visitorRef')
}

function handleViewRecords(row) {
  currentVisitorId.value = row.visitorId
  recordQuery.value.pageNum = 1
  recordOpen.value = true
  getRecordList()
}

function getRecordList() {
  recordLoading.value = true
  listVisitorRecord({ ...recordQuery.value, visitorId: currentVisitorId.value }).then(response => {
    recordList.value = response.rows
    recordTotal.value = response.total
    recordLoading.value = false
  }).catch(() => { recordLoading.value = false })
}

getList()
</script>
