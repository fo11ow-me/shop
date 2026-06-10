<template>
  <div class="order-wrap">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <span class="current">订单</span>
    </div>

    <div class="order-layout">
      <Sidebar active="orders" />

      <div class="main-area">
        <div class="order-tabs">
          <span v-for="t in tabs" :key="t.value" :class="['tab-item', { active: activeTab === t.value }]"
            @click="switchTab(t.value)">{{ t.label }}</span>
        </div>

        <el-empty v-if="!filteredOrders.length" description="暂无订单" :image-size="80" />

        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header" @click="toggleExpand(order.id)">
            <div class="header-left">
              <span class="order-status" :class="'status-' + order.status">{{ statusLabel(order.status) }}</span>
              <span class="order-sn">订单号: {{ order.orderSn || order.id }}</span>
            </div>
            <div class="header-right">
              <span class="order-date">{{ order.createTime || order.gmtCreate }}</span>
              <el-icon :class="['expand-icon', { expanded: expandedId === order.id }]"><ArrowDown /></el-icon>
            </div>
          </div>

          <div class="order-meta">
            <span>共 {{ order.items?.length || 0 }} 件商品</span>
            <span class="order-amount"><em>&yen;{{ (order.totalAmount || 0).toFixed(2) }}</em></span>
          </div>

          <div class="order-actions">
            <button v-if="order.status === 0" class="btn-pay" @click="openPayDialog(order.id)">立即支付</button>
            <button v-if="order.status === 0" class="btn-cancel" @click="handleCancel(order.id)">取消订单</button>
            <button v-if="order.status === 2" class="btn-receipt" @click="handleReceipt(order.id)">确认收货</button>
            <button v-if="order.status === 3" class="btn-del" @click="handleDelete(order.id)">删除</button>
          </div>

          <div v-if="expandedId === order.id" class="order-detail">
            <!-- Recipient Info -->
            <div class="detail-section">
              <div class="section-head">
                <span class="section-title">收件人信息</span>
                <button v-if="order.status === 0" class="edit-btn" @click="startEditRecipient(order)">
                  {{ editingId === order.id ? '取消' : '编辑' }}
                </button>
              </div>
              <div v-if="editingId === order.id" class="edit-form">
                <el-form :model="editForm" size="small" label-width="80px">
                  <el-form-item label="收件人"><el-input v-model="editForm.recipientName" placeholder="请输入收件人" /></el-form-item>
                  <el-form-item label="手机号"><el-input v-model="editForm.recipientPhone" placeholder="请输入手机号" /></el-form-item>
                  <el-form-item label="地址"><el-input v-model="editForm.recipientAddress" placeholder="请输入地址" /></el-form-item>
                </el-form>
                <el-button type="primary" size="small" @click="saveRecipient(order.id)">保存</el-button>
              </div>
              <div v-else class="info-grid">
                <div class="info-item"><span class="info-label">收件人</span><span>{{ order.recipientName || '-' }}</span></div>
                <div class="info-item"><span class="info-label">手机号</span><span>{{ order.recipientPhone || '-' }}</span></div>
                <div class="info-item full"><span class="info-label">地址</span><span>{{ order.recipientAddress || '-' }}</span></div>
              </div>
            </div>

            <!-- Order Items -->
            <div class="detail-section">
              <div class="section-head"><span class="section-title">商品明细</span></div>
              <el-table :data="order.items || []" border size="small">
                <el-table-column label="商品" min-width="160" align="center">
                  <template #default="{ row }">
                    <div class="product-cell">
                      <img v-if="row.productImg" :src="getImageUrl(row.productImg)" class="thumb" />
                      <span v-else class="no-img">-</span>
                      <span>{{ row.productName }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="productPrice" label="单价" width="90" align="center">
                  <template #default="{ row }">&yen;{{ (row.productPrice || 0).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="amount" label="数量" width="60" align="center" />
                <el-table-column label="小计" width="90" align="center">
                  <template #default="{ row }">&yen;{{ ((row.productPrice || 0) * (row.amount || 0)).toFixed(2) }}</template>
                </el-table-column>
              </el-table>
            </div>

            <!-- Payment Info -->
            <div class="detail-section">
              <div class="section-head"><span class="section-title">支付信息</span></div>
              <div class="info-grid">
                <div class="info-item"><span class="info-label">支付方式</span><span>{{ payMethodLabel(order.payMethod) }}</span></div>
                <div v-if="order.status >= 1 && order.paymentTime" class="info-item">
                  <span class="info-label">支付时间</span><span>{{ order.paymentTime }}</span>
                </div>
                <div v-if="order.status >= 1 && order.paymentSn" class="info-item">
                  <span class="info-label">支付流水号</span><span>{{ order.paymentSn }}</span>
                </div>
              </div>
            </div>

            <!-- Delivery Info -->
            <div class="detail-section">
              <div class="section-head"><span class="section-title">配送信息</span></div>
              <div class="info-grid">
                <div class="info-item full"><span class="info-label">配送方式</span>
                  <span v-if="order.status === 0" class="delivery-tags">
                    <span v-for="(d, i) in deliveryOptions" :key="i"
                      :class="['option-tag', { active: orderDelivery[order.id] === i }]"
                      @click="selectDelivery(order.id, i)">{{ d }}</span>
                  </span>
                  <span v-else>{{ deliveryOptions[order.expressDelivery] || '普通配送' }}</span>
                </div>
                <div v-if="order.status >= 2 && order.deliveryTime" class="info-item">
                  <span class="info-label">发货时间</span><span>{{ order.deliveryTime }}</span>
                </div>
                <div v-if="order.status >= 3 && order.receiptTime" class="info-item">
                  <span class="info-label">收货时间</span><span>{{ order.receiptTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <PayDialog v-model:visible="payDialogVisible" :order-id="payingOrderId" @paid="load()" />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import Sidebar from '../components/Sidebar.vue'
import PayDialog from '../components/PayDialog.vue'
import { getOrderList, cancelOrder, receiptOrder, updateRecipient, deleteOrder } from '../api'
import { getImageUrl } from '../api'

const tabs = [
  { label: '全部', value: null },
  { label: '未支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 }
]

const statusMap = { 0: '未支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const payMethodMap = { 0: '-', 1: '微信支付', 2: '支付宝', 3: '银联支付' }

const statusLabel = (s) => statusMap[s] || '未知'
const payMethodLabel = (pm) => payMethodMap[pm] || pm || '-'

const activeTab = ref(null)
const allOrders = ref([])
const expandedId = ref(null)
const editingId = ref(null)
const editForm = reactive({ recipientName: '', recipientPhone: '', recipientAddress: '' })

const filteredOrders = computed(() => {
  if (activeTab.value === null) return allOrders.value
  return allOrders.value.filter(o => o.status === activeTab.value)
})

const switchTab = (val) => { activeTab.value = val; expandedId.value = null; editingId.value = null }

const toggleExpand = (id) => {
  if (expandedId.value === id) { expandedId.value = null; editingId.value = null }
  else expandedId.value = id
}

const load = async () => {
  try {
    const res = await getOrderList()
    allOrders.value = Array.isArray(res.data) ? res.data : (res.data?.records || res.data?.list || [])
    allOrders.value.forEach(order => {
      if (order.status === 0 && orderDelivery[order.id] === undefined) {
        orderDelivery[order.id] = order.expressDelivery || 0
      }
    })
  } catch { ElMessage.error('加载订单失败') }
}
load()

const startEditRecipient = (order) => {
  if (editingId.value === order.id) { editingId.value = null; return }
  editingId.value = order.id
  editForm.recipientName = order.recipientName || ''
  editForm.recipientPhone = order.recipientPhone || ''
  editForm.recipientAddress = order.recipientAddress || ''
}

const saveRecipient = async (id) => {
  try {
    await updateRecipient(id, editForm)
    ElMessage.success('保存成功')
    editingId.value = null
    load()
  } catch { ElMessage.error('保存失败') }
}

const payDialogVisible = ref(false)
const payingOrderId = ref(null)

const deliveryOptions = ['顺丰快递', '百世汇通', '圆通快递', '中通快递']

const orderDelivery = reactive({})

const selectDelivery = async (orderId, index) => {
  orderDelivery[orderId] = index
  try {
    await updateRecipient(orderId, { expressDelivery: index })
    ElMessage.success('配送方式已更新')
  } catch { ElMessage.error('更新失败') }
}

const openPayDialog = (id) => {
  payingOrderId.value = id
  payDialogVisible.value = true
}

const handleReceipt = async (id) => {
  try {
    await ElMessageBox.confirm('确认已收到货物？', '确认收货', { type: 'warning' })
    await receiptOrder(id)
    ElMessage.success('已确认收货')
    load()
  } catch { /* 取消或失败 */ }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    await cancelOrder(id)
    ElMessage.success('已取消')
    load()
  } catch { /* 取消或失败 */ }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该订单？', '提示', { type: 'warning' })
    await deleteOrder(id)
    ElMessage.success('已删除')
    load()
  } catch { /* 取消或失败 */ }
}
</script>

<style scoped>
.order-wrap { width: 1160px; margin: 0 auto; padding: 24px 0; }
.breadcrumb { font-size: 13px; color: #999; margin-bottom: 24px; }
.breadcrumb a { color: #666; text-decoration: none; }
.breadcrumb a:hover { color: #A10000; }
.sep { margin: 0 8px; color: #ccc; }
.current { color: #333; }

.order-layout { display: flex; gap: 24px; }
.main-area { flex: 1; min-width: 0; }

.order-tabs { display: flex; gap: 0; margin-bottom: 24px; border-bottom: 2px solid #eee; }
.tab-item { padding: 10px 24px; font-size: 14px; color: #666; cursor: pointer; position: relative;
  transition: color .2s; user-select: none; }
.tab-item:hover { color: #A10000; }
.tab-item.active { color: #A10000; font-weight: 600; }
.tab-item.active::after { content: ''; position: absolute; bottom: -2px; left: 0; right: 0;
  height: 2px; background: #A10000; }

.order-card { background: #fff; border: 1px solid #eee; border-radius: 10px; padding: 20px 24px;
  margin-bottom: 16px; transition: all .2s; }
.order-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.04); }

.order-header { display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; border-bottom: 1px solid #f5f5f5; cursor: pointer; user-select: none; }
.header-left { display: flex; align-items: center; gap: 16px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.order-status { font-size: 15px; font-weight: 600; }
.status-0 { color: #C10000; }
.status-1 { color: #E6A23C; }
.status-2 { color: #409EFF; }
.status-3 { color: #67C23A; }
.status-4 { color: #909399; }
.order-sn { font-size: 13px; color: #888; }
.order-date { font-size: 13px; color: #aaa; }
.expand-icon { font-size: 18px; color: #999; transition: transform .25s; }
.expand-icon.expanded { transform: rotate(180deg); }

.order-meta { display: flex; justify-content: space-between; align-items: center;
  padding: 12px 0; font-size: 13px; color: #888; }
.order-amount em { font-size: 20px; font-weight: 700; color: #C10000; font-style: normal; }

.order-actions { display: flex; gap: 10px; padding-bottom: 6px; }
.btn-pay { padding: 8px 24px; border: none; border-radius: 20px; background: #A10000;
  color: #fff; font-size: 14px; cursor: pointer; transition: all .2s; }
.btn-pay:hover { background: #c10000; box-shadow: 0 2px 8px rgba(161,0,0,0.2); }
.btn-cancel { padding: 8px 20px; border: 1px solid #f5d0d0; border-radius: 20px; background: #fff;
  color: #C10000; font-size: 13px; cursor: pointer; transition: all .15s; }
.btn-cancel:hover { background: #fef0f0; border-color: #C10000; }
.btn-receipt { padding: 8px 24px; border: none; border-radius: 20px; background: #409EFF;
  color: #fff; font-size: 14px; cursor: pointer; transition: all .2s; }
.btn-receipt:hover { background: #66b1ff; box-shadow: 0 2px 8px rgba(64,158,255,0.2); }
.btn-del { padding: 8px 20px; border: 1px solid #ddd; border-radius: 20px; background: #fff;
  color: #888; font-size: 13px; cursor: pointer; transition: all .15s; }
.btn-del:hover { color: #666; border-color: #bbb; }

.order-detail { margin-top: 14px; padding-top: 14px; border-top: 1px solid #f0f0f0; }
.detail-section { margin-bottom: 18px; }
.detail-section:last-child { margin-bottom: 0; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.section-title { font-size: 14px; font-weight: 600; color: #333; }
.edit-btn { padding: 4px 12px; border: 1px solid #ddd; border-radius: 4px; background: #fff;
  color: #A10000; font-size: 12px; cursor: pointer; }
.edit-btn:hover { border-color: #A10000; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 20px; }
.info-item { display: flex; gap: 8px; font-size: 13px; color: #666; }
.info-item.full { grid-column: 1 / -1; }
.info-label { color: #999; flex-shrink: 0; }

.edit-form { margin-bottom: 12px; padding: 14px; background: #fafafa; border-radius: 8px; }
.edit-form .el-form-item { margin-bottom: 8px; }
.edit-form .el-form-item:last-child { margin-bottom: 12px; }

.product-cell { display: flex; align-items: center; gap: 8px; }
.thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; }
.no-img { color: #ccc; font-size: 12px; }

.option-tag { padding: 5px 14px; border: 2px solid #eee; border-radius: 20px; font-size: 12px;
  color: #666; cursor: pointer; transition: all .15s; white-space: nowrap; }
.option-tag:hover, .option-tag.active { border-color: #A10000; color: #A10000; background: #fef5f5; }
.delivery-tags { display: flex; gap: 6px; flex-wrap: wrap; }
</style>
