<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入公告标题" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="公告分类" clearable>
          <el-option label="物业通知" value="物业通知" />
          <el-option label="社区新闻" value="社区新闻" />
          <el-option label="政策法规" value="政策法规" />
          <el-option label="温馨提示" value="温馨提示" />
          <el-option label="活动募集" value="活动募集" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="公告状态" clearable>
          <el-option label="草稿" value="草稿" />
          <el-option label="已发布" value="已发布" />
          <el-option label="已撤回" value="已撤回" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:announcement:add']">新增公告</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="announcementList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="标题" prop="title" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="分类" prop="category" width="100" />
      <el-table-column label="发布范围" prop="targetScope" width="100">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.targetScope || '全部' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="有效期至" prop="expireDate" width="110" />
      <el-table-column label="发布时间" prop="publishTime" width="160" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="300" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:announcement:edit']">修改</el-button>
          <el-button v-if="scope.row.status === '草稿'" link type="success" icon="Upload" @click="handlePublish(scope.row)" v-hasPermi="['com:announcement:publish']">发布</el-button>
          <el-button v-if="scope.row.status === '已发布'" link type="warning" icon="Download" @click="handleRevoke(scope.row)" v-hasPermi="['com:announcement:publish']">撤回</el-button>
          <el-button link type="primary" icon="View" @click="handleViewReads(scope.row)" v-hasPermi="['com:announcement:read:list']">阅读</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:announcement:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="850px" append-to-body :close-on-click-modal="false">
      <el-form ref="announcementRef" :model="form" :rules="rules" label-width="88px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width:100%">
                <el-option label="物业通知" value="物业通知" />
                <el-option label="社区新闻" value="社区新闻" />
                <el-option label="政策法规" value="政策法规" />
                <el-option label="温馨提示" value="温馨提示" />
                <el-option label="活动募集" value="活动募集" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="正文" prop="content">
          <Editor v-model="form.content" :min-height="300" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="发布范围" prop="targetScope">
              <el-select v-model="form.targetScope" placeholder="请选择" style="width:100%">
                <el-option label="全部" value="全部" />
                <el-option label="指定楼栋" value="指定楼栋" />
                <el-option label="指定人群" value="指定人群" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推送方式" prop="pushMethod">
              <el-select v-model="form.pushMethod" multiple placeholder="请选择" style="width:100%">
                <el-option label="APP推送" value="APP推送" />
                <el-option label="短信" value="短信" />
                <el-option label="微信" value="微信" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="有效期至" prop="expireDate">
              <el-date-picker v-model="form.expireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.targetScope === '指定人群'">
          <el-col :span="12">
            <el-form-item label="目标人群" prop="targetGroups">
              <el-select v-model="form.targetGroups" multiple placeholder="请选择" style="width:100%">
                <el-option label="业主" value="业主" />
                <el-option label="租户" value="租户" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">保 存</el-button>
          <el-button type="success" v-if="form.announcementId && form.status === '草稿'" @click="submitAndPublish">保存并发布</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 阅读记录对话框 -->
    <el-dialog title="阅读记录" v-model="readOpen" width="700px" append-to-body>
      <el-table v-loading="readLoading" :data="readList">
        <el-table-column label="用户ID" prop="userId" width="80" />
        <el-table-column label="阅读时间" prop="readTime" width="180" />
        <el-table-column label="创建时间" prop="createTime" width="180" />
      </el-table>
      <pagination v-show="readTotal > 0" :total="readTotal" v-model:page="readQuery.pageNum" v-model:limit="readQuery.pageSize" @pagination="getReadList" />
    </el-dialog>
  </div>
</template>

<script setup name="AnnouncementManagement">
import { listAnnouncement, getAnnouncement, addAnnouncement, updateAnnouncement, delAnnouncement, publishAnnouncement, revokeAnnouncement, listAnnouncementRead } from '@/api/com/announcement'
import Editor from '@/components/Editor'
import { getCurrentInstance, ref, reactive, toRefs } from 'vue'

