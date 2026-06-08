<template>
  <div class="wrapper" v-loading="loading">
    <!-- Breadcrumb -->
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link v-if="product" :to="`/category/${product.categoryId}`">{{ product.categoryName || product.categoryId }}</router-link>
      <span class="sep">/</span>
      <span class="current">{{ product?.name }}</span>
    </div>

    <div v-if="product" class="detail">
      <!-- Left: Image Gallery -->
      <div class="gallery">
        <div class="main-image">
          <img :src="mainImage ? getImageUrl(mainImage) : tempBanner1" :alt="product.name" />
        </div>
        <div class="thumb-list" v-if="images.length > 1">
          <div v-for="(img, i) in images" :key="i" :class="['thumb', { active: mainImage === img.url }]"
            @click="mainImage = img.url">
            <img :src="getImageUrl(img.url)" />
          </div>
        </div>
      </div>

      <!-- Right: Product Info -->
      <div class="info">
        <h1 class="product-name">{{ product.name }}</h1>
        <p class="product-desc">{{ product.detail || '品质家居，精选好物' }}</p>

        <!-- Seckill Countdown -->
        <div v-if="seckillInfo" class="seckill-bar">
          <span class="seckill-bar-icon">⚡</span>
          <span class="seckill-bar-price">秒杀价 <em>&yen;{{ seckillInfo.seckillPrice }}</em></span>
          <span class="seckill-bar-countdown">剩余 {{ formatCountdown(seckillInfo.countdown) }}</span>
        </div>

        <div class="price-box" v-if="!seckillInfo">
          <span class="price-label">价格</span>
          <span class="price-value"><em>&yen;</em>{{ product.price }}</span>
        </div>

        <div class="meta" v-if="!seckillInfo">
          <div class="meta-item">
            <span class="label">库存</span>
            <span class="value" :class="{ low: product.stock < 10 }">{{ product.stock }} 件</span>
          </div>
          <div class="meta-item">
            <span class="label">运费</span>
            <span class="value free">免运费</span>
          </div>
        </div>

        <div class="qty-row" v-if="!seckillInfo">
          <span class="label">数量</span>
          <div class="qty-control">
            <button @click="amount > 1 && amount--" :disabled="amount <= 1">−</button>
            <span class="qty-value">{{ amount }}</span>
            <button @click="amount < product.stock && amount++" :disabled="amount >= product.stock">+</button>
          </div>
        </div>

        <div class="btn-row">
          <template v-if="seckillInfo">
            <button class="btn-buy seckill-btn" @click="goSeckill">⚡ 立即秒杀</button>
          </template>
          <template v-else>
            <button class="btn-cart" @click="handleAddCart">
              <el-icon :size="18"><ShoppingCart /></el-icon>加入购物车
            </button>
            <button class="btn-buy" @click="handleBuyNow">立即购买</button>
          </template>
        </div>
      </div>
    </div>

    <!-- Related Products -->
    <section v-if="related.length" class="related">
      <h3>猜你喜欢</h3>
      <div class="related-grid">
        <router-link v-for="p in related" :key="p.id" :to="`/product/${p.id}`" class="related-card">
          <div class="related-img">
            <span class="img-placeholder">{{ p.name?.charAt(0) }}</span>
          </div>
          <p class="related-name">{{ p.name }}</p>
          <p class="related-price">&yen;{{ p.price }}</p>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import tempBanner1 from '@/assets/img/temp/category-banner.jpg'
import { getImageUrl } from '@/api/product'
import { useAuthStore } from '@/stores/auth'
import { getProductDetail, addToCart, buyNow } from '../api'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const images = ref([])
const related = ref([])
const amount = ref(1)
const mainImage = ref('')
const loading = ref(false)
const seckillInfo = ref(null)
let seckillTimer = null

