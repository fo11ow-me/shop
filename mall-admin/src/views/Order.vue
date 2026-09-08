<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="page-search" shadow="never">
      <el-form :inline="true" :model="query" size="small" class="page-search-form">
        <el-form-item label="订单号" class="page-search-item">
          <el-input v-model="query.orderSn" clearable placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="用户名" class="page-search-item">
          <el-input v-model="query.userName" clearable placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="状态" class="page-search-item">
          <el-select v-model="query.status" clearable multiple placeholder="请选择状态" collapse-tags class="w-200px">
            <el-option v-for="item in enumStore.getOptions('orderStatus')" :key="item.code" :label="item.message" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" class="page-search-item">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item class="page-search-action">
          <el-button type="primary" :icon="'Search'" @click="handleSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区 + 表格 -->
    <el-card class="page-table" shadow="never">
      <div class="page-table-actions">
        <div class="btn-group">
          <el-button type="primary" :icon="'Van'" :disabled="!canBatchDeliver" @click="handleBatchDeliver">批量发货</el-button>
          <el-button type="danger" :icon="'Delete'" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        border
        v-loading="loading"
        height="calc(100vh - 450px)"
        @selection-change="handleSelectionChange"
      >
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="orderSn" label="订单号" min-width="180" align="center" />
        <el-table-column prop="userName" label="用户" min-width="100" align="center" />
        <el-table-column prop="totalAmount" label="金额" width="120" align="center">
          <template #default="{row}">￥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="orderStatusTypes[row.status] || 'info'" size="small">{{ enumStore.getLabel('orderStatus', row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="100" align="center">
          <template #default="{row}">{{ enumStore.getLabel('payMethod', row.payMethod) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" size="small" v-if="row.status === 1" @click="handleDeliver(row.id)">发货</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 0" @click="handleCancel(row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="750px" :close-on-click-modal="false">
      <el-descriptions :column="2" border class="mb-20px">
        <el-descriptions-item label="订单号">{{ detailData.orderSn }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="金额">￥{{ detailData.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="orderStatusTypes[detailData.status] || 'info'" size="small">{{ enumStore.getLabel('orderStatus', detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ enumStore.getLabel('payMethod', detailData.payMethod) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <h4 class="mb-10px text-14px font-600 text-#303133">商品明细</h4>
      <el-table :data="detailData.items || []" border size="small">
        <el-table-column label="图片" width="80" align="center">
          <template #default="{row}">
            <img v-if="itemImages[row.productImg]" :src="itemImages[row.productImg]" style="width:50px;height:50px;object-fit:cover;border-radius:4px" />
            <span v-else style="color:#ccc;font-size:12px">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="160" align="center" />
        <el-table-column prop="productPrice" label="单价" width="100" align="center">
          <template #default="{row}">￥{{ row.productPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="数量" width="80" align="center" />
        <el-table-column label="小计" width="100" align="center">
          <template #default="{row}">￥{{ ((row.productPrice || 0) * (row.amount || 0))?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as orderApi from '@/api/order'
import { getProductImage } from '@/api/product'
import { useEnumStore } from '@/stores/modules/enum'

const enumStore = useEnumStore()

const orderStatusTypes = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }

const query = reactive({ current: 1, size: 10, orderSn: '', userName: '', status: [] })
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref([])
const detailVisible = ref(false)
const detailData = ref({})
const itemImages = reactive({})

const canBatchDeliver = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(r => r.status === 1))

function handleSelectionChange(rows) { selectedRows.value = rows }

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query }
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    if (params.status?.length) {
      params.status = params.status.join(',')
    } else {
      delete params.status
    }
    const res = await orderApi.list(params)
    tableData.value = res.data.list || res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  fetchData()
}

function handleReset() {
  query.orderSn = ''
  query.userName = ''
  query.status = []
  dateRange.value = []
  query.current = 1
  fetchData()
}

async function handleDetail(row) {
  try {
    loading.value = true
    const res = await orderApi.detail(row.id)
    detailData.value = res.data || {}
    detailVisible.value = true
    const items = detailData.value.items || []
    for (const item of items) {
      if (item.productImg && !itemImages[item.productImg]) {
        try {
          const blobRes = await getProductImage(item.productImg)
          itemImages[item.productImg] = URL.createObjectURL(blobRes.data)
        } catch { /* ignore */ }
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleDeliver(id) {
  try {
    await ElMessageBox.confirm('确认发货？', '提示', { type: 'warning' })
    await orderApi.deliver(id)
    ElMessage.success('发货成功')
    fetchData()
  } catch { /* 取消操作 */ }
}

async function handleCancel(id) {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    await orderApi.cancel(id)
    ElMessage.success('已取消')
    fetchData()
  } catch { /* 取消操作 */ }
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 条订单？`, '批量删除', { type: 'warning' })
    const ids = selectedRows.value.map(r => r.id).join(',')
    await orderApi.deleteBatch(ids)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch { /* 取消操作 */ }
}

async function handleBatchDeliver() {
  try {
    await ElMessageBox.confirm(`确认为选中的 ${selectedRows.value.length} 条订单批量发货？`, '批量发货', { type: 'warning' })
    const ids = selectedRows.value.map(r => r.id).join(',')
    await orderApi.batchDeliver(ids)
    ElMessage.success('批量发货成功')
    fetchData()
  } catch { /* 取消操作 */ }
}

onMounted(fetchData)
</script>
