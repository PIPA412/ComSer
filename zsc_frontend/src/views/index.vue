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
          <el-empty description="暂无公告" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
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

</style>
