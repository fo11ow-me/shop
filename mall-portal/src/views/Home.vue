<template>
  <div class="home" v-loading="loading">
    <!-- Hero Banner -->
    <section class="hero">
      <el-carousel height="480px" :interval="4000" arrow="never" indicator-position="none">
        <el-carousel-item v-for="b in banners" :key="b">
          <div class="hero-slide">
            <img :src="b" />
            <div class="hero-overlay">
              <h2>品质家居 · 美好生活</h2>
              <p>精选好物，用心装扮你的家</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- Category Quick Nav -->
    <section class="quick-nav wrapper">
      <router-link v-for="cat in rootCats" :key="cat.id" :to="`/category/${cat.id}`" class="quick-item">
        <span class="quick-icon">{{ cat.name?.charAt(0) }}</span>
        <span class="quick-label">{{ cat.name }}</span>
      </router-link>
    </section>

    <!-- Product Sections -->
    <section v-for="cat in homeData" :key="cat.category.id" class="section wrapper">
      <header class="section-head">
        <h3>{{ cat.category.name }}</h3>
        <div v-if="cat.children?.length" class="section-tags">
          <router-link v-for="c in cat.children" :key="c.id" :to="`/category/${c.id}`">{{ c.name }}</router-link>
        </div>
      </header>

      <div class="grid">
        <ProductCard v-for="p in cat.products?.slice(0, 8)" :key="p.id"
          :product="p" :image-key="cat.productImages?.[p.id] || ''" @buy="buyNowHandler" />
      </div>
    </section>

    <el-empty v-if="!homeData.length" description="暂无商品数据" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData, buyNow } from '../api'
import { useAuthStore } from '@/stores/auth'
import ProductCard from '../components/ProductCard.vue'
import { ElMessage } from 'element-plus'
import banner1 from '@/assets/img/home-banner-1.jpg'
import banner2 from '@/assets/img/home-banner-2.jpg'

const router = useRouter()
const authStore = useAuthStore()
const banners = [banner1, banner2]
const homeData = ref([])
const loading = ref(false)
const rootCats = computed(() => homeData.value.map(c => c.category))

const buyNowHandler = (productId) => {
  if (!authStore.isLogin) { router.push('/login'); return }
  router.push({ name: 'Checkout', query: { productId, amount: 1 } })
}

;(async () => {
  loading.value = true
  try { const res = await getHomeData(); homeData.value = res.data || [] }
  catch { ElMessage.error('加载首页数据失败') }
  finally { loading.value = false }
})()
</script>

<style scoped>
.wrapper { width: 1240px; margin: 0 auto; }

/* ====== Hero ====== */
.hero { margin-bottom: 40px; }
.hero-slide { position: relative; height: 480px; }
.hero-slide img { width: 100%; height: 100%; object-fit: cover; }
.hero-overlay { position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; background: rgba(0,0,0,0.15); }
.hero-overlay h2 { font-size: 42px; color: #fff; font-weight: 600; text-shadow: 0 2px 8px rgba(0,0,0,0.3); margin: 0; }
.hero-overlay p { font-size: 18px; color: rgba(255,255,255,0.9); margin: 12px 0 0; letter-spacing: 4px; }

/* ====== Quick Nav ====== */
.quick-nav { display: flex; justify-content: center; gap: 32px; padding: 30px 0 40px; }
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 8px; text-decoration: none;
  color: #555; transition: color .2s; }
.quick-item:hover { color: #A10000; }
.quick-icon { width: 56px; height: 56px; border-radius: 50%; background: #fdf2f2; color: #A10000;
  display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 600;
  transition: all .2s; }
.quick-item:hover .quick-icon { background: #A10000; color: #fff; transform: translateY(-2px); }
.quick-label { font-size: 13px; }

/* ====== Sections ====== */
.section { margin-bottom: 56px; }
.section-head { display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 22px; padding-bottom: 14px; border-bottom: 2px solid #A10000; }
.section-head h3 { font-size: 22px; font-weight: 600; color: #222; margin: 0; }
.section-tags { display: flex; gap: 12px; }
.section-tags a { font-size: 13px; color: #888; text-decoration: none; transition: color .2s; }
.section-tags a:hover { color: #A10000; }

/* ====== Product Grid ====== */
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
</style>
