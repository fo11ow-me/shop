<template>
  <div class="step-bar">
    <el-steps :active="current" align-center finish-status="success">
      <el-step title="购物车" />
      <el-step title="确认订单" />
      <el-step title="下单成功" />
    </el-steps>
  </div>
</template>

<script setup>
defineProps({ current: { type: Number, default: 0 } })
</script>

<style scoped>
.step-bar { margin-bottom: 24px; padding: 20px 40px; background: #fff; border-radius: 10px; }
.step-bar :deep(.el-step__head.is-success) { color: #A10000; border-color: #A10000; }
.step-bar :deep(.el-step__title.is-success) { color: #A10000; }
.step-bar :deep(.el-step__line) { background-color: #e5e5e5; }
.step-bar :deep(.is-success .el-step__line) { background-color: #A10000; }
</style>