const { proxy } = getCurrentInstance()

const announcementList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, category: undefined, status: undefined },
  rules: {
    title: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

// 阅读记录
const readOpen = ref(false)
const readLoading = ref(false)
const readList = ref([])
const readTotal = ref(0)
const currentAnnouncementId = ref(null)
const readQuery = ref({ pageNum: 1, pageSize: 10 })

function statusTagType(status) {
  const map = { '草稿': 'info', '已发布': 'success', '已撤回': 'warning' }
  return map[status] || 'info'
}

function getList() {
  loading.value = true
  listAnnouncement(queryParams.value).then(response => {
    announcementList.value = response.rows
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
  ids.value = selection.map(item => item.announcementId)
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增公告'
}

function handleUpdate(row) {
  getAnnouncement(row.announcementId).then(response => {
    fillForm(response.data)
    open.value = true
    title.value = '修改公告'
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除公告"' + row.title + '"？').then(function() {
    return delAnnouncement(row.announcementId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handlePublish(row) {
  proxy.$modal.confirm('确认发布该公告？发布后居民端可见。').then(() => {
    publishAnnouncement(row.announcementId).then(() => {
      proxy.$modal.msgSuccess('发布成功')
      getList()
    })
  })
}

function handleRevoke(row) {
  proxy.$modal.confirm('确认撤回该公告？撤回后居民端不可见。').then(() => {
    revokeAnnouncement(row.announcementId).then(() => {
      proxy.$modal.msgSuccess('已撤回')
      getList()
    })
  })
}

function submitForm() {
  proxy.$refs.announcementRef.validate(valid => {
    if (valid) {
      const data = prepareData({ ...form.value })
      if (data.announcementId != undefined) {
        updateAnnouncement(data).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        }).catch(() => {})
      } else {
        addAnnouncement(data).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        }).catch(() => {})
      }
    }
  })
}

function submitAndPublish() {
  proxy.$refs.announcementRef.validate(valid => {
    if (valid) {
      const data = prepareData({ ...form.value })
      const savePromise = data.announcementId != undefined
        ? updateAnnouncement(data)
        : addAnnouncement(data)
      savePromise.then(res => {
        const id = res.data
        publishAnnouncement(id).then(() => {
          proxy.$modal.msgSuccess('保存并发布成功')
          open.value = false
          getList()
        })
      }).catch(() => {})
    }
  })
}

// 提交前将数组字段转为逗号分隔字符串
function prepareData(data) {
  if (Array.isArray(data.pushMethod)) {
    data.pushMethod = data.pushMethod.join(',')
  }
  if (Array.isArray(data.targetGroups)) {
    data.targetGroups = data.targetGroups.join(',')
  }
  return data
}

// 编辑时，回填时将字符串切回数组
function fillForm(data) {
  form.value = data
  if (typeof form.value.pushMethod === 'string') {
    form.value.pushMethod = form.value.pushMethod.split(',').filter(Boolean)
  }
  if (typeof form.value.targetGroups === 'string') {
    form.value.targetGroups = form.value.targetGroups.split(',').filter(Boolean)
  }
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    announcementId: undefined,
    title: undefined,
    content: undefined,
    category: undefined,
    targetScope: '全部',
    pushMethod: [],
    targetGroups: [],
    expireDate: undefined,
    remark: undefined
  }
  proxy.resetForm('announcementRef')
}

function handleViewReads(row) {
  currentAnnouncementId.value = row.announcementId
  readQuery.value.pageNum = 1
  readOpen.value = true
  getReadList()
}

function getReadList() {
  readLoading.value = true
  listAnnouncementRead({ ...readQuery.value, announcementId: currentAnnouncementId.value }).then(response => {
    readList.value = response.rows
    readTotal.value = response.total
    readLoading.value = false
  }).catch(() => { readLoading.value = false })
}

getList()
</script>
