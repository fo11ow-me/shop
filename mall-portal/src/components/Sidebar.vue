<template>
  <aside class="sidebar">
    <nav class="side-nav">
      <router-link to="/userinfo" class="side-item" :class="{ active: active === 'userinfo' }">个人信息</router-link>
      <router-link to="/cart" class="side-item" :class="{ active: active === 'cart' }">购物车</router-link>
      <router-link to="/orders" class="side-item" :class="{ active: active === 'orders' }">订单</router-link>
    </nav>
  </aside>
</template>

<script setup>
defineProps({
  active: { type: String, default: '' }
})
</script>

<style scoped>
.sidebar { width: 200px; flex-shrink: 0; }

.side-nav { background: #fff; border-radius: 10px; border: 1px solid #eee; overflow: hidden; }
.side-item { display: block; padding: 14px 20px; font-size: 14px; color: #666;
  text-decoration: none; border-bottom: 1px solid #f5f5f5; transition: all .15s; }
.side-item:last-child { border-bottom: none; }
.side-item:hover { color: #A10000; background: #fefafa; }
.side-item.active { color: #A10000; font-weight: 500; background: #fef5f5; }
</style>
