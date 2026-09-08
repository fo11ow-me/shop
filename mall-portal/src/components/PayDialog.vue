<template>
  <el-dialog :model-value="visible" @update:model-value="$emit('update:visible', $event)"
    title="选择支付方式" width="480px" center :close-on-click-modal="false">
    <div class="pay-method-list">
      <div v-for="m in payMethods" :key="m.value"
        :class="['pay-method-card', { active: selectedPayMethod === m.value }]"
        @click="selectedPayMethod = m.value">
        <img :src="m.img" class="pay-method-img" />
        <el-icon v-if="selectedPayMethod === m.value" class="pay-check" color="#A10000" :size="20">
          <CircleCheckFilled />
        </el-icon>
      </div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="danger" @click="confirmPay" :disabled="selectedPayMethod === null" :loading="paying">
        确认支付
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { payOrder } from '@/api/order'
import way01 from '@/assets/img/temp/payment-alipay.jpg'
import way02 from '@/assets/img/temp/payment-wechat.jpg'
import way03 from '@/assets/img/temp/payment-card.jpg'

const props = defineProps({
  visible: Boolean,
  orderId: Number
})

const emit = defineEmits(['update:visible', 'paid'])

const selectedPayMethod = ref(null)
const paying = ref(false)

const payMethods = [
  { label: '支付宝', value: 2, img: way01 },
  { label: '微信支付', value: 1, img: way02 },
  { label: '银联支付', value: 3, img: way03 }
]

watch(() => props.visible, (v) => {
  if (v) selectedPayMethod.value = null
})

const confirmPay = async () => {
  paying.value = true
  try {
    await payOrder(props.orderId, selectedPayMethod.value)
    ElMessage.success('支付成功')
    emit('update:visible', false)
    emit('paid')
  } catch { ElMessage.error('支付失败') }
  finally { paying.value = false }
}
</script>

<style scoped>
.pay-method-list { display: flex; gap: 16px; justify-content: center; }
.pay-method-card { display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 12px; border: 2px solid #eee; border-radius: 10px; cursor: pointer;
  transition: all .15s; position: relative; width: 110px; }
.pay-method-card:hover { border-color: #A10000; }
.pay-method-card.active { border-color: #A10000; background: #fef5f5; }
.pay-method-img { width: 72px; height: 36px; object-fit: contain; border-radius: 4px; }
.pay-check { position: absolute; top: 6px; right: 6px; }
</style>
