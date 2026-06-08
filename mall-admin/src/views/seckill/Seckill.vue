<template>
  <div class="page-container">
    <el-card class="page-table" shadow="never">
      <div class="page-table-actions">
        <el-button type="success" :icon="'Plus'" @click="handleAdd">新建秒杀</el-button>
      </div>
      <el-table :data="tableData" border v-loading="loading">
        <template #empty><el-empty description="暂无秒杀场次" /></template>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="productId" label="商品ID" width="90" align="center" />
        <el-table-column prop="seckillPrice" label="秒杀价" width="100" align="center">
          <template #default="{row}">￥{{ row.seckillPrice }}</template>
        </el-table-column>
        <el-table-column prop="seckillStock" label="库存" width="80" align="center" />
        <el-table-column label="时间" min-width="280" align="center">
          <template #default="{row}">{{ row.startTime }} ~ {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag v-if="statusTag(row).type" :type="statusTag(row).type" size="small">{{ statusTag(row).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{row}">
            <el-button type="primary" size="small" :disabled="statusTag(row).ended" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" :disabled="statusTag(row).ended" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-table-pagination">
        <el-pagination v-model:current-page="query.current" :page-size="query.size" :total="total"
          layout="total, prev, pager, next" @current-change="fetchData" />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog :title="isEdit ? '编辑秒杀' : '新建秒杀'" v-model="dialogVisible" width="480px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品ID" required>
          <el-input-number v-model="form.productId" :min="1" />
        </el-form-item>
        <el-form-item label="秒杀价" required>
          <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.seckillStock" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const query = reactive({ current: 1, size: 10 })
const form = reactive({ productId: null, seckillPrice: null, seckillStock: null, startTime: '', endTime: '' })

const statusTag = (row) => {
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (now < start) return { text: '即将开始', type: 'warning', ended: false }
  if (now > end) return { text: '已结束', type: 'info', ended: true }
  return { text: '进行中', type: 'success', ended: false }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/seckill/list', { params: query })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, productId: null, seckillPrice: null, seckillStock: null, startTime: '', endTime: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.productId || !form.seckillPrice || !form.seckillStock || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写完整信息'); return
  }
  try {
    if (isEdit.value) {
      await request.put('/seckill', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/seckill', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该秒杀场次？', '提示', { type: 'warning' })
  try {
    await request.delete(`/seckill/${id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 16px; }
.page-table { margin-bottom: 16px; }
.page-table-actions { margin-bottom: 16px; }
.page-table-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
