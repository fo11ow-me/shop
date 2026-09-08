<template>
  <div class="cart-page" v-loading="loading">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <span class="current">购物车</span>
    </div>

    <div class="cart-layout">
      <Sidebar active="cart" />
      <div class="main-area">
        <StepBar :current="0" />

        <!-- Empty state -->
        <div v-if="!cartList.length" class="empty-cart">
          <el-icon :size="80" color="#ddd"><ShoppingCart /></el-icon>
          <p>购物车是空的</p>
          <router-link to="/" class="go-shop">去逛逛</router-link>
        </div>

        <template v-else>
          <div class="cart-table">
            <div class="cart-header">
              <span class="col-check"><el-checkbox v-model="allChecked" @change="toggleAll" /></span>
              <span class="col-product">商品</span>
              <span class="col-price">单价</span>
              <span class="col-qty">数量</span>
              <span class="col-subtotal">小计</span>
              <span class="col-action">操作</span>
            </div>

            <div v-for="item in cartList" :key="item.id" class="cart-item">
              <span class="col-check">
                <el-checkbox :model-value="selectedIds.includes(item.id)"
                  @change="(v) => toggleItem(item.id, v)" />
              </span>
              <span class="col-product">
                <div class="product-cell">
                  <router-link :to="'/product/' + item.productId" class="product-img">
                    <img v-if="item.productImg" :src="getImageUrl(item.productImg)" />
                    <el-icon v-else :size="32" color="#ddd"><Goods /></el-icon>
                  </router-link>
                  <div class="product-info">
                    <p class="product-title">{{ item.productName || '商品 #' + item.productId }}</p>
                    <p class="product-cat">分类</p>
                  </div>
                </div>
              </span>
              <span class="col-price">&yen;{{ item.productPrice || '0.00' }}</span>
              <span class="col-qty">
                <div class="qty-control">
                  <button @click="updateQty(item, -1)" :disabled="item.amount <= 1">−</button>
                  <span>{{ item.amount }}</span>
                  <button @click="updateQty(item, 1)">+</button>
                </div>
              </span>
              <span class="col-subtotal">&yen;{{ ((item.productPrice || 0) * item.amount).toFixed(2) }}</span>
              <span class="col-action">
                <button class="del-btn" @click="handleDelete(item.id)">删除</button>
              </span>
            </div>
          </div>

          <div class="cart-bottom">
            <div class="bottom-left">
              <el-checkbox v-model="allChecked" @change="toggleAll">全选</el-checkbox>
              <button class="batch-del" @click="batchDelete" :disabled="!selectedIds.length">批量删除</button>
            </div>
            <div class="bottom-right">
              <span class="summary">已选 <em>{{ selectedIds.length }}</em> 件，合计</span>
              <span class="total">&yen;{{ totalPrice.toFixed(2) }}</span>
              <button class="checkout-btn" @click="handleCheckout" :disabled="!selectedIds.length">去结算</button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Goods } from '@element-plus/icons-vue'
import { getImageUrl } from '@/api/product'
import Sidebar from '../components/Sidebar.vue'
import StepBar from '../components/StepBar.vue'
import { getCartList, updateCartAmount, deleteCartItem, batchDeleteCart } from '../api'

const router = useRouter()
const cartList = ref([])
const loading = ref(false)
const selectedIds = ref([])
const allChecked = ref(false)
const totalPrice = computed(() => {
  let t = 0
  cartList.value.forEach(item => {
    if (selectedIds.value.includes(item.id)) t += (item.productPrice || 0) * item.amount
  })
  return t
})

const toggleAll = () => {
  selectedIds.value = allChecked.value ? cartList.value.map(i => i.id) : []
}
const toggleItem = (id, checked) => {
  if (checked) selectedIds.value.push(id)
  else selectedIds.value = selectedIds.value.filter(s => s !== id)
  allChecked.value = selectedIds.value.length === cartList.value.length
}

const load = async () => {
  loading.value = true
  try { const res = await getCartList(); cartList.value = res.data || [] }
  catch { ElMessage.error('加载购物车失败') }
  finally { loading.value = false }
}
load()

const updateQty = async (item, delta) => {
  const n = item.amount + delta
  if (n < 1) return
  if (delta > 0 && item.productStock && n > item.productStock) { ElMessage.warning('已达到最大库存'); return }
  item.amount = n
  try { await updateCartAmount({ id: item.id, amount: n }) } catch { item.amount -= delta }
}

const handleDelete = async (id) => {
  try {
    await deleteCartItem(id)
    selectedIds.value = selectedIds.value.filter(s => s !== id)
    ElMessage.success('已删除')
    load()
  } catch { ElMessage.error('删除失败') }
}

const batchDelete = async () => {
  if (!selectedIds.value.length) { ElMessage.warning('请选择'); return }
  try {
    await batchDeleteCart(selectedIds.value)
    ElMessage.success('已批量删除')
    selectedIds.value = []
    allChecked.value = false
    load()
  } catch { ElMessage.error('批量删除失败') }
}

