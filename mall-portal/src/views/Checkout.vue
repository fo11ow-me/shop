<template>
  <div class="checkout-page wrapper">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link to="/cart">购物车</router-link>
      <span class="sep">/</span>
      <span class="current">确认订单</span>
    </div>
    <div class="checkout-wrap">
      <Sidebar active="cart" />

      <div class="main-area">
        <StepBar :current="showSuccess ? 2 : 1" />

        <div v-if="showSuccess" class="success-block">
          <p class="success-msg">订单提交成功！即将跳转至订单页…</p>
          <button class="btn-pay-now" @click="openPayDialog">立即支付</button>
        </div>

        <template v-else>

          <div class="checkout-layout">
            <div class="main-col">
              <div class="block">
                <div class="block-title"><span class="dot"></span>收件信息</div>
                <div class="address-card">
                  <div class="addr-info">
                    <el-form ref="formRef" :model="form" :rules="rules" class="addr-form">
                      <el-form-item prop="recipientName">
                        <el-input v-model="form.recipientName" placeholder="请输入收件人姓名" size="large" />
                      </el-form-item>
                      <el-form-item prop="phone">
                        <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
                      </el-form-item>
                      <el-form-item prop="address">
                        <el-input v-model="form.address" placeholder="请输入收货地址" size="large" />
                      </el-form-item>
                    </el-form>
                  </div>
                  <div v-if="!hasAddress" class="address-warning">
                    <el-icon><WarningFilled /></el-icon>
                    <span>请先填写收货地址</span>
                  </div>
                </div>
              </div>

              <div class="block">
                <div class="block-title"><span class="dot"></span>配送方式</div>
                <div class="option-row">
                  <span v-for="(s, i) in shippings" :key="i" :class="['option-tag', { active: shipping === i }]"
                    @click="shipping = i">{{ s }}</span>
                </div>
              </div>
            </div>

            <div class="side-col">
              <div class="summary-card">
                <div class="sum-header">
                  <span>订单内容</span>
                  <router-link :to="backLink" class="back-link">{{ backLabel }}</router-link>
                </div>
                <div class="sum-body">
                  <div v-for="item in cartItems" :key="item.id" class="sum-item">
                    <span>{{ item.productName || '商品 #' + item.productId }}</span>
                    <span>x{{ item.productAmount || item.amount || 1 }}</span>
                  </div>
                </div>
                <div class="sum-lines">
                  <div class="sum-line"><span>商品金额</span><span>&yen;{{ totalPrice.toFixed(2) }}</span></div>
                  <div class="sum-line"><span>优惠</span><span class="discount">-&yen;0.00</span></div>
                  <div class="sum-line"><span>运费</span><span class="free">免运费</span></div>
                </div>
                <div class="sum-total">
                  <span>合计</span>
                  <span class="total-price">&yen;{{ totalPrice.toFixed(2) }}</span>
                </div>
                <button class="submit-btn" @click="handleSubmit" :disabled="submitting || !hasAddress">
                  {{ submitting ? '提交中...' : '提交订单' }}
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <PayDialog v-model:visible="payDialogVisible" :order-id="payingOrderId" @paid="onPaid" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import Sidebar from '../components/Sidebar.vue'
import StepBar from '../components/StepBar.vue'
import PayDialog from '../components/PayDialog.vue'
import { useUserStore } from '@/stores/user'
import { createOrderFromCart, buyNow, getCartList, getProductDetail, updateUserInfo } from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const showSuccess = ref(false)
const shipping = ref(0)
const cartItems = ref([])
const formRef = ref(null)
const form = reactive({ recipientName: '', phone: '', address: '' })

const redirectTimer = ref(null)

const payDialogVisible = ref(false)
const payingOrderId = ref(null)

const openPayDialog = () => {
  payDialogVisible.value = true
  clearTimeout(redirectTimer.value)
  redirectTimer.value = setTimeout(() => router.push('/orders'), 5000)
}

