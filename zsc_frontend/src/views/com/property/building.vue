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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:property:building:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="buildingList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="楼栋编号" prop="buildingCode" />
      <el-table-column label="楼栋名称" prop="buildingName" />
      <el-table-column label="楼层数" prop="floorCount" />
      <el-table-column label="地址" prop="address" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="状态" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:property:building:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:property:building:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="buildingRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="楼栋名称" prop="buildingName">
          <el-input v-model="form.buildingName" placeholder="请输入楼栋名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="楼栋编号" prop="buildingCode">
          <el-input v-model="form.buildingCode" placeholder="请输入楼栋编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="楼层数" prop="floorCount">
          <el-input-number v-model="form.floorCount" :min="1" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" maxlength="255" />
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
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Building">
import { listBuilding, getBuilding, addBuilding, updateBuilding, delBuilding } from '@/api/com/property'

const { proxy } = getCurrentInstance()
const buildingList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, buildingName: null, status: null })
const form = ref({})
const rules = ref({
  buildingName: [{ required: true, message: '楼栋名称不能为空', trigger: 'blur' }],
  floorCount: [{ required: true, message: '楼层数不能为空', trigger: 'blur' }]
})

function getList() { loading.value = true; listBuilding(queryParams.value).then(res => { buildingList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '添加楼栋' }
function handleUpdate(row) { getBuilding(row.buildingId).then(res => { form.value = res.data; open.value = true; title.value = '修改楼栋' }) }
function handleDelete(row) { proxy.$modal.confirm('确认删除该楼栋?').then(() => delBuilding(row.buildingId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})) }
function submitForm() { buildingRef.value?.validate(valid => { if (valid) { (form.value.buildingId ? updateBuilding : addBuilding)(form.value).then(() => { proxy.$modal.msgSuccess('操作成功'); open.value = false; getList() }).catch(() => {}) } }) }
function cancel() { open.value = false; reset() }
function reset() { form.value = { status: '0', floorCount: 1 } }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.buildingId) }
getList()
</script>
