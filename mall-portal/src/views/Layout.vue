<template>
  <div class="layout">
    <!-- Header -->
    <header class="header" :class="{ 'is-sticky': isSticky }">
      <div class="header-top wrapper">
        <router-link to="/" class="logo">
          <img src="/logo.png" alt="最家家居" />
        </router-link>

        <div class="header-actions">
          <form @submit.prevent="doSearch" class="search-form">
            <el-input v-model="keyword" placeholder="搜索商品..." size="small" class="search-input"
              @keyup.enter="doSearch" clearable />
            <button type="submit" class="search-btn">
              <el-icon><Search /></el-icon>
            </button>
          </form>

          <div class="user-links">
            <template v-if="!isLogin">
              <router-link to="/login">登录</router-link>
              <span class="divider">|</span>
              <router-link to="/register">注册</router-link>
            </template>
            <template v-else>
              <span class="greeting">{{ username }}</span>
              <span class="divider">|</span>
              <router-link to="/userinfo">个人信息</router-link>
              <span class="divider">|</span>
              <a @click="handleLogout">退出</a>
            </template>
          </div>

          <router-link to="/cart" class="cart-icon">
            <el-icon :size="20"><ShoppingCart /></el-icon>
            <span class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</span>
          </router-link>
        </div>
      </div>

      <!-- Category Navigation -->
      <nav class="nav-bar">
        <div class="wrapper">
          <ul class="nav-list">
            <li class="nav-item">
              <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/seckill" class="nav-link seckill-nav" active-class="active">限时秒杀</router-link>
            </li>
            <li v-for="cat in rootCategories" :key="cat.id" class="nav-item"
              @mouseenter="hoverCat = cat.id" @mouseleave="hoverCat = null">
              <router-link :to="`/category/${cat.id}`" class="nav-link" :class="{ active: isCatActive(cat.id) }">
                {{ cat.name }}
                <span v-if="cat.children?.length" class="arrow">▾</span>
              </router-link>
              <transition name="dropdown">
                <div v-if="cat.children?.length && hoverCat === cat.id" class="sub-nav"
                  @mouseenter="hoverCat = cat.id" @mouseleave="hoverCat = null">
                  <router-link v-for="child in cat.children" :key="child.id"
                    :to="`/category/${child.id}`" class="sub-link">{{ child.name }}</router-link>
                </div>
              </transition>
            </li>
          </ul>
        </div>
      </nav>
    </header>

    <!-- Spacer for sticky header -->
    <div class="header-spacer"></div>

    <!-- Main Content -->
    <main class="main-content"><router-view /></main>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-service wrapper">
        <div v-for="item in footerItems" :key="item.title" class="service-item">
          <img :src="item.icon" />
          <span>{{ item.title }}</span>
        </div>
      </div>
      <div class="footer-bottom">
        <p>最家家居 &copy; 2026 公司版权所有</p>
        <p>京ICP备080100-44备0000111000号 | 举报电话：188-0130-1238</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, ShoppingCart } from '@element-plus/icons-vue'

import foot1 from '@/assets/img/footer-quality.png'
import foot2 from '@/assets/img/footer-privacy.png'
import foot3 from '@/assets/img/footer-returns.png'
import foot4 from '@/assets/img/footer-support.png'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { getCategories } from '../api'
import { getCartList } from '@/api/cart'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const keyword = ref('')
const isSticky = ref(false)
const hoverCat = ref(null)
const rootCategories = ref([])
const cartCount = ref(0)

const isLogin = computed(() => authStore.isLogin)
const username = computed(() => authStore.user?.name)

const footerItems = [
  { title: '7天无理由退货', icon: foot1 },
  { title: '15天免费换货', icon: foot2 },
  { title: '满599包邮', icon: foot3 },
  { title: '手机特色服务', icon: foot4 }
]

const isCatActive = (catId) => {
  if (route.name === 'ProductList') return Number(route.params.id) === catId
  return false
}

const doSearch = () => {
  if (keyword.value.trim()) {
    router.push({ name: 'Search', query: { keyword: keyword.value.trim() } })
  }
}

const handleLogout = () => {
  authStore.logout()
  userStore.clear()
  router.push('/')
}

const handleScroll = () => { isSticky.value = window.scrollY > 80 }

