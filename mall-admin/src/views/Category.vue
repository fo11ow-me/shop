<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="page-search" shadow="never">
      <el-form :inline="true" size="small" class="page-search-form">
        <el-form-item label="分类名称">
          <el-input v-model="searchName" clearable placeholder="请输入分类名称" @input="handleSearch" />
        </el-form-item>
        <el-form-item class="page-search-action">
          <el-button type="primary" :icon="'Search'" @click="handleSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 表格区 -->
    <el-card class="page-table" shadow="never">
      <div class="page-table-actions">
        <el-button type="success" :icon="'Plus'" @click="handleAdd">新增分类</el-button>
      </div>
      <el-table :data="pageData" border row-key="id" default-expand-all :tree-props="{children: 'children'}" height="calc(100vh - 450px)">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column prop="name" label="分类名称" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDel(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-pagination">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" v-model:current-page="query.current" v-model:page-size="query.size" :page-sizes="[10, 20, 50]" @current-change="slicePage" @size-change="slicePage" />
      </div>
    </el-card>

    <el-dialog :title="dlgTitle" v-model="dlgVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" clearable placeholder="选择上级分类（留空为顶级）">
            <el-option label="顶级分类" :value="null" />
            <el-option v-for="item in rawTree" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlgVisible=false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as categoryApi from '@/api/category'

const rawTree = ref([])
const allFiltered = ref([])
const pageData = ref([])
const searchName = ref('')
const query = reactive({ current: 1, size: 10 })
const total = ref(0)
const dlgVisible = ref(false)
const dlgTitle = ref('')
const form = reactive({ id: null, parentId: null, name: '' })
const formRef = ref(null)
const rules = reactive({ name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] })

function filterByName(list, keyword) {
  if (!keyword) return list
  return list.reduce((acc, item) => {
    const children = item.children ? filterByName(item.children, keyword) : []
    if (item.name.includes(keyword) || children.length) { acc.push({ ...item, children }) }
    return acc
  }, [])
}

function applyFilter() {
  const src = JSON.parse(JSON.stringify(rawTree.value))
  allFiltered.value = filterByName(src, searchName.value)
  total.value = allFiltered.value.length
  slicePage()
}

function slicePage() {
  const start = (query.current - 1) * query.size
  pageData.value = allFiltered.value.slice(start, start + query.size)
}

function handleSearch() { query.current = 1; applyFilter() }
function resetSearch() { searchName.value = ''; query.current = 1; applyFilter() }

async function fetchData() {
  const res = await categoryApi.tree()
  rawTree.value = res.data || []
  searchName.value = ''
  query.current = 1
  applyFilter()
}

function handleAdd() {
  dlgTitle.value = '新增分类'
  Object.assign(form, { id: null, parentId: null, name: '' })
  dlgVisible.value = true
}

function handleEdit(row) {
  dlgTitle.value = '编辑分类'
  Object.assign(form, { id: row.id, parentId: row.parentId, name: row.name })
  dlgVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.id) await categoryApi.edit({ id: form.id, name: form.name })
  else await categoryApi.add({ name: form.name, parentId: form.parentId })
  ElMessage.success('保存成功'); dlgVisible.value = false; fetchData()
}

async function handleDel(id) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await categoryApi.del(id)
  ElMessage.success('删除成功'); fetchData()
}

onMounted(fetchData)
</script>
