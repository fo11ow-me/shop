<template>
  <div class="error-page">
    <div class="error-content">
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #f5f5f5;
}
.error-content {
  text-align: center;
}
.error-content h1 {
  font-size: 80px;
  color: #dcdfe6;
  margin: 0;
}
.error-content p {
  font-size: 18px;
  color: #909399;
  margin: 16px 0 24px;
}
</style>
