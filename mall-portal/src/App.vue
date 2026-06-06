<template>
  <router-view />
  <transition name="gotop-fade">
    <div v-show="showBar" class="gotop">
      <router-link to="/cart" class="gotop-item" title="购物车">
        <el-icon :size="20"><ShoppingCart /></el-icon>
        <span class="tip">购物车</span>
      </router-link>
      <a href="javascript:void(0)" class="gotop-item" title="客服 400-800-8200">
        <el-icon :size="20"><Service /></el-icon>
        <span class="tip">客服</span>
      </a>
      <router-link to="/userinfo" class="gotop-item" title="个人信息">
        <el-icon :size="20"><User /></el-icon>
        <span class="tip">个人信息</span>
      </router-link>
      <a href="javascript:void(0)" class="gotop-item" @click="scrollToTop" title="返回顶部">
        <el-icon :size="20"><Top /></el-icon>
      </a>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ShoppingCart, Service, User, Top } from '@element-plus/icons-vue'

const showBar = ref(false)
let lastScroll = 0

const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

onMounted(() => {
  const handler = () => {
    const y = window.scrollY
    showBar.value = y > 300
    lastScroll = y
  }
  window.addEventListener('scroll', handler, { passive: true })
})
onUnmounted(() => window.removeEventListener('scroll', handler))
</script>

<style>
.gotop { position: fixed; right: 16px; bottom: 120px; z-index: 99; display: flex; flex-direction: column; gap: 2px; }
.gotop-item { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;
  background: #fff; border: 1px solid #e8e8e8; border-radius: 8px; color: #666; text-decoration: none;
  position: relative; transition: all .2s; }
.gotop-item:hover { color: #A10000; border-color: #A10000; background: #fef5f5; box-shadow: 0 2px 8px rgba(161,0,0,0.1); }
.gotop-item .tip { position: absolute; right: 54px; background: #333; color: #fff; font-size: 12px;
  padding: 4px 10px; border-radius: 4px; white-space: nowrap; opacity: 0; pointer-events: none; transition: opacity .2s; }
.gotop-item:hover .tip { opacity: 1; }
.gotop-fade-enter-active, .gotop-fade-leave-active { transition: opacity .3s, transform .3s; }
.gotop-fade-enter-from, .gotop-fade-leave-to { opacity: 0; transform: translateX(20px); }
</style>
