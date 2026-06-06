<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="page-search" shadow="never">
      <el-form :inline="true" :model="query" size="small" class="page-search-form">
        <el-form-item label="商品名称" class="page-search-item">
          <el-input v-model="query.name" clearable placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="状态" class="page-search-item">
          <el-select v-model="query.status" clearable placeholder="请选择状态" class="w-180px">
            <el-option v-for="item in enumStore.getOptions('productStatus')" :key="item.code" :label="item.message" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" class="page-search-item">
          <el-tree-select
            v-model="query.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择分类"
            clearable
            check-strictly
            style="width:200px"
          />
        </el-form-item>
        <el-form-item class="page-search-action">
          <el-button type="primary" :icon="'Search'" @click="handleSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card class="page-table" shadow="never">
      <div class="page-table-actions">
        <el-button type="success" :icon="'Plus'" @click="handleAdd">新增商品</el-button>
      </div>
      <el-table :data="tableData" border v-loading="loading" height="calc(100vh - 450px)">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="name" label="商品名称" min-width="180" align="center" />
        <el-table-column label="图片" width="80" align="center">
          <template #default="{row}">
            <el-image v-if="imageUrls[row.id]" :src="imageUrls[row.id]" style="width:50px;height:50px" fit="cover" />
            <span v-else class="text-#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="{row}">￥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{row}">
            <el-switch
              :model-value="row.status === 1"
              @change="handleToggleStatus(row)"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" min-width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDel(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-pagination">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" v-model:current-page="query.current" v-model:page-size="query.size" :page-sizes="[10, 20, 50, 100]" @current-change="fetchData" @size-change="fetchData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dlgTitle" v-model="dlgVisible" width="600px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
        <el-form-item label="商品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" :precision="0" style="width:200px" /></el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTreeForEdit"
            :props="{ label: 'name', value: 'id', children: 'children', disabled: 'disabled' }"
            placeholder="请选择分类"
            check-strictly
            clearable
            style="width:240px"
          />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.detail" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="商品图片">
          <el-upload :http-request="handleUpload" :file-list="fileList" list-type="picture-card" :on-remove="handleRemoveImg" :before-upload="beforeUpload">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dlgVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as productApi from '@/api/product'
import * as categoryApi from '@/api/category'
import { getProductImage } from '@/api/product'
import { upload } from '@/api/oss'
import { useEnumStore } from '@/stores/modules/enum'

const enumStore = useEnumStore()
const imageUrls = reactive({})

async function loadProductImages(products) {
  for (const p of products) {
    if (p.images?.length) {
      try {
        const res = await getProductImage(p.images[0].url)
        const old = imageUrls[p.id]
        imageUrls[p.id] = URL.createObjectURL(res.data)
        if (old) URL.revokeObjectURL(old)
      } catch { /* ignore */ }
    }
  }
}

const query = reactive({ current: 1, size: 10, name: '', status: null, categoryId: null })
const tableData = ref([])
const total = ref(0)

watch(tableData, loadProductImages)
const loading = ref(false)
const dlgVisible = ref(false)
const dlgTitle = ref('')
const form = reactive({ id: null, name: '', price: 0, stock: 0, categoryId: null, detail: '', status: 1 })
const categoryOptions = ref([])
const categoryTree = ref([])
const categoryTreeForEdit = ref([])
const fileList = ref([])
const formRef = ref(null)
const validateInteger = (rule, value, cb) => {
  if (value !== null && value !== undefined && !Number.isInteger(value)) cb(new Error('库存必须为整数'))
  else cb()
}
const rules = reactive({
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }, { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }, { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' }, { validator: validateInteger, trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
})

async function handleUpload(options) {
  try { const res = await upload(options.file, 'product'); options.onSuccess(res) }
  catch (e) { options.onError(e) }
}
function handleRemoveImg(file) {
  const idx = fileList.value.findIndex(f => f.uid === file.uid)
  if (idx > -1) fileList.value.splice(idx, 1)
}
function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) ElMessage.error('只能上传图片文件')
  return isImage
}

async function fetchData() {
  loading.value = true
  try {
    const res = await productApi.list(query)
    tableData.value = res.data.list || res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
function markParentDisabled(nodes) {
  if (!nodes) return
  for (const node of nodes) {
    if (node.children && node.children.length > 0) {
      node.disabled = true
      markParentDisabled(node.children)
    }
  }
}
function deepClone(obj) {
  return JSON.parse(JSON.stringify(obj))
}
async function loadCategories() {
  const [allRes, treeRes] = await Promise.all([categoryApi.all(), categoryApi.tree()])
  categoryOptions.value = allRes.data || []
  const tree = treeRes.data || []
  categoryTree.value = tree
  const editTree = deepClone(tree)
  markParentDisabled(editTree)
  categoryTreeForEdit.value = editTree
}
function handleSearch() { query.current = 1; fetchData() }
function handleReset() { query.name = ''; query.status = null; query.categoryId = null; query.current = 1; fetchData() }

function handleAdd() {
  dlgTitle.value = '新增商品'
  Object.assign(form, { id: null, name: '', price: 0, stock: 0, categoryId: null, detail: '', status: 1 })
  fileList.value = []
  dlgVisible.value = true
}
function handleEdit(row) {
  dlgTitle.value = '编辑商品'
  Object.assign(form, { ...row })
  fileList.value = (row.images || []).map(img => ({ uid: img.id, name: img.name || 'image', url: img.url }))
  dlgVisible.value = true
}
async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const imageUrls = fileList.value.map(f => f.url || f.response?.data?.url).filter(Boolean)
  const data = { ...form, images: imageUrls.map(url => ({ url })) }
  if (form.id) await productApi.edit(data)
  else await productApi.add(data)
  ElMessage.success('保存成功'); dlgVisible.value = false; fetchData()
}
async function handleDel(id) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await productApi.del(id)
  ElMessage.success('删除成功'); fetchData()
}
async function handleToggleStatus(row) {
  const nextLabel = enumStore.getLabel('productStatus', row.status === 1 ? 0 : 1)
  const action = nextLabel
  await ElMessageBox.confirm(`确定${action}该商品？`, '提示', { type: 'warning' })
  await productApi.toggleStatus(row.id)
  ElMessage.success(`${action}成功`); fetchData()
}

onMounted(() => { fetchData(); loadCategories() })
</script>