const onPaid = () => {
  clearTimeout(redirectTimer.value)
  router.push('/orders')
}

const rules = {
  recipientName: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

const shippings = ['顺丰快递', '百世汇通', '圆通快递', '中通快递']

const isBuyNow = computed(() => !!route.query.productId)
const buyNowProductId = computed(() => Number(route.query.productId) || 0)
const buyNowAmount = computed(() => Number(route.query.amount) || 1)
const backLink = computed(() => isBuyNow.value ? '/' : '/cart')
const backLabel = computed(() => isBuyNow.value ? '← 返回首页' : '← 返回购物车')

const cartIds = computed(() => {
  const ids = route.query.cartIds
  return ids ? ids.split(',').map(Number) : []
})

const hasAddress = computed(() => form.address && form.address.trim().length > 0)

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    const price = item.productPrice || 0
    const qty = item.amount || 1
    return sum + price * qty
  }, 0)
})

onMounted(async () => {
  try {
    await userStore.fetchUserInfo()
    form.recipientName = userStore.user?.name || userStore.user?.code || ''
    form.phone = userStore.user?.phone || ''
    form.address = userStore.user?.address || ''

    if (isBuyNow.value) {
      const productRes = await getProductDetail(buyNowProductId.value)
      const product = productRes.data
      cartItems.value = [{
        productName: product?.name || '商品',
        productPrice: product?.price || 0,
        amount: buyNowAmount.value
      }]
    } else {
      const cartRes = await getCartList()
      const allItems = cartRes.data || []
      cartItems.value = allItems.filter(item => cartIds.value.includes(item.id))
    }
  } catch {
    ElMessage.error('加载订单信息失败')
  }
})

onUnmounted(() => {
  if (redirectTimer.value) {
    clearTimeout(redirectTimer.value)
  }
})

const handleSubmit = async () => {
  if (submitting.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    // 1. Save address info to user profile first
    await updateUserInfo({ phone: form.phone, address: form.address })

    // 2. Create order
    let res
    if (isBuyNow.value) {
      res = await buyNow({ productId: buyNowProductId.value, amount: buyNowAmount.value, recipientName: form.recipientName, recipientPhone: form.phone, recipientAddress: form.address, expressDelivery: shipping.value })
    } else {
      res = await createOrderFromCart({
        cartIds: cartIds.value,
        addressId: userStore.user?.id,
        recipientName: form.recipientName,
        recipientPhone: form.phone,
        recipientAddress: form.address,
        expressDelivery: shipping.value
      })
    }
    payingOrderId.value = res.data?.id
    ElMessage.success('下单成功')
    showSuccess.value = true
    redirectTimer.value = setTimeout(() => router.push('/orders'), 3000)
  } catch (e) {
    if (e?.code === 1610 || (e?.message && e.message.includes('收货地址'))) {
      ElMessage.error('请先添加收货地址')
    } else {
      ElMessage.error(e?.message || '下单失败')
    }
    submitting.value = false
  }
}
</script>

