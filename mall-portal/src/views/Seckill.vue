<template>
  <div class="seckill-page wrapper">
    <!-- Hero -->
    <section class="seckill-hero">
      <h1>限时秒杀</h1>
      <p>超值好物，手慢无</p>
    </section>

    <!-- Active Sessions -->
    <section class="seckill-section">
      <div class="section-head">
        <h3><span class="dot pulse"></span>进行中</h3>
        <span class="section-badge" v-if="!loading">{{ activeSessions.length }} 场</span>
      </div>

      <!-- Skeleton -->
      <div v-if="loading" class="seckill-grid">
        <div v-for="n in 4" :key="'sk'+n" class="seckill-card skeleton">
          <div class="sc-img shimmer"></div>
          <div class="sc-body">
            <div class="skeleton-line w-3/4"></div>
            <div class="skeleton-line w-1/2 mt-2"></div>
            <div class="skeleton-line w-full mt-2"></div>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else-if="activeSessions.length === 0" class="empty-state">
        <span class="empty-icon">⏰</span>
        <p class="empty-title">暂无进行中的秒杀</p>
        <p class="empty-hint" v-if="upcomingSessions.length > 0">下方有即将开始的活动，敬请期待</p>
        <p class="empty-hint" v-else>关注首页，精彩活动随时上线</p>
      </div>

      <!-- Cards -->
      <div v-else class="seckill-grid">
        <div v-for="(s, idx) in activeSessions" :key="s.sessionId"
          class="seckill-card" :style="{ animationDelay: idx * 0.05 + 's' }">
          <div class="sc-img">
            <img v-if="s.productImg" :src="getImageUrl(s.productImg)" :alt="s.productName || '商品图片'" />
            <span v-else class="sc-placeholder">{{ s.productName?.charAt(0) || '?' }}</span>
            <div class="sc-stock-bar">
              <div class="sc-stock-fill" :style="{ width: stockPercent(s) + '%' }"></div>
            </div>
          </div>
          <div class="sc-body">
            <p class="sc-name">{{ s.productName || '商品 #' + s.productId }}</p>
            <p class="sc-price">秒杀价 <em>&yen;{{ s.seckillPrice }}</em></p>
            <p class="sc-stock">剩余 <strong>{{ s.remainingStock }}</strong> 件</p>
            <div class="sc-countdown" :class="{ ending: s.countdown <= 60 }"
              :aria-label="'剩余 ' + formatCountdown(s.countdown)">
              <el-icon :size="14"><Clock /></el-icon>
              {{ s.countdown <= 0 ? '已结束' : formatCountdown(s.countdown) }}
              <span v-if="s.countdown > 0 && s.countdown <= 60" class="ending-tag">即将结束</span>
            </div>
            <button class="sc-btn" :class="{ pulsing: s.countdown > 0 && s.countdown <= 30 }"
              :disabled="s.countdown <= 0 || s.pending"
              @click="execute(s.sessionId)">
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
      <div v-if="upcomingSessions.length === 0 && !loading" class="empty-state">
        <span class="empty-icon">📅</span>
        <p class="empty-title">暂无即将开始的秒杀</p>
      </div>
      <div v-else class="seckill-grid">
        <div v-for="(s, idx) in upcomingSessions" :key="s.sessionId"
          class="seckill-card upcoming" :style="{ animationDelay: idx * 0.05 + 's' }">
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

    <!-- Result Dialog -->
    <el-dialog v-model="showResult" title="" width="360px" align-center :show-close="false">
      <div class="result-box" :class="{ success: resultSuccess }">
        <span class="result-icon">{{ resultSuccess ? '✅' : '😞' }}</span>
        <p class="result-title">{{ resultSuccess ? '秒杀成功！' : '未抢到' }}</p>
        <p class="result-msg">{{ resultMsg }}</p>
        <div class="result-actions">
          <el-button v-if="resultSuccess" type="danger" round @click="goPay">去支付</el-button>
          <el-button v-else round @click="showResult = false">继续逛逛</el-button>
        </div>
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

onUnmounted(() => { clearInterval(countdownTimer); clearInterval(pollTimer) })

function serverNow() { return new Date(Date.now() + serverOffset * 1000) }

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

function stockPercent(session) {
  if (!session.totalStock || session.totalStock === 0) return 0
  return Math.max(0, Math.min(100, (session.remainingStock / session.totalStock) * 100))
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
        showResultMsg(true, data.msg || '订单已生成，请尽快完成支付')
      } else if (data && (data.status === 2 || data.status === -1)) {
        clearInterval(pollTimer)
        activeSessions.value = activeSessions.value.map(s => s.sessionId === sessionId ? { ...s, pending: false } : s)
        showResultMsg(false, data.msg || '继续浏览其他秒杀商品，下次加油！')
      }
    }).catch(() => {})
  }, 2000)
}

function goPay() {
  showResult.value = false
  router.push('/orders')
}

function showResultMsg(success, msg) { resultSuccess.value = success; resultMsg.value = msg; showResult.value = true }
</script>

<style scoped>
.wrapper { width: 1160px; margin: 0 auto; padding: 24px 0; }