const handleCheckout = () => {
  if (!selectedIds.value.length) { ElMessage.warning('请选择商品'); return }
  router.push({ name: 'Checkout', query: { cartIds: selectedIds.value.join(',') } })
}
</script>

<style scoped>
.cart-page { width: 1160px; margin: 0 auto; padding: 24px 0; }
.breadcrumb { font-size: 13px; color: #999; margin-bottom: 24px; }
.breadcrumb a { color: #666; text-decoration: none; }
.breadcrumb a:hover { color: #A10000; }
.sep { margin: 0 8px; color: #ccc; }
.current { color: #333; }

.step-banner { margin-bottom: 20px; }
.step-banner img { width: 100%; display: block; }

.cart-layout { display: flex; gap: 24px; }
.main-area { flex: 1; }
.page-title { font-size: 24px; font-weight: 600; margin: 0 0 4px; }
.count { font-size: 16px; color: #999; font-weight: 400; }
.keep-shopping { font-size: 13px; color: #888; text-decoration: none; }
.keep-shopping:hover { color: #A10000; }

/* ====== Empty ====== */
.empty-cart { text-align: center; padding: 80px 0; }
.empty-cart p { font-size: 16px; color: #999; margin: 20px 0; }
.go-shop { display: inline-block; padding: 10px 32px; background: #A10000; color: #fff;
  border-radius: 24px; text-decoration: none; font-size: 14px; transition: all .2s; }
.go-shop:hover { background: #c10000; }

/* ====== Cart Table ====== */
.cart-table { margin-top: 20px; border-radius: 10px; overflow: hidden; border: 1px solid #eee; }
.cart-header { display: flex; align-items: center; height: 44px; background: #fafafa;
  font-size: 14px; color: #666; border-bottom: 1px solid #eee; }
.cart-item { display: flex; align-items: center; padding: 16px 0; border-bottom: 1px solid #f5f5f5;
  transition: background .15s; }
.cart-item:hover { background: #fefefe; }
.cart-item:last-child { border-bottom: none; }

.col-check { width: 56px; display: flex; justify-content: center; }
.col-product { flex: 4; }
.col-price { flex: 1; text-align: center; font-size: 14px; color: #333; }
.col-qty { flex: 1.5; display: flex; justify-content: center; }
.col-subtotal { flex: 1; text-align: center; font-size: 15px; font-weight: 600; color: #C10000; }
.col-action { width: 80px; text-align: center; }

.product-cell { display: flex; align-items: center; gap: 14px; }
.product-img { width: 100px; height: 100px; border-radius: 8px; overflow: hidden;
  background: #fafafa; display: flex; align-items: center; justify-content: center; }
.product-img img { width: 100%; height: 100%; object-fit: cover; }
.product-title { font-size: 14px; color: #333; margin: 0 0 4px; }
.product-cat { font-size: 12px; color: #aaa; margin: 0; }

.qty-control { display: flex; align-items: center; border: 1px solid #e8e8e8; border-radius: 6px; overflow: hidden; }
.qty-control button { width: 30px; height: 30px; border: none; background: #fafafa; font-size: 16px;
  color: #666; cursor: pointer; transition: all .15s; }
.qty-control button:hover:not(:disabled) { background: #A10000; color: #fff; }
.qty-control button:disabled { color: #ddd; cursor: not-allowed; }
.qty-control span { width: 40px; text-align: center; font-size: 14px; font-weight: 500; }

.del-btn { padding: 4px 12px; border: 1px solid #e8e8e8; border-radius: 4px; background: #fff;
  color: #999; font-size: 12px; cursor: pointer; transition: all .15s; }
.del-btn:hover { color: #A10000; border-color: #A10000; }

/* ====== Bottom ====== */
.cart-bottom { display: flex; justify-content: space-between; align-items: center;
  margin-top: 20px; padding: 20px 24px; background: #fafafa; border-radius: 10px; }
.bottom-left { display: flex; align-items: center; gap: 16px; }
.batch-del { padding: 6px 16px; border: 1px solid #e8e8e8; border-radius: 4px; background: #fff;
  color: #999; font-size: 13px; cursor: pointer; transition: all .15s; }
.batch-del:hover:not(:disabled) { color: #A10000; border-color: #A10000; }
.batch-del:disabled { opacity: 0.4; cursor: not-allowed; }
.bottom-right { display: flex; align-items: baseline; gap: 12px; }
.summary { font-size: 14px; color: #666; }
.summary em { color: #A10000; font-style: normal; font-weight: 600; }
.total { font-size: 24px; font-weight: 700; color: #C10000; }
.checkout-btn { padding: 12px 40px; border: none; border-radius: 24px;
  background: linear-gradient(135deg, #A10000, #c10000); color: #fff; font-size: 16px;
  font-weight: 600; cursor: pointer; transition: all .2s; }
.checkout-btn:hover:not(:disabled) { box-shadow: 0 4px 16px rgba(161,0,0,0.3); }
.checkout-btn:disabled { background: #ccc; cursor: not-allowed; }
</style>
