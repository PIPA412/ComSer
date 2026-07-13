<template>
  <div class="app-container home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <h2>
        <el-icon :size="32"><HomeFilled /></el-icon>
        欢迎使用社区服务管理系统
      </h2>
      <p>基于若依快速开发框架，为社区提供高效便捷的管理服务</p>
    </div>

    <el-row :gutter="20">
      <!-- 通知公告 -->
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20"><Bell /></el-icon>
              <span>通知公告</span>
            </div>
          </template>
          <div v-if="announcements.length > 0">
            <div v-for="item in announcements" :key="item.announcementId" class="announcement-item" @click="showDetail(item)">
              <div class="announcement-header">
                <el-tag size="small" :type="categoryType(item.category)">{{ item.category || '通知' }}</el-tag>
                <span class="announcement-title">{{ item.title }}</span>
                <span class="announcement-time">{{ item.publishTime }}</span>
              </div>
              <div class="announcement-summary" v-html="truncateContent(item.content, 120)" />
            </div>
          </div>
          <el-empty v-else description="暂无公告" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailOpen" :title="current.title" width="700px" append-to-body>
      <div class="announcement-meta">
        <el-tag :type="categoryType(current.category)">{{ current.category || '通知' }}</el-tag>
        <span style="margin-left:12px;color:#909399">发布时间：{{ current.publishTime }}</span>
        <span v-if="current.expireDate" style="margin-left:12px;color:#909399">有效期至：{{ current.expireDate }}</span>
      </div>
      <div class="announcement-content" v-html="current.content" style="margin-top:16px;line-height:1.8" />
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { listPublishedAnnouncement, recordRead } from '@/api/com/announcement'
import { ref, onMounted } from 'vue'

const announcements = ref([])
const detailOpen = ref(false)
const current = ref({})

function categoryType(cat) {
  const map = { '物业通知': '', '社区新闻': 'success', '政策法规': 'warning', '温馨提示': 'info', '活动募集': 'danger' }
  return map[cat] || ''
}

function truncateContent(html, maxLen) {
  if (!html) return ''
  const text = html.replace(/<[^>]+>/g, '')
  if (text.length <= maxLen) return text
  return text.substring(0, maxLen) + '...'
}

function showDetail(item) {
  current.value = item
  detailOpen.value = true
  // 记录阅读统计
  recordRead(item.announcementId).catch(() => {})
}

onMounted(() => {
  listPublishedAnnouncement().then(res => {
    announcements.value = res.data || []
  }).catch(() => {})
})
</script>

<style scoped>
.home {
  padding: 10px;
}

.welcome-banner {
  text-align: center;
  padding: 30px 0;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: #fff;
}

.welcome-banner h2 {
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 24px;
}

.welcome-banner p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.announcement-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.announcement-item:last-child {
  border-bottom: none;
}
.announcement-item:hover {
  background: #fafafa;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.announcement-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.announcement-title {
  font-size: 15px;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-time {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
}

.announcement-summary {
  margin-top: 6px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}
</style>