const formatCountdown = (sec) => {
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const s = sec % 60
  return `${h}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
}

const goSeckill = () => router.push('/seckill')

onMounted(async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
    images.value = res.data?.images || []
    related.value = res.data?.related || []
    mainImage.value = images.value[0]?.url || ''
    // Check seckill
    const sessionsRes = await request.get('/seckill/sessions')
    const sessions = sessionsRes.data || []
    const match = sessions.find(s => s.productId === product.value?.id)
    if (match) {
      seckillInfo.value = { ...match, countdown: Math.max(0, Math.floor((new Date(match.endTime).getTime() - Date.now()) / 1000)) }
      seckillTimer = setInterval(() => {
        if (seckillInfo.value) seckillInfo.value.countdown = Math.max(0, seckillInfo.value.countdown - 1)
      }, 1000)
    }
  } catch {
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => clearInterval(seckillTimer))

const checkAuth = () => {
  if (!useAuthStore().isLogin) { router.push('/login'); return false }
  return true
}

const handleAddCart = async () => {
  if (!checkAuth()) return
  try {
    await addToCart({ productId: product.value.id, amount: amount.value })
    ElMessage.success('已加入购物车')
  } catch {
    // request interceptor handles the error message
  }
}

const handleBuyNow = () => {
  if (!checkAuth()) return
  router.push({ name: 'Checkout', query: { productId: product.value.id, amount: amount.value } })
}
</script>

<style scoped>
.wrapper { width: 1160px; margin: 0 auto; padding: 24px 0; }

/* ====== Breadcrumb ====== */
.breadcrumb { font-size: 13px; color: #999; margin-bottom: 24px; }
.breadcrumb a { color: #666; text-decoration: none; }
.breadcrumb a:hover { color: #A10000; }
.sep { margin: 0 8px; color: #ccc; }
.current { color: #333; }

/* ====== Detail Layout ====== */
.detail { display: flex; gap: 48px; }

/* ====== Gallery ====== */
.gallery { width: 540px; flex-shrink: 0; }
.main-image { border-radius: 12px; overflow: hidden; background: #fafafa; }
.main-image img { width: 100%; aspect-ratio: 1; object-fit: cover; display: block; }
.thumb-list { display: flex; gap: 10px; margin-top: 12px; }
.thumb { width: 72px; height: 72px; border-radius: 8px; overflow: hidden; cursor: pointer;
  border: 2px solid transparent; transition: all .2s; }
.thumb:hover { border-color: #ffcccc; }
.thumb.active { border-color: #A10000; }
.thumb img { width: 100%; height: 100%; object-fit: cover; }

/* ====== Info ====== */
.info { flex: 1; }
.product-name { font-size: 26px; font-weight: 600; color: #222; margin: 0 0 8px; }
.product-desc { font-size: 14px; color: #888; margin: 0 0 24px; line-height: 1.6; }

.price-box { background: linear-gradient(135deg, #fef5f5, #fff5f5); border-radius: 12px;
  padding: 16px 20px; margin-bottom: 20px; display: flex; align-items: baseline; gap: 16px; }
.price-label { font-size: 14px; color: #999; }
.price-value { font-size: 32px; font-weight: 700; color: #C10000; }
.price-value em { font-size: 18px; font-style: normal; margin-right: 2px; }

.meta { display: flex; gap: 32px; margin-bottom: 24px; }
.meta-item { display: flex; gap: 12px; }
.meta-item .label { font-size: 14px; color: #999; }
.meta-item .value { font-size: 14px; color: #333; }
.meta-item .value.low { color: #e6a23c; }
.meta-item .value.free { color: #67c23a; }

.qty-row { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.qty-row .label { font-size: 14px; color: #999; }
.qty-control { display: flex; align-items: center; border: 1px solid #e8e8e8; border-radius: 8px; overflow: hidden; }
.qty-control button { width: 36px; height: 36px; border: none; background: #fafafa; font-size: 18px;
  color: #555; cursor: pointer; transition: all .15s; display: flex; align-items: center; justify-content: center; }
.qty-control button:hover:not(:disabled) { background: #A10000; color: #fff; }
.qty-control button:disabled { color: #ddd; cursor: not-allowed; }
.qty-value { width: 52px; text-align: center; font-size: 16px; font-weight: 600; color: #333; }

.btn-row { display: flex; gap: 14px; }
.btn-cart { flex: 1; height: 48px; border: 2px solid #A10000; border-radius: 24px; background: #fff;
  color: #A10000; font-size: 16px; font-weight: 500; cursor: pointer; display: flex;
  align-items: center; justify-content: center; gap: 8px; transition: all .2s; }
.btn-cart:hover { background: #fef5f5; }
.btn-buy { flex: 1; height: 48px; border: none; border-radius: 24px; background: linear-gradient(135deg, #A10000, #c10000);
  color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; transition: all .2s; }
.btn-buy:hover { box-shadow: 0 4px 16px rgba(161,0,0,0.3); transform: translateY(-1px); }

/* ====== Related ====== */
.related { margin-top: 60px; }
.related h3 { font-size: 20px; font-weight: 600; text-align: center; margin: 0 0 24px; }
.related-grid { display: flex; justify-content: center; gap: 24px; flex-wrap: wrap; }
.related-card { width: 200px; text-decoration: none; color: inherit; border-radius: 10px;
  overflow: hidden; transition: all .2s; border: 1px solid #f0f0f0; }
.related-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.related-img { width: 200px; height: 200px; background: #fafafa; }
.img-placeholder { width: 100%; height: 100%; display: flex; align-items: center;
  justify-content: center; font-size: 40px; color: #ddd; }
.related-name { font-size: 14px; color: #333; padding: 10px 10px 4px; margin: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.related-price { color: #A10000; font-size: 16px; font-weight: 600; padding: 0 10px 12px; margin: 0; }

/* Seckill Bar */
.seckill-bar { display: flex; align-items: center; gap: 16px; padding: 16px 20px;
  background: linear-gradient(135deg, #fff5f5, #ffe8e8); border-radius: 10px;
  border: 1px solid #ffcccc; margin-bottom: 20px; }
.seckill-bar-icon { font-size: 20px; }
.seckill-bar-price { font-size: 16px; color: #666; }
.seckill-bar-price em { font-size: 26px; font-weight: 700; color: #C10000; font-style: normal; margin-left: 4px; }
.seckill-bar-countdown { margin-left: auto; font-size: 15px; font-weight: 600; color: #C10000; }
.seckill-btn { background: linear-gradient(135deg, #C10000, #E60000) !important;
  font-size: 18px !important; letter-spacing: 2px; }
</style>