<style scoped>
.wrapper { width: 1160px; margin: 0 auto; padding: 24px 0; }
.breadcrumb { font-size: 13px; color: #999; margin-bottom: 24px; }
.breadcrumb a { color: #666; text-decoration: none; }
.breadcrumb a:hover { color: #A10000; }
.sep { margin: 0 8px; color: #ccc; }
.current { color: #333; }
.checkout-wrap { display: flex; gap: 24px; }
.main-area { flex: 1; min-width: 0; }
.step-banner { margin-bottom: 24px; }
.step-banner img { width: 100%; display: block; }
.success-block { text-align: center; padding: 60px 0; }
.success-msg { font-size: 16px; color: #666; margin: 0 0 24px; }
.go-orders { display: inline-block; padding: 10px 32px; background: #A10000; color: #fff;
  border-radius: 24px; text-decoration: none; font-size: 14px; transition: all .2s; }
.go-orders:hover { background: #c10000; }
.page-title { font-size: 24px; font-weight: 600; margin: 0 0 28px; }
.checkout-layout { display: flex; gap: 28px; align-items: flex-start; }

.main-col { flex: 1; }

.block { margin-bottom: 24px; }
.block-title { font-size: 16px; font-weight: 600; color: #222; margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #A10000; display: inline-block; }

.address-card { background: #fdf8f6; border: 1px solid #fce4dc; border-radius: 10px; padding: 20px 24px; }
.addr-name { font-size: 15px; font-weight: 500; color: #333; margin: 0 0 8px; display: flex; align-items: center; gap: 6px; }
.addr-detail { font-size: 13px; color: #888; margin: 0 0 4px; }
.addr-form .el-form-item { margin-bottom: 12px; }
.addr-form .el-form-item:last-child { margin-bottom: 0; }
.address-warning { display: flex; align-items: center; gap: 6px; margin-top: 12px;
  padding: 8px 14px; background: #fef0f0; border: 1px solid #fde2e2;
  border-radius: 8px; color: #C10000; font-size: 13px; }

.option-row { display: flex; gap: 10px; flex-wrap: wrap; }
.option-item { width: 72px; height: 36px; border: 2px solid #eee; border-radius: 8px; overflow: hidden;
  cursor: pointer; transition: all .15s; display: flex; align-items: center; justify-content: center; }
.option-item img { max-width: 100%; max-height: 100%; object-fit: contain; }
.option-item:hover, .option-item.active { border-color: #A10000; }
.option-tag { padding: 7px 18px; border: 2px solid #eee; border-radius: 20px; font-size: 13px;
  color: #666; cursor: pointer; transition: all .15s; }
.option-tag:hover, .option-tag.active { border-color: #A10000; color: #A10000; background: #fef5f5; }

.side-col { width: 380px; flex-shrink: 0; }
.summary-card { background: #fff; border: 1px solid #eee; border-radius: 12px; overflow: hidden; position: sticky; top: 100px; }
.sum-header { display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid #f0f0f0; font-size: 15px; font-weight: 500; }
.back-link { font-size: 12px; color: #999; text-decoration: none; font-weight: 400; }
.back-link:hover { color: #A10000; }
.sum-body { padding: 12px 20px; }
.sum-item { display: flex; justify-content: space-between; padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0; font-size: 13px; color: #666; }
.sum-lines { padding: 12px 20px; border-top: 1px solid #f0f0f0; }
.sum-line { display: flex; justify-content: space-between; font-size: 13px; color: #888; margin-bottom: 6px; }
.discount { color: #C10000; }
.free { color: #67c23a; }
.sum-total { display: flex; justify-content: space-between; align-items: baseline;
  padding: 16px 20px; border-top: 1px solid #e0e0e0; font-size: 15px; font-weight: 500; }
.total-price { font-size: 24px; font-weight: 700; color: #C10000; }

.submit-btn { width: calc(100% - 40px); margin: 0 20px 20px; height: 48px; border: none;
  border-radius: 24px; background: linear-gradient(135deg, #A10000, #c10000); color: #fff;
  font-size: 16px; font-weight: 600; cursor: pointer; transition: all .2s; }
.submit-btn:hover:not(:disabled) { box-shadow: 0 4px 16px rgba(161,0,0,0.3); transform: translateY(-1px); }
.submit-btn:disabled { background: #ccc; cursor: not-allowed; }

.btn-pay-now { display: block; margin: 0 auto 24px; padding: 10px 32px; border: none; border-radius: 24px;
  background: linear-gradient(135deg, #A10000, #c10000); color: #fff; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all .2s; }
.btn-pay-now:hover { box-shadow: 0 4px 16px rgba(161,0,0,0.3); transform: translateY(-1px); }

</style>
