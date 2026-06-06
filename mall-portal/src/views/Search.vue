<template>
  <div class="search-page wrapper" v-loading="loading">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <span class="current">搜索"{{ keyword }}"</span>
    </div>

    <div class="result-head">
      <span>共找到 <em>{{ total }}</em> 件商品</span>
    </div>

    <div v-if="!products.length" class="empty-result">
      <el-icon :size="60" color="#ddd"><Search /></el-icon>
      <p>未找到相关商品</p>
      <span>试试其他关键词吧</span>
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
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { searchProducts } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const products = ref([])
const productImages = ref({})
const page = ref(1)
const total = ref(0)
const size = 12
const keyword = ref('')
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    keyword.value = route.query.keyword || ''
    const res = await searchProducts({ keyword: keyword.value, page: page.value, size })
    products.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
    productImages.value = res.data?.productImages || {}
  } catch {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}
load()
</script>

<style scoped>
.wrapper { width: 1160px; margin: 0 auto; padding: 24px 0; }
.breadcrumb { font-size: 13px; color: #999; margin-bottom: 16px; }
.breadcrumb a { color: #666; text-decoration: none; }
.breadcrumb a:hover { color: #A10000; }
.sep { margin: 0 8px; color: #ccc; }
.current { color: #333; }

.result-head { margin-bottom: 20px; font-size: 14px; color: #888; }
.result-head em { color: #A10000; font-weight: 600; font-style: normal; }

.empty-result { text-align: center; padding: 80px 0; }
.empty-result p { font-size: 16px; color: #999; margin: 16px 0 8px; }
.empty-result span { font-size: 13px; color: #bbb; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
</style>