onMounted(async () => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  try {
    const cats = await getCategories()
    rootCategories.value = cats.data || []
  } catch {}
  if (authStore.isLogin) {
    try {
      const cart = await getCartList()
      cartCount.value = cart?.data?.length || 0
    } catch {}
  }
})

onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
/* ====== Header ====== */
.header { position: relative; z-index: 100; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.header.is-sticky { position: fixed; top: 0; left: 0; right: 0; }

.wrapper { width: 1160px; margin: 0 auto; }

/* Top bar */
.header-top { display: flex; align-items: center; height: 72px; gap: 24px; }
.logo { flex-shrink: 0; }
.logo img { height: 44px; display: block; }

.header-actions { display: flex; align-items: center; gap: 20px; flex: 1; justify-content: flex-end; }

/* Search */
.search-form { display: flex; align-items: center; width: 220px; }
.search-input :deep(.el-input__wrapper) { border-radius: 20px 0 0 20px; border-right: none; box-shadow: 1px 0 0 0 #dcdfe6 inset; }
.search-btn { width: 38px; height: 32px; border: 1px solid #dcdfe6; border-left: none; border-radius: 0 20px 20px 0;
  background: #f5f5f5; cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: #666; transition: all .2s; }
.search-btn:hover { background: #A10000; color: #fff; border-color: #A10000; }

/* User links */
.user-links { display: flex; align-items: center; gap: 6px; font-size: 13px; white-space: nowrap; }
.user-links a { color: #555; text-decoration: none; cursor: pointer; transition: color .2s; }
.user-links a:hover { color: #A10000; }
.greeting { color: #333; font-size: 13px; }
.divider { color: #ddd; }

/* Cart icon */
.cart-icon { position: relative; color: #555; transition: color .2s; display: flex; align-items: center; }
.cart-icon:hover { color: #A10000; }
.cart-badge { position: absolute; top: -8px; right: -10px; background: #A10000; color: #fff;
  font-size: 11px; min-width: 18px; height: 18px; border-radius: 9px; display: flex;
  align-items: center; justify-content: center; padding: 0 4px; }

/* Nav bar */
.nav-bar { background: #fff; border-top: 1px solid #f0f0f0; }
.nav-list { display: flex; justify-content: center; list-style: none; padding: 0; margin: 0; }
.nav-item { position: relative; }
.nav-link { display: block; padding: 12px 24px; font-size: 15px; color: #333; text-decoration: none;
  font-weight: 500; transition: color .2s; white-space: nowrap; }
.nav-link:hover, .nav-link.active { color: #A10000; }
.nav-link.seckill-nav { color: #A10000; font-weight: 600; position: relative; }
.nav-link.seckill-nav::before { content: '⚡'; margin-right: 4px; font-size: 13px; }
.nav-link.seckill-nav:hover { color: #C10000; }
.nav-link .arrow { font-size: 10px; margin-left: 2px; }

/* Sub nav dropdown */
.sub-nav { position: absolute; top: 100%; left: 0; min-width: 140px; background: #fff;
  border: 1px solid #eee; border-radius: 0 0 8px 8px; box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  padding: 8px 0; z-index: 102; }
.sub-link { display: block; padding: 8px 20px; font-size: 14px; color: #555; text-decoration: none;
  transition: all .15s; }
.sub-link:hover { color: #A10000; background: #fdf2f2; }

.dropdown-enter-active { transition: all .2s ease-out; }
.dropdown-leave-active { transition: all .15s ease-in; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-4px); }

/* Spacer */
.header-spacer { height: 0; }
.header.is-sticky + .header-spacer { height: 120px; }
.header:not(.is-sticky) + .header-spacer { height: 0; }

/* Main */
.main-content { min-height: 500px; }

/* ====== Footer ====== */
.footer { margin-top: 60px; }
.footer-service { display: flex; padding: 30px 0; border-top: 1px solid #eee; border-bottom: 1px solid #eee; }
.service-item { flex: 1; text-align: center; }
.service-item:not(:last-child) { border-right: 1px solid #eee; }
.service-item img { display: block; margin: 0 auto; width: 44px; height: 44px; opacity: 0.7; }
.service-item span { display: block; margin-top: 12px; font-size: 15px; color: #666; }
.footer-bottom { background: #1a1a1a; padding: 28px 0; text-align: center; }
.footer-bottom p { margin: 4px 0; color: #999; font-size: 12px; line-height: 1.8; }
</style>