/* ====== Hero ====== */
.seckill-hero {
  background: linear-gradient(135deg, #A10000, #C10000); border-radius: 16px;
  padding: 48px; text-align: center; color: #fff; margin-bottom: 36px;
}
.seckill-hero h1 { font-size: 36px; font-weight: 700; margin: 0 0 8px; }
.seckill-hero p { font-size: 16px; opacity: 0.85; margin: 0; }

/* ====== Section ====== */
.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-head h3 { font-size: 20px; font-weight: 600; color: #222; margin: 0; display: flex; align-items: center; gap: 10px; }
.dot { width: 10px; height: 10px; border-radius: 50%; background: #A10000; display: inline-block; }
.dot.pulse { animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
.section-badge { font-size: 13px; color: #A10000; background: #fef5f5; padding: 4px 14px; border-radius: 12px; }

/* ====== Empty ====== */
.empty-state { text-align: center; padding: 64px 24px; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 16px; }
.empty-title { font-size: 16px; color: #666; margin: 0 0 8px; }
.empty-hint { font-size: 13px; color: #aaa; margin: 0; }

/* ====== Skeleton ====== */
.skeleton .sc-img { background: #f0f0f0; }
.shimmer { background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-line { height: 14px; border-radius: 4px; background: #f0f0f0; }
.skeleton-line.w-3\/4 { width: 75%; }
.skeleton-line.w-1\/2 { width: 50%; }
.skeleton-line.w-full { width: 100%; }
.skeleton-line.mt-2 { margin-top: 8px; }

/* ====== Grid ====== */
.seckill-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(265px, 1fr)); gap: 20px; margin-bottom: 40px; }

/* ====== Card ====== */
.seckill-card {
  background: #fff; border-radius: 14px; overflow: hidden; border: 1px solid #f0f0f0;
  transition: all .3s; opacity: 0; animation: cardIn .4s ease-out forwards;
}
@keyframes cardIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
.seckill-card:hover { box-shadow: 0 8px 30px rgba(0,0,0,0.08); transform: translateY(-3px); border-color: #ffcccc; }
.seckill-card.upcoming { opacity: 0.7; }
.seckill-card.upcoming:hover { opacity: 0.9; }

/* ====== Card Image ====== */
.sc-img {
  position: relative; aspect-ratio: 1; background: #fafafa; display: flex;
  align-items: center; justify-content: center; color: #ccc; font-size: 14px; overflow: hidden;
}
.sc-img img { width: 100%; height: 100%; object-fit: cover; transition: transform .6s; }
.seckill-card:hover .sc-img img { transform: scale(1.08); }
.sc-placeholder {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 56px; color: #ddd; background: #fafafa;
}

/* ====== Stock Bar ====== */
.sc-stock-bar { position: absolute; bottom: 0; left: 0; right: 0; height: 3px; background: rgba(0,0,0,0.08); }
.sc-stock-fill { height: 100%; background: #A10000; transition: width .5s; }

/* ====== Card Body ====== */
.sc-body { padding: 20px; }
.sc-name { font-size: 16px; color: #333; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sc-price { font-size: 14px; color: #999; margin: 0 0 6px; }
.sc-price em { font-size: 22px; font-weight: 700; color: #A10000; font-style: normal; margin-left: 4px; }
.sc-stock { font-size: 13px; color: #888; margin: 0 0 10px; }
.sc-stock strong { color: #A10000; font-weight: 600; }

/* ====== Countdown ====== */
.sc-countdown {
  display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 600;
  color: #e6a23c; margin-bottom: 12px;
}
.sc-countdown.ending { color: #C10000; }
.ending-tag { font-size: 11px; background: #C10000; color: #fff; padding: 1px 6px; border-radius: 4px; margin-left: 4px; }

/* ====== Time / Tag ====== */
.sc-time { font-size: 13px; color: #888; margin: 0 0 8px; }
.coming-tag { display: inline-block; padding: 3px 14px; background: #fdf6ec; color: #e6a23c; border-radius: 10px; font-size: 12px; font-weight: 500; }

/* ====== Button ====== */
.sc-btn {
  width: 100%; height: 44px; border: none; border-radius: 22px; background: #A10000; color: #fff;
  font-size: 15px; font-weight: 500; cursor: pointer; transition: all .2s;
}
.sc-btn:hover:not(:disabled) { background: #C10000; box-shadow: 0 4px 12px rgba(161,0,0,0.3); }
.sc-btn:disabled { background: #ddd; color: #999; cursor: not-allowed; }
.sc-btn.pulsing { animation: btnPulse 0.8s ease-in-out infinite; }
@keyframes btnPulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.03); box-shadow: 0 4px 16px rgba(161,0,0,0.4); } }

/* ====== Result Dialog ====== */
.result-box { text-align: center; padding: 24px 8px; }
.result-icon { font-size: 56px; display: block; margin-bottom: 20px; }
.result-title { font-size: 20px; font-weight: 600; color: #333; margin: 0 0 8px; }
.result-msg { font-size: 14px; color: #888; margin: 0 0 24px; line-height: 1.6; }
.result-box.success .result-title { color: #67c23a; }
.result-actions { display: flex; gap: 12px; justify-content: center; }
</style>
