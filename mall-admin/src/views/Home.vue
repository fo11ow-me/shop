<template>
  <div v-loading="loading" class="max-w-1400px mx-auto px-8px pb-80px">
    <!-- 指标卡片 -->
    <el-row :gutter="16" class="mb-16px">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <div class="card-stat">
          <div class="flex items-start justify-between">
            <div>
              <p class="text-13px text-#909399 mb-6px">{{ card.label }}</p>
              <p class="text-28px font-700 text-#303133 leading-none">{{ card.value }}</p>
            </div>
            <div class="w-40px h-40px rounded-8px flex items-center justify-center text-white flex-shrink-0" :style="{ background: card.color }">
              <el-icon size="20"><component :is="card.icon" /></el-icon>
            </div>
          </div>
          <div class="flex items-center gap-4px mt-14px text-12px" :class="card.trend > 0 ? 'text-#67c23a' : card.trend < 0 ? 'text-#f56c6c' : 'text-#909399'">
            <el-icon v-if="card.trend !== 0"><component :is="card.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
            <span>{{ card.trend !== 0 ? '较昨日 ' + (card.trend > 0 ? '+' : '') + card.trend + '%' : card.desc }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :span="24">
        <div class="bg-white rounded-6px p-16px shadow-sm mb-16px">
          <div class="flex items-center justify-between mb-16px">
            <h3 class="text-16px font-600 text-#303133 m-0">销售趋势</h3>
            <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
              <el-radio-button :value="7">近7天</el-radio-button>
              <el-radio-button :value="30">近30天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChartRef" class="w-full h-190px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <div class="bg-white rounded-6px p-16px shadow-sm mb-16px">
          <h3 class="text-16px font-600 text-#303133 mb-16px m-0">分类销量 TOP5</h3>
          <div ref="categoryChartRef" class="w-full h-190px"></div>
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="bg-white rounded-6px p-16px shadow-sm mb-16px">
          <h3 class="text-16px font-600 text-#303133 mb-16px m-0">订单状态概览</h3>
          <div ref="statusChartRef" class="w-full h-190px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { queryCount, queryTrend, queryCategorySales } from '@/api/home'
import { useEnumStore } from '@/stores/modules/enum'
import { ShoppingCartFull, Money, UserFilled, GoodsFilled, Top, Bottom } from '@element-plus/icons-vue'

const enumStore = useEnumStore()

const loading = ref(false)
const trendDays = ref(7)
const trendChartRef = ref(null)
const categoryChartRef = ref(null)
const statusChartRef = ref(null)

const countData = reactive({
  userCount: 0, productCount: 0,
  todayOrderCount: 0, todaySales: 0,
  yesterdayOrderCount: 0, yesterdaySales: 0
})

const calcTrend = (today, yesterday) => {
  if (!yesterday) return 0
  return Math.round(((today - yesterday) / yesterday) * 100)
}

const statCards = computed(() => [
  {
    label: '今日订单数', value: countData.todayOrderCount, icon: ShoppingCartFull,
    color: 'linear-gradient(135deg, #409eff, #337ecc)',
    desc: '较昨日', trend: calcTrend(countData.todayOrderCount, countData.yesterdayOrderCount)
  },
  {
    label: '今日销售额', value: '￥' + (countData.todaySales || 0).toFixed(2), icon: Money,
    color: 'linear-gradient(135deg, #67c23a, #529b2e)',
    desc: '较昨日', trend: calcTrend(countData.todaySales, countData.yesterdaySales)
  },
  {
    label: '总用户数', value: countData.userCount, icon: UserFilled,
    color: 'linear-gradient(135deg, #e6a23c, #d48806)',
    desc: '累计注册', trend: 0
  },
  {
    label: '在售商品', value: countData.productCount, icon: GoodsFilled,
    color: 'linear-gradient(135deg, #a855f7, #7c3aed)',
    desc: '当前在售', trend: 0
  }
])

async function loadCount() {
  const res = await queryCount()
  if (res.data) {
    Object.assign(countData, res.data)
  }
}

async function loadTrend() {
  const res = await queryTrend(trendDays.value)
  const data = res.data || []
  const chart = echarts.getInstanceByDom(trendChartRef.value) || echarts.init(trendChartRef.value, null, { renderer: 'svg' })
  chart.setOption({
    textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
    color: ['#409eff', '#67c23a'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      boxShadow: '0 2px 12px rgba(0,0,0,0.08)'
    },
    legend: {
      data: ['订单数', '销售额'],
      right: 0,
      top: 0,
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 20,
      textStyle: { color: '#606266', fontSize: 12 }
    },
    grid: { left: 8, right: 16, top: 44, bottom: 8 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisTick: { show: false },
      axisLabel: { color: '#909399', fontSize: 11, rotate: trendDays.value === 30 ? 30 : 0 },
    },
    yAxis: [
      {
        type: 'value',
        name: '',
        minInterval: 1,
        splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
        axisLabel: { color: '#909399', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false }
      },
      {
        type: 'value',
        name: '',
        splitLine: { show: false },
        axisLabel: { show: false },
        axisLine: { show: false },
        axisTick: { show: false }
      }
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: data.map(d => d.orderCount),
        barWidth: 24,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#a0cfff' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#337ecc' },
              { offset: 1, color: '#79bbff' }
            ])
          }
        }
      },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(d => d.sales),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#67c23a' },
        itemStyle: { color: '#67c23a', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.15)' },
            { offset: 1, color: 'rgba(103,194,58,0)' }
          ])
        }
      }
    ]
  }, true)
}

