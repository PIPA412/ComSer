<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">预警规则配置</span>
          <el-button type="primary" @click="handleAdd" v-hasPermi="['com:dashboard:alert']">新增规则</el-button>
        </div>
      </template>

      <el-table :data="ruleList" v-loading="loading" stripe>
        <el-table-column label="规则名称" prop="ruleName" min-width="140" />
        <el-table-column label="指标名称" prop="metricName" min-width="120" />
        <el-table-column label="指标标识" prop="metricKey" min-width="140">
          <template #default="s"><el-tag>{{ s.row.metricKey }}</el-tag></template>
        </el-table-column>
        <el-table-column label="比较方式" prop="compareType" width="90">
          <template #default="s">
            <el-tag type="info">{{ {GT:'>', LT:'<', GTE:'>=', LTE:'<='}[s.row.compareType] || s.row.compareType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阈值" prop="threshold" width="100" />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="s">
            <el-tag :type="s.row.status === '0' ? 'success' : 'danger'">{{ s.row.status === '0' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="s">
            <el-button link type="primary" size="small" @click="handleEdit(s.row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleToggle(s.row)">
              {{ s.row.status === '0' ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDel(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-if="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px" append-to-body>
      <el-form ref="ruleFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="如：收缴率过低预警" />
        </el-form-item>
        <el-form-item label="指标标识" prop="metricKey">
          <el-select v-model="form.metricKey" placeholder="选择指标" style="width:100%">
            <el-option label="collection_rate（当月收缴率）" value="collection_rate" />
            <el-option label="complaint_monthly（月度投诉量）" value="complaint_monthly" />
            <el-option label="repair_overdue_rate（报修超时率）" value="repair_overdue_rate" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标名称" prop="metricName">
          <el-input v-model="form.metricName" placeholder="展示用名称，如：当月收缴率" />
        </el-form-item>
        <el-form-item label="比较方式" prop="compareType">
          <el-select v-model="form.compareType" style="width:100%">
            <el-option label="大于 (>)" value="GT" />
            <el-option label="小于 (<)" value="LT" />
            <el-option label="大于等于 (>=)" value="GTE" />
            <el-option label="小于等于 (<=)" value="LTE" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number v-model="form.threshold" :min="0" :precision="2" :step="5" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AlertRule">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { listAlertRules, addAlertRule, updateAlertRule, delAlertRule } from '@/api/com/dashboard'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const ruleList = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const ruleFormRef = ref()
const form = reactive({
  ruleId: undefined, ruleName: '', metricKey: '', metricName: '',
  compareType: 'LT', threshold: 0, status: '0'
})
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  metricKey: [{ required: true, message: '请选择指标标识', trigger: 'change' }],
  metricName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listAlertRules({ pageNum: queryParams.pageNum, pageSize: queryParams.pageSize }).then(res => {
    ruleList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleAdd() {
  dialogTitle.value = '新增预警规则'
  form.ruleId = undefined; form.ruleName = ''; form.metricKey = ''
  form.metricName = ''; form.compareType = 'LT'; form.threshold = 0
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑预警规则'
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleToggle(row) {
  const newStatus = row.status === '0' ? '1' : '0'
  const msg = newStatus === '1' ? '停用后该规则不再触发预警，确定停用？' : '确定启用该规则？'
  proxy.$modal.confirm(msg).then(() => {
    updateAlertRule({ ruleId: row.ruleId, status: newStatus }).then(() => {
      proxy.$modal.msgSuccess(newStatus === '1' ? '已停用' : '已启用')
      getList()
    })
  }).catch(() => {})
}

function handleDel(row) {
  proxy.$modal.confirm('确定删除规则「' + row.ruleName + '」？').then(() => {
    delAlertRule(row.ruleId).then(() => { proxy.$modal.msgSuccess('已删除'); getList() })
  }).catch(() => {})
}

function submitForm() {
  ruleFormRef.value.validate(valid => {
    if (!valid) return
    if (form.ruleId) {
      updateAlertRule(form).then(() => { proxy.$modal.msgSuccess('修改成功'); dialogVisible.value = false; getList() })
    } else {
      addAlertRule(form).then(() => { proxy.$modal.msgSuccess('新增成功'); dialogVisible.value = false; getList() })
    }
  })
}

onMounted(() => getList())
</script>
