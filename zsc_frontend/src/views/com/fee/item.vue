<template>
  <div>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:fee:item:add']">新增</el-button></el-col>
    </el-row>
    <el-table v-loading="loading" :data="itemList">
      <el-table-column label="费用名称" prop="itemName" />
      <el-table-column label="计费方式" prop="chargeType" />
      <el-table-column label="单价(元)" prop="unitPrice" />
      <el-table-column label="计费周期" prop="billingCycle" />
      <el-table-column label="状态" prop="status">
        <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" />
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:fee:item:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:fee:item:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="费用名称" prop="itemName"><el-input v-model="form.itemName" placeholder="请输入费用名称" /></el-form-item>
        <el-form-item label="计费方式" prop="chargeType">
          <el-select v-model="form.chargeType" placeholder="请选择">
            <el-option label="固定" value="固定" />
            <el-option label="按面积" value="按面积" />
            <el-option label="按户" value="按户" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :precision="2" :step="0.1" /></el-form-item>
        <el-form-item label="计费周期" prop="billingCycle">
          <el-select v-model="form.billingCycle" placeholder="请选择">
            <el-option label="月" value="月" />
            <el-option label="季" value="季" />
            <el-option label="年" value="年" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="FeeItem">
import { listFeeItem, getFeeItem, addFeeItem, updateFeeItem, delFeeItem } from '@/api/com/fee'
const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict ? proxy.useDict('sys_normal_disable') : { sys_normal_disable: ref([]) }
const itemList = ref([]); const loading = ref(true); const total = ref(0)
const open = ref(false); const title = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10 })
const form = ref({ status: '0' })
const rules = { itemName: [{ required: true, message: '费用名称不能为空', trigger: 'blur' }] }

function getList() { loading.value = true; listFeeItem(queryParams.value).then(res => { itemList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleAdd() { form.value = { status: '0' }; title.value = '新增费用项目'; open.value = true }
function handleUpdate(row) { getFeeItem(row.itemId).then(res => { form.value = res.data; title.value = '修改费用项目'; open.value = true }) }
function submitForm() { proxy.$refs.formRef.validate(v => { if (v) { (form.value.itemId ? updateFeeItem(form.value) : addFeeItem(form.value)).then(() => { proxy.$modal.msgSuccess('操作成功'); open.value = false; getList() }) } }) }
function handleDelete(row) { proxy.$modal.confirm('确认删除?').then(() => delFeeItem(row.itemId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })) }
getList()
</script>