async function loadCategorySales() {
  const res = await queryCategorySales()
  const data = res.data || []
  const chart = echarts.getInstanceByDom(categoryChartRef.value) || echarts.init(categoryChartRef.value, null, { renderer: 'svg' })
  chart.setOption({
    textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      boxShadow: '0 2px 12px rgba(0,0,0,0.08)'
    },
    grid: { left: 8, right: 40, top: 4, bottom: 4 },
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#909399', fontSize: 11 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: data.map(d => d.categoryName),
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#606266', fontSize: 12 },
      axisMargin: 8
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.count),
      barWidth: 16,
      label: {
        show: true,
        position: 'right',
        color: '#606266',
        fontSize: 11,
        formatter: '{c} 件'
      },
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409eff' },
          { offset: 0.7, color: '#66b1ff' },
          { offset: 1, color: '#a0cfff' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#337ecc' },
            { offset: 0.7, color: '#409eff' },
            { offset: 1, color: '#79bbff' }
          ])
        }
      }
    }]
  }, true)
}

function loadStatusChart() {
  const chart = echarts.getInstanceByDom(statusChartRef.value) || echarts.init(statusChartRef.value, null, { renderer: 'svg' })
  const colorMap = { 0: '#909399', 1: '#e6a23c', 2: '#409eff', 3: '#67c23a', 4: '#f56c6c' }
  const data = enumStore.getOptions('orderStatus').map(item => ({
    value: 0, name: item.message, color: colorMap[item.code] || '#909399'
  }))
  const total = data.reduce((s, d) => s + d.value, 0)
  chart.setOption({
    textStyle: { fontFamily: 'Microsoft YaHei, PingFang SC, sans-serif' },
    color: data.map(d => d.color),
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
      formatter: '{b}: {c} 单 ({d}%)'
    },
    legend: {
      bottom: 0,
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 16,
      textStyle: { color: '#606266', fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['55%', '78%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 3,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 13, fontWeight: 'bold' },
        scaleSize: 8
      },
      data: data
    }],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '34%',
        style: {
          text: total || '--',
          textAlign: 'center',
          fill: '#303133',
          fontSize: 22,
          fontWeight: 'bold'
        }
      },
      {
        type: 'text',
        left: 'center',
        top: '45%',
        style: {
          text: '订单总数',
          textAlign: 'center',
          fill: '#909399',
          fontSize: 12
        }
      }
    ]
  }, true)
}

onMounted(async () => {
  loading.value = true
  try {
    await loadCount()
    await loadTrend()
    await loadCategorySales()
    await nextTick()
    loadStatusChart()
  } finally {
    loading.value = false
  }
})

// 响应窗口 resize
window.addEventListener('resize', () => {
  [trendChartRef, categoryChartRef, statusChartRef].forEach(ref => {
    if (ref.value) echarts.getInstanceByDom(ref.value)?.resize()
  })
})
</script>
