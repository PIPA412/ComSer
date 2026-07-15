<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- ==================== 服务商 Tab ==================== -->
      <el-tab-pane label="服务商管理" name="provider">
        <!-- 搜索栏 -->
        <el-form :model="queryParams1" ref="queryRef1" :inline="true" v-show="showSearch1" label-width="80px">
          <el-form-item label="名称" prop="providerName">
            <el-input v-model="queryParams1.providerName" placeholder="请输入服务商名称" clearable @keyup.enter="handleQuery1" />
          </el-form-item>
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="queryParams1.contactName" placeholder="请输入联系人" clearable @keyup.enter="handleQuery1" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams1.status" placeholder="状态" clearable>
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery1">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery1">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 操作栏 -->
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd1" v-hasPermi="['com:convenience:provider:add']">新增</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch1" @queryTable="getList1" />
        </el-row>
        <!-- 数据表格 -->
        <el-table v-loading="loading1" :data="providerList">
          <el-table-column label="名称" prop="providerName" min-width="140" />
          <el-table-column label="联系人" prop="contactName" width="100" />
          <el-table-column label="电话" prop="contactPhone" width="120" />
          <el-table-column label="地址" prop="address" show-overflow-tooltip min-width="160" />
          <el-table-column label="入驻时间" prop="settleDate" width="170" />
          <el-table-column label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="180" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate1(scope.row)" v-hasPermi="['com:convenience:provider:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete1(scope.row)" v-hasPermi="['com:convenience:provider:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total1 > 0" :total="total1" v-model:page="queryParams1.pageNum" v-model:limit="queryParams1.pageSize" @pagination="getList1" />

        <!-- 服务商弹窗 -->
        <el-dialog :title="title1" v-model="open1" width="600px" append-to-body>
          <el-form ref="formRef1" :model="form1" :rules="rules1" label-width="100px">
            <el-form-item label="服务商名称" prop="providerName">
              <el-input v-model="form1.providerName" placeholder="请输入服务商名称" maxlength="100" />
            </el-form-item>
            <el-form-item label="服务类型" prop="serviceType">
              <el-input v-model="form1.serviceType" placeholder="请输入服务类型（如：家政、维修）" maxlength="50" />
            </el-form-item>
            <el-row>
              <el-col :span="12">
                <el-form-item label="联系人" prop="contactName">
                  <el-input v-model="form1.contactName" placeholder="请输入联系人" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="form1.contactPhone" placeholder="请输入联系电话" maxlength="20" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="地址" prop="address">
              <el-input v-model="form1.address" placeholder="请输入地址" maxlength="255" />
            </el-form-item>
            <el-row>
              <el-col :span="12">
                <el-form-item label="入驻时间" prop="settleDate">
                  <el-date-picker v-model="form1.settleDate" type="datetime" placeholder="选择入驻时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态">
                  <el-radio-group v-model="form1.status">
                    <el-radio v-for="dict in [{label:'启用',value:'0'},{label:'停用',value:'1'}]" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="描述" prop="description">
              <el-input v-model="form1.description" type="textarea" placeholder="请输入服务商描述" maxlength="500" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form1.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button type="primary" @click="submitForm1">确 定</el-button>
              <el-button @click="cancel1">取 消</el-button>
            </div>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ==================== 服务项目 Tab ==================== -->
      <el-tab-pane label="服务项目管理" name="item">
        <el-form :model="queryParams2" ref="queryRef2" :inline="true" v-show="showSearch2" label-width="80px">
          <el-form-item label="名称" prop="itemName">
            <el-input v-model="queryParams2.itemName" placeholder="请输入服务名称" clearable @keyup.enter="handleQuery2" />
          </el-form-item>
          <el-form-item label="预约方式" prop="bookingMethod">
            <el-select v-model="queryParams2.bookingMethod" placeholder="预约方式" clearable>
              <el-option label="线上预约" value="线上" />
              <el-option label="电话预约" value="电话" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams2.status" placeholder="状态" clearable>
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery2">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery2">重置</el-button>
          </el-form-item>
        </el-form>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd2" v-hasPermi="['com:convenience:item:add']">新增</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch2" @queryTable="getList2" />
        </el-row>
        <el-table v-loading="loading2" :data="itemList">
          <el-table-column label="服务名称" prop="itemName" min-width="140" />
          <el-table-column label="服务商ID" prop="providerId" width="100" />
          <el-table-column label="价格(元)" prop="price" width="100" />
          <el-table-column label="时长(分钟)" prop="duration" width="100" />
          <el-table-column label="预约方式" prop="bookingMethod" width="100" />
          <el-table-column label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" align="center" width="180" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate2(scope.row)" v-hasPermi="['com:convenience:item:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete2(scope.row)" v-hasPermi="['com:convenience:item:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total2 > 0" :total="total2" v-model:page="queryParams2.pageNum" v-model:limit="queryParams2.pageSize" @pagination="getList2" />

        <!-- 服务项目弹窗 -->
        <el-dialog :title="title2" v-model="open2" width="600px" append-to-body>
          <el-form ref="formRef2" :model="form2" :rules="rules2" label-width="110px">
            <el-form-item label="所属服务商" prop="providerId">
              <el-select v-model="form2.providerId" placeholder="请选择服务商" filterable>
                <el-option v-for="p in providerOptions" :key="p.providerId" :label="p.providerName" :value="p.providerId" />
              </el-select>
            </el-form-item>
            <el-form-item label="服务名称" prop="itemName">
              <el-input v-model="form2.itemName" placeholder="请输入服务名称" maxlength="100" />
            </el-form-item>
            <el-row>
              <el-col :span="12">
                <el-form-item label="价格(元)" prop="price">
                  <el-input-number v-model="form2.price" :precision="2" :min="0" :step="10" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="时长(分钟)" prop="duration">
                  <el-input-number v-model="form2.duration" :min="1" :step="30" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="预约方式" prop="bookingMethod">
              <el-radio-group v-model="form2.bookingMethod">
                <el-radio value="线上">线上预约</el-radio>
                <el-radio value="电话">电话预约</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="服务详情" prop="serviceDetail">
              <el-input v-model="form2.serviceDetail" type="textarea" :rows="4" placeholder="请输入服务详情" maxlength="2000" />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form2.status">
                <el-radio value="0">启用</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form2.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button type="primary" @click="submitForm2">确 定</el-button>
              <el-button @click="cancel2">取 消</el-button>
            </div>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ==================== 预约订单 Tab ==================== -->
      <el-tab-pane label="预约订单管理" name="order">
        <el-form :model="queryParams3" ref="queryRef3" :inline="true" v-show="showSearch3" label-width="90px">
          <el-form-item label="订单编号" prop="orderNo">
            <el-input v-model="queryParams3.orderNo" placeholder="请输入订单编号" clearable @keyup.enter="handleQuery3" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams3.status" placeholder="订单状态" clearable>
              <el-option label="待接单" value="待接单" />
              <el-option label="已接单" value="已接单" />
              <el-option label="已完成" value="已完成" />
              <el-option label="已取消" value="已取消" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery3">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery3">重置</el-button>
          </el-form-item>
        </el-form>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:showSearch="showSearch3" @queryTable="getList3" />
        </el-row>
        <el-table v-loading="loading3" :data="orderList">
          <el-table-column label="订单编号" prop="orderNo" width="160" />
          <el-table-column label="服务项目ID" prop="itemId" width="100" />
          <el-table-column label="联系人" prop="contactName" width="100" />
          <el-table-column label="联系电话" prop="contactPhone" width="120" />
          <el-table-column label="预约时间" prop="bookingTime" width="170" />
          <el-table-column label="金额(元)" prop="amount" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === '待接单'" type="warning">待接单</el-tag>
              <el-tag v-else-if="scope.row.status === '已接单'" type="primary">已接单</el-tag>
              <el-tag v-else-if="scope.row.status === '已完成'" type="success">已完成</el-tag>
              <el-tag v-else-if="scope.row.status === '已取消'" type="info">已取消</el-tag>
              <span v-else>{{ scope.row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" align="center" width="240" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.status === '待接单'" link type="primary" icon="Check" @click="handleAccept(scope.row)" v-hasPermi="['com:convenience:order:accept']">接单</el-button>
              <el-button v-if="scope.row.status === '已接单'" link type="success" icon="CircleCheck" @click="handleComplete(scope.row)" v-hasPermi="['com:convenience:order:complete']">完成</el-button>
              <el-button v-if="scope.row.status === '待接单'" link type="warning" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['com:convenience:order:cancel']">取消</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete3(scope.row)" v-hasPermi="['com:convenience:order:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total3 > 0" :total="total3" v-model:page="queryParams3.pageNum" v-model:limit="queryParams3.pageSize" @pagination="getList3" />
      </el-tab-pane>

      <!-- ==================== 服务评价 Tab ==================== -->
      <el-tab-pane label="服务评价管理" name="review">
        <el-form :model="queryParams4" ref="queryRef4" :inline="true" v-show="showSearch4" label-width="80px">
          <el-form-item label="评分" prop="rating">
            <el-select v-model="queryParams4.rating" placeholder="评分" clearable>
              <el-option v-for="i in 5" :key="i" :label="i + '星'" :value="i" />
            </el-select>
          </el-form-item>
          <el-form-item label="服务商" prop="providerId">
            <el-select v-model="queryParams4.providerId" placeholder="服务商" clearable filterable>
              <el-option v-for="p in providerOptions" :key="p.providerId" :label="p.providerName" :value="p.providerId" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery4">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery4">重置</el-button>
          </el-form-item>
        </el-form>
        <el-row :gutter="10" class="mb8">
          <right-toolbar v-model:showSearch="showSearch4" @queryTable="getList4" />
        </el-row>
        <el-table v-loading="loading4" :data="reviewList">
          <el-table-column label="评价ID" prop="reviewId" width="80" />
          <el-table-column label="订单ID" prop="orderId" width="80" />
          <el-table-column label="评分" width="120">
            <template #default="scope">
              <span v-for="i in 5" :key="i">
                <el-icon v-if="i <= scope.row.rating" color="#f7ba2a"><StarFilled /></el-icon>
                <el-icon v-else color="#ccc"><Star /></el-icon>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="评价内容" prop="content" show-overflow-tooltip min-width="200" />
          <el-table-column label="评价人" prop="createBy" width="120" />
          <el-table-column label="评价时间" prop="createTime" width="170" />
          <el-table-column label="操作" align="center" width="120" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Delete" @click="handleDelete4(scope.row)" v-hasPermi="['com:convenience:review:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total4 > 0" :total="total4" v-model:page="queryParams4.pageNum" v-model:limit="queryParams4.pageSize" @pagination="getList4" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="ConvenienceManagement">
import { listProvider, getProvider, getAllProvider, addProvider, updateProvider, delProvider } from '@/api/com/convenience'
import { listServiceItem, getServiceItem, addServiceItem, updateServiceItem, delServiceItem } from '@/api/com/convenience'
import { listServiceOrder, acceptServiceOrder, completeServiceOrder, cancelServiceOrder, delServiceOrder } from '@/api/com/convenience'
import { listServiceReview, delServiceReview } from '@/api/com/convenience'
import { StarFilled, Star } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// ---- 当前 Tab ----
const activeTab = ref('provider')

// ======================== 服务商管理 ========================
const providerList = ref([])
const loading1 = ref(true)
const showSearch1 = ref(true)
const total1 = ref(0)
const title1 = ref('')
const open1 = ref(false)
const queryParams1 = ref({ pageNum: 1, pageSize: 10, providerName: null, contactName: null, status: null })
const form1 = ref({})
const rules1 = ref({
  providerName: [{ required: true, message: '服务商名称不能为空', trigger: 'blur' }],
  contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }]
})

function getList1() {
  loading1.value = true
  listProvider(queryParams1.value).then(res => {
    providerList.value = res.rows
    total1.value = res.total
    loading1.value = false
  }).catch(() => { loading1.value = false })
}
function handleQuery1() { queryParams1.value.pageNum = 1; getList1() }
function resetQuery1() { proxy.$refs.queryRef1?.resetFields(); handleQuery1() }
function handleAdd1() { form1.value = { status: '0' }; open1.value = true; title1.value = '新增服务商' }
function handleUpdate1(row) {
  getProvider(row.providerId).then(res => {
    form1.value = res.data
    open1.value = true
    title1.value = '修改服务商'
  })
}
function handleDelete1(row) {
  proxy.$modal.confirm('确认删除服务商「' + row.providerName + '」？').then(() => {
    delProvider(row.providerId).then(() => { getList1(); proxy.$modal.msgSuccess('删除成功') })
  }).catch(() => {})
}
function submitForm1() {
  proxy.$refs.formRef1?.validate(valid => {
    if (valid) {
      const api = form1.value.providerId ? updateProvider : addProvider
      api(form1.value).then(() => {
        proxy.$modal.msgSuccess('操作成功')
        open1.value = false
        getList1()
      }).catch(() => {})
    }
  })
}
function cancel1() { open1.value = false; form1.value = { status: '0' } }

// ======================== 服务项目管理 ========================
const itemList = ref([])
const loading2 = ref(true)
const showSearch2 = ref(true)
const total2 = ref(0)
const title2 = ref('')
const open2 = ref(false)
const queryParams2 = ref({ pageNum: 1, pageSize: 10, itemName: null, bookingMethod: null, status: null })
const form2 = ref({})
const rules2 = ref({
  providerId: [{ required: true, message: '请选择服务商', trigger: 'change' }],
  itemName: [{ required: true, message: '服务名称不能为空', trigger: 'blur' }],
  price: [{ required: true, message: '价格不能为空', trigger: 'blur' }]
})
const providerOptions = ref([])

function getList2() {
  loading2.value = true
  listServiceItem(queryParams2.value).then(res => {
    itemList.value = res.rows
    total2.value = res.total
    loading2.value = false
  }).catch(() => { loading2.value = false })
}
function handleQuery2() { queryParams2.value.pageNum = 1; getList2() }
function resetQuery2() { proxy.$refs.queryRef2?.resetFields(); handleQuery2() }
function handleAdd2() {
  getAllProvider().then(res => { providerOptions.value = res.data })
  form2.value = { status: '0', price: 0, duration: 60, bookingMethod: '线上' }
  open2.value = true
  title2.value = '新增服务项目'
}
function handleUpdate2(row) {
  getAllProvider().then(res => { providerOptions.value = res.data })
  getServiceItem(row.itemId).then(res => {
    form2.value = res.data
    open2.value = true
    title2.value = '修改服务项目'
  })
}
function handleDelete2(row) {
  proxy.$modal.confirm('确认删除服务项目「' + row.itemName + '」？').then(() => {
    delServiceItem(row.itemId).then(() => { getList2(); proxy.$modal.msgSuccess('删除成功') })
  }).catch(() => {})
}
function submitForm2() {
  proxy.$refs.formRef2?.validate(valid => {
    if (valid) {
      const api = form2.value.itemId ? updateServiceItem : addServiceItem
      api(form2.value).then(() => {
        proxy.$modal.msgSuccess('操作成功')
        open2.value = false
        getList2()
      }).catch(() => {})
    }
  })
}
function cancel2() { open2.value = false; form2.value = { status: '0', price: 0, duration: 60, bookingMethod: '线上' } }

// ======================== 预约订单管理 ========================
const orderList = ref([])
const loading3 = ref(true)
const showSearch3 = ref(true)
const total3 = ref(0)
const queryParams3 = ref({ pageNum: 1, pageSize: 10, orderNo: null, status: null })

function getList3() {
  loading3.value = true
  listServiceOrder(queryParams3.value).then(res => {
    orderList.value = res.rows
    total3.value = res.total
    loading3.value = false
  }).catch(() => { loading3.value = false })
}
function handleQuery3() { queryParams3.value.pageNum = 1; getList3() }
function resetQuery3() { proxy.$refs.queryRef3?.resetFields(); handleQuery3() }
function handleDelete3(row) {
  proxy.$modal.confirm('确认删除订单「' + (row.orderNo || row.orderId) + '」？').then(() => {
    delServiceOrder(row.orderId).then(() => { getList3(); proxy.$modal.msgSuccess('删除成功') })
  }).catch(() => {})
}
function handleAccept(row) {
  proxy.$modal.confirm('确认接单？').then(() => {
    acceptServiceOrder({ orderId: row.orderId }).then(() => { proxy.$modal.msgSuccess('已接单'); getList3() })
  }).catch(() => {})
}
function handleComplete(row) {
  proxy.$modal.confirm('确认完成该订单？').then(() => {
    completeServiceOrder({ orderId: row.orderId }).then(() => { proxy.$modal.msgSuccess('已完成'); getList3() })
  }).catch(() => {})
}
function handleCancel(row) {
  proxy.$modal.confirm('确认取消该订单？').then(() => {
    cancelServiceOrder({ orderId: row.orderId }).then(() => { proxy.$modal.msgSuccess('已取消'); getList3() })
  }).catch(() => {})
}

// ======================== 服务评价管理 ========================
const reviewList = ref([])
const loading4 = ref(true)
const showSearch4 = ref(true)
const total4 = ref(0)
const queryParams4 = ref({ pageNum: 1, pageSize: 10, rating: null, providerId: null })

function getList4() {
  loading4.value = true
  listServiceReview(queryParams4.value).then(res => {
    reviewList.value = res.rows
    total4.value = res.total
    loading4.value = false
  }).catch(() => { loading4.value = false })
}
function handleQuery4() { queryParams4.value.pageNum = 1; getList4() }
function resetQuery4() { proxy.$refs.queryRef4?.resetFields(); handleQuery4() }
function handleDelete4(row) {
  proxy.$modal.confirm('确认删除该评价？').then(() => {
    delServiceReview(row.reviewId).then(() => { getList4(); proxy.$modal.msgSuccess('删除成功') })
  }).catch(() => {})
}

// ======================== Tab 切换 ========================
function handleTabClick() {
  if (activeTab.value === 'provider') { getList1() }
  else if (activeTab.value === 'item') { getList2() }
  else if (activeTab.value === 'order') { getList3() }
  else if (activeTab.value === 'review') { getList4() }
}

// ======================== 加载服务商选项（通用） ========================
function loadProviderOptions() {
  getAllProvider().then(res => { providerOptions.value = res.data }).catch(() => {})
}

// 初始加载第一个 Tab
getList1()
loadProviderOptions()
</script>
