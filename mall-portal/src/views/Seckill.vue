<template>
  <div class="seckill-page wrapper">
    <div class="seckill-hero">
      <h1>限时秒杀</h1>
      <p>超值好物，手慢无</p>
    </div>

    <!-- Active Sessions -->
    <section class="seckill-section">
      <div class="section-head">
        <h3><span class="dot pulse"></span>进行中</h3>
        <span class="section-badge">{{ activeSessions.length }} 场</span>
      </div>
      <div v-if="activeSessions.length === 0 && !loading" class="empty-tip">暂无进行中的秒杀活动</div>
      <div v-else class="seckill-grid">
        <div v-for="s in activeSessions" :key="s.sessionId" class="seckill-card">
          <div class="sc-img">
            <img v-if="s.productImg" :src="getImageUrl(s.productImg)" :alt="s.productName || '商品图片'" />
            <span v-else class="sc-placeholder">{{ s.productName?.charAt(0) || '?' }}</span>
          </div>
          <div class="sc-body">
            <p class="sc-name">{{ s.productName || '商品 #' + s.productId }}</p>
            <p class="sc-price">秒杀价 <em>&yen;{{ s.seckillPrice }}</em></p>
            <p class="sc-stock">剩余 {{ s.remainingStock }} 件</p>
            <div class="sc-countdown" :class="{ ending: s.countdown <= 60 }">
              <el-icon :size="14"><Clock /></el-icon>
              {{ s.countdown <= 0 ? '已结束' : formatCountdown(s.countdown) }}
            </div>
            <button class="sc-btn" :disabled="s.countdown <= 0 || s.pending" @click="execute(s.sessionId)">
              {{ s.pending ? '排队中...' : '立即秒杀' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Upcoming -->
    <section class="seckill-section">
      <div class="section-head">
        <h3><span class="dot"></span>即将开始</h3>
      </div>
      <div v-if="upcomingSessions.length === 0 && !loading" class="empty-tip">暂无即将开始的秒杀活动</div>
      <div v-else class="seckill-grid">
        <div v-for="s in upcomingSessions" :key="s.sessionId" class="seckill-card upcoming">
          <div class="sc-img">
            <img v-if="s.productImg" :src="getImageUrl(s.productImg)" :alt="s.productName || '商品图片'" />
            <span v-else class="sc-placeholder">{{ s.productName?.charAt(0) || '?' }}</span>
          </div>
          <div class="sc-body">
            <p class="sc-name">{{ s.productName || '商品 #' + s.productId }}</p>
            <p class="sc-price">秒杀价 <em>&yen;{{ s.seckillPrice }}</em></p>
            <p class="sc-time">{{ s.startTime }}</p>
            <span class="coming-tag">即将开始</span>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="showResult" title="秒杀结果" width="400px" align-center>
      <div class="result-box" :class="{ success: resultSuccess }">
        <span class="result-emoji">{{ resultSuccess ? '🎉' : '😞' }}</span>
        <p>{{ resultMsg }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getImageUrl } from '@/api/product'
import { Clock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const activeSessions = ref([])
const upcomingSessions = ref([])
const loading = ref(true)
const showResult = ref(false)
const resultSuccess = ref(false)
const resultMsg = ref('')
let countdownTimer = null
let pollTimer = null
let serverOffset = 0

onMounted(async () => {
  try {
    const start = Date.now()
    const res = await request.get('/seckill/server-time')
    const roundTrip = Date.now() - start
    serverOffset = (res.data - (start + roundTrip / 2)) / 1000
  } catch { serverOffset = 0 }
  loadSessions()
})

function serverNow() { return new Date(Date.now() + serverOffset * 1000) }
onUnmounted(() => { clearInterval(countdownTimer); clearInterval(pollTimer) })

function loadSessions() {
  loading.value = true
  request.get('/seckill/sessions').then(res => {
    const list = res.data || []
    const now = serverNow().getTime()
    activeSessions.value = list.map(s => ({
      ...s, pending: false,
      countdown: Math.max(0, Math.floor((new Date(s.endTime).getTime() - now) / 1000))
    }))
    startCountdown()
  }).catch(() => { ElMessage.error('加载秒杀活动失败') })
  request.get('/seckill/sessions/upcoming').then(res => {
    upcomingSessions.value = res.data || []
  }).catch(() => { ElMessage.error('加载即将开始的秒杀失败') }).finally(() => { loading.value = false })
}

function formatCountdown(sec) {
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const s = sec % 60
  return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function startCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    activeSessions.value = activeSessions.value.map(s => ({ ...s, countdown: Math.max(0, s.countdown - 1) }))
  }, 1000)
}

function execute(sessionId) {
  if (!authStore.isLogin) { router.push('/login'); return }
  activeSessions.value = activeSessions.value.map(s => s.sessionId === sessionId ? { ...s, pending: true } : s)
  request.post('/seckill/execute', null, { params: { sessionId } }).then(() => {
    startPolling(sessionId)
  }).catch(err => {
    activeSessions.value = activeSessions.value.map(s => s.sessionId === sessionId ? { ...s, pending: false } : s)
    showResultMsg(false, err?.message || '秒杀失败')
  })
}

function startPolling(sessionId) {
  clearInterval(pollTimer)
  let attempts = 0
  pollTimer = setInterval(() => {
    if (++attempts > 30) {
      clearInterval(pollTimer)
      activeSessions.value = activeSessions.value.map(s => s.sessionId === sessionId ? { ...s, pending: false } : s)
      showResultMsg(false, '排队超时，请查看订单')
      return
    }
    request.get(`/seckill/result/${sessionId}`).then(res => {
      const data = res.data
      if (data?.status === 1) {
        clearInterval(pollTimer)
        activeSessions.value = activeSessions.value.map(s => s.sessionId === sessionId ? { ...s, pending: false } : s)
        showResultMsg(true, data.msg || '恭喜您抢到商品！')
      } else if (data && (data.status === 2 || data.status === -1)) {
        clearInterval(pollTimer)
        activeSessions.value = activeSessions.value.map(s => s.sessionId === sessionId ? { ...s, pending: false } : s)
        showResultMsg(false, data.msg || '未抢到，下次加油！')
      }
    }).catch(() => {})
  }, 2000)
}

function showResultMsg(success, msg) { resultSuccess.value = success; resultMsg.value = msg; showResult.value = true }
</script>

<style scoped>
.wrapper { width: 1160px; margin: 0 auto; padding: 24px 0; }

.seckill-hero { background: linear-gradient(135deg, #A10000, #C10000); border-radius: 16px;
  padding: 48px; text-align: center; color: #fff; margin-bottom: 36px; }
.seckill-hero h1 { font-size: 36px; font-weight: 700; margin: 0 0 8px; }
.seckill-hero p { font-size: 16px; opacity: 0.85; margin: 0; }

.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-head h3 { font-size: 20px; font-weight: 600; color: #222; margin: 0; display: flex; align-items: center; gap: 10px; }
.dot { width: 10px; height: 10px; border-radius: 50%; background: #A10000; display: inline-block; }
.dot.pulse { animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
.section-badge { font-size: 13px; color: #A10000; background: #fef5f5; padding: 4px 14px; border-radius: 12px; }

.empty-tip { text-align: center; padding: 48px; color: #bbb; font-size: 15px; }

.seckill-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(265px, 1fr)); gap: 20px; margin-bottom: 40px; }

.seckill-card { background: #fff; border-radius: 14px; overflow: hidden; border: 1px solid #f0f0f0;
  transition: all .3s; }
.seckill-card:hover { box-shadow: 0 8px 30px rgba(0,0,0,0.06); transform: translateY(-2px); }
.seckill-card.upcoming { opacity: 0.75; }
.sc-img { height: 200px; background: #fafafa; display: flex; align-items: center; justify-content: center;
  color: #ccc; font-size: 14px; overflow: hidden; }
.sc-img img { width: 100%; height: 100%; object-fit: cover; }
.sc-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 56px; color: #ddd; background: #fafafa; }
.sc-body { padding: 20px; }
.sc-name { font-size: 16px; color: #333; margin: 0 0 8px; }
.sc-price { font-size: 14px; color: #999; margin: 0 0 6px; }
.sc-price em { font-size: 22px; font-weight: 700; color: #A10000; font-style: normal; margin-left: 4px; }
.sc-stock { font-size: 13px; color: #888; margin: 0 0 10px; }
.sc-countdown { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 600;
  color: #e6a23c; margin-bottom: 12px; }
.sc-countdown.ending { color: #C10000; }
.sc-time { font-size: 13px; color: #888; margin: 0 0 8px; }
.sc-btn { width: 100%; height: 40px; border: none; border-radius: 20px; background: #A10000; color: #fff;
  font-size: 15px; font-weight: 500; cursor: pointer; transition: all .2s; }
.sc-btn:hover:not(:disabled) { background: #C10000; box-shadow: 0 4px 12px rgba(161,0,0,0.25); }
.sc-btn:disabled { background: #ddd; color: #999; cursor: not-allowed; }
.coming-tag { display: inline-block; padding: 3px 14px; background: #fdf6ec; color: #e6a23c;
  border-radius: 10px; font-size: 12px; font-weight: 500; }
.result-box { text-align: center; padding: 24px; }
.result-emoji { font-size: 48px; display: block; margin-bottom: 16px; }
.result-box p { font-size: 16px; color: #666; margin: 0; }
.result-box.success p { color: #67c23a; }
</style>
