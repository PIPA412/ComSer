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

    <!-- 居民快捷入口 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="12">
        <div class="action-card complaint-card" @click="goComplaint('complaint')">
          <div class="action-icon">
            <el-icon :size="48"><WarningFilled /></el-icon>
          </div>
          <div class="action-info">
            <h3>我要投诉</h3>
            <p>反映社区问题，维护自身权益</p>
          </div>
          <el-icon class="action-arrow" :size="20"><ArrowRight /></el-icon>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="action-card suggestion-card" @click="goComplaint('suggestion')">
          <div class="action-icon">
            <el-icon :size="48"><ChatLineSquare /></el-icon>
          </div>
          <div class="action-info">
            <h3>我要建议</h3>
            <p>建言献策，共建美好社区</p>
          </div>
          <el-icon class="action-arrow" :size="20"><ArrowRight /></el-icon>
        </div>
      </el-col>
    </el-row>

    <!-- 通知公告 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20"><Bell /></el-icon>
              <span>通知公告</span>
            </div>
          </template>
          <el-empty description="暂无公告" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { HomeFilled, Bell, WarningFilled, ChatLineSquare, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

function goComplaint(tab) {
  if (tab === 'complaint') {
    router.push('/complaint/complaint')
  } else {
    router.push('/complaint/suggestion')
  }
}
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

/* 快捷入口 */
.quick-actions {
  margin-bottom: 20px;
}

.action-card {
  display: flex;
  align-items: center;
  padding: 28px 24px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.complaint-card {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
  border: 1px solid #ffd0d0;
}

.complaint-card:hover {
  background: linear-gradient(135deg, #ffeded 0%, #ffd6d6 100%);
}

.complaint-card .action-icon {
  color: #e74c3c;
}

.suggestion-card {
  background: linear-gradient(135deg, #f0f7ff 0%, #e3f0ff 100%);
  border: 1px solid #c8dfff;
}

.suggestion-card:hover {
  background: linear-gradient(135deg, #e6f0ff 0%, #d0e5ff 100%);
}

.suggestion-card .action-icon {
  color: #409eff;
}

.action-icon {
  flex-shrink: 0;
  margin-right: 18px;
}

.action-info h3 {
  margin: 0 0 6px;
  font-size: 20px;
  color: #303133;
}

.action-info p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.action-arrow {
  flex-shrink: 0;
  margin-left: auto;
  color: #c0c4cc;
  transition: transform 0.3s;
}

.action-card:hover .action-arrow {
  transform: translateX(4px);
  color: #606266;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
</style>
