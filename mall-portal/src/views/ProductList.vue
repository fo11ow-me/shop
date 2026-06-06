<template>
  <div class="category-page" v-loading="loading">
    <!-- Banner -->
    <div class="cat-banner">
      <img :src="tempBanner1" />
      <div class="banner-overlay"><h2>品质家居</h2></div>
    </div>

    <div class="wrapper">
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="sep">/</span>
        <span class="current">{{ categoryName || '全部商品' }}</span>
      </div>

      <div class="sort-bar">
        <span>共 <em>{{ total }}</em> 件商品</span>
      </div>

      <div v-if="!products.length" class="empty">
        <el-empty description="暂无商品" :image-size="100" />
      </div>

      <div v-else class="product-grid">
        <ProductCard v-for="p in products" :key="p.id"
          :product="p" :image-key="productImages[p.id] || ''" />
      </div>

      <div v-if="total > size" style="text-align:center;margin-top:32px">
        <el-pagination background :current-page="page" :total="total" :page-size="size"
          @current-change="load" layout="prev, pager, next" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import tempBanner1 from '@/assets/img/temp/banner1.jpg'
import { getProductsByCategory, getCategories } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const products = ref([])
const productImages = ref({})
const categoryName = ref('')
const page = ref(1)
const total = ref(0)
const size = 12
const loading = ref(false)
let catTree = []

const findCategoryName = (id) => {
  const search = (list) => {
    for (const c of list) {
      if (c.id === Number(id)) return c.name
      if (c.children?.length) {
        const found = search(c.children)
        if (found) return found
      }
    }
    return null
  }
  return search(catTree) || ''
}

const load = async (p = 1) => {
  page.value = p
  loading.value = true
  try {
    const id = route.params.id
    if (!id) return
    const res = await getProductsByCategory(id, { current: p, size })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
    productImages.value = res.data?.productImages || {}
    categoryName.value = findCategoryName(id)
  } catch {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const initPage = async () => {
  try {
    const catRes = await getCategories()
    catTree = catRes.data || []
  } catch { /* ignore */ }
  load(1)
}
initPage()

watch(() => route.params.id, () => { page.value = 1; load(1) })
</script>

<style scoped>
.cat-banner { position: relative; margin-bottom: 24px; }
.cat-banner img { width: 100%; max-height: 200px; object-fit: cover; display: block; }
.banner-overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.2); }
.banner-overlay h2 { color: #fff; font-size: 28px; font-weight: 600; margin: 0; }

.wrapper { width: 1160px; margin: 0 auto; padding-bottom: 32px; }
.breadcrumb { font-size: 13px; color: #999; margin-bottom: 12px; }
.breadcrumb a { color: #666; text-decoration: none; }
.breadcrumb a:hover { color: #A10000; }
.sep { margin: 0 8px; color: #ccc; }
.current { color: #333; }

.sort-bar { height: 48px; line-height: 48px; border-bottom: 1px solid #eee; margin-bottom: 20px;
  font-size: 14px; color: #888; }
.sort-bar em { color: #A10000; font-weight: 600; font-style: normal; }

.empty { padding: 60px 0; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
</style>
