<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="page-search" shadow="never">
      <el-form :inline="true" :model="query" size="small" class="page-search-form">
        <el-form-item label="姓名" class="page-search-item">
          <el-input v-model="query.name" clearable placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="性别" class="page-search-item">
          <el-select v-model="query.gender" clearable placeholder="请选择性别" class="w-180px">
            <el-option v-for="item in enumStore.getOptions('gender')" :key="item.code" :label="item.message" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" class="page-search-item">
          <el-select v-model="query.status" clearable placeholder="请选择状态" class="w-180px">
            <el-option v-for="item in enumStore.getOptions('userStatus')" :key="item.code" :label="item.message" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间" class="page-search-item">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
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
          <el-button type="success" :icon="'Plus'" @click="handleAdd">新增用户</el-button>
          <el-button type="primary" :icon="'CircleCheck'" :disabled="!selIds.length" @click="handleBatchEnable(1)">批量启用</el-button>
          <el-button :icon="'CircleClose'" :disabled="!selIds.length" @click="handleBatchEnable(0)">批量禁用</el-button>
          <el-button type="danger" :icon="'Delete'" :disabled="!selIds.length" @click="handleBatchDel">批量删除</el-button>
        </div>
        <el-button :icon="'Download'" @click="handleExport">导出</el-button>
      </div>

      <el-table :data="tableData" border v-loading="loading" height="calc(100vh - 450px)" @selection-change="v => selIds = v.map(r => r.id)">
        <template #empty><el-empty description="暂无数据" /></template>
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="头像" width="70" align="center">
          <template #default="{row}"><el-avatar :size="36" :src="avatarUrls[row.id] || ''" /></template>
        </el-table-column>
        <el-table-column prop="code" label="用户名" min-width="120" align="center" />
        <el-table-column prop="name" label="姓名" min-width="100" align="center" />
        <el-table-column label="性别" width="70" align="center">
          <template #default="{row}">
            <el-tag :type="genderTagType[row.gender] || 'info'" size="small">{{ enumStore.getLabel('gender', row.gender) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{row}">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleToggleRole(row)">{{ row.role === 1 ? '设为用户' : '设为管理' }}</el-button>
            <el-button link type="success" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="handleDel(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" v-model:current-page="query.current" v-model:page-size="query.size" :page-sizes="[10, 20, 50, 100]" @current-change="fetchData" @size-change="fetchData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dlgTitle" v-model="dlgVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="code"><el-input v-model="form.code" :disabled="!!form.id" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别"><el-select v-model="form.gender" clearable><el-option v-for="item in enumStore.getOptions('gender')" :key="item.code" :label="item.message" :value="item.code" /></el-select></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日"><el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12" v-if="!form.id">
            <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态"><el-select v-model="form.status"><el-option v-for="item in enumStore.getOptions('userStatus')" :key="item.code" :label="item.message" :value="item.code" /></el-select></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="头像">
              <el-upload :http-request="handleUpload" :file-list="avatarList" list-type="picture-card" :on-remove="handleRemoveAvatar" :before-upload="beforeAvatarUpload" :limit="1">
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dlgVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" v-model="pwdVisible" width="400px" :close-on-click-modal="false">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px" size="small">
        <el-form-item label="新密码" prop="newPwd"><el-input v-model="pwdForm.newPwd" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPwd"><el-input v-model="pwdForm.confirmPwd" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pwdVisible = false">取消</el-button><el-button type="primary" @click="savePwd">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as userApi from '@/api/user'
import { useRouter } from 'vue-router'
import { useEnumStore } from '@/stores/modules/enum'

const router = useRouter()
const enumStore = useEnumStore()
const genderTagType = { 0: 'primary', 1: 'danger' }
const avatarUrls = reactive({})

async function loadAvatars(users) {
  for (const u of users) {
    if (u?.avatar) {
      try {
        const res = await userApi.getUserAvatar(u.id)
        const old = avatarUrls[u.id]
        avatarUrls[u.id] = URL.createObjectURL(res.data)
        if (old) URL.revokeObjectURL(old)
      } catch { /* ignore */ }
    }
  }
}

const query = reactive({ current: 1, size: 10, code: '', name: '', phone: '', gender: null, status: null })
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)

watch(tableData, loadAvatars)
const loading = ref(false)
const selIds = ref([])

// 新增/编辑
const dlgVisible = ref(false)
const dlgTitle = ref('')
const form = reactive({ id: null, code: '', name: '', gender: null, phone: '', email: '', birthday: '', address: '', remark: '', status: 1, password: '123' })
const formRef = ref(null)
const avatarList = ref([])
const validateNoSpecialChars = (rule, value, cb) => {
  if (/[^a-zA-Z0-9_一-龥]/.test(value)) cb(new Error('用户名不能包含特殊字符'))
  else cb()
}
const validateNotBlank = (rule, value, cb) => {
  if (!(value ?? '').trim()) cb(new Error('姓名不能为空'))
  else cb()
}
const rules = {
  code: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
    { validator: validateNoSpecialChars, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { validator: validateNotBlank, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '密码不少于3位', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 角色切换
async function handleToggleRole(row) {
  const newRole = row.role === 1 ? 0 : 1
  const label = newRole === 1 ? '管理员' : '用户'
  try {
    await ElMessageBox.confirm(`确定将该用户角色设为"${label}"？`, '提示', { type: 'warning' })
    await userApi.edit({ id: row.id, role: newRole })
    ElMessage.success('角色修改成功'); fetchData()
  } catch { /* 取消 */ }
}

// 重置密码
const pwdVisible = ref(false)
const pwdForm = reactive({ newPwd: '', confirmPwd: '' })
const pwdFormRef = ref(null)
const validateConfirmPwd = (rule, value, cb) => {
  if (value !== pwdForm.newPwd) cb(new Error('两次密码不一致'))
  else cb()
}
const pwdRules = {
  newPwd: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 3, message: '密码不少于3位', trigger: 'blur' }],
  confirmPwd: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirmPwd, trigger: 'blur' }]
}

// 头像
function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) ElMessage.error('只能上传图片文件')
  return isImage
}
async function handleUpload(options) {
  try { const res = await userApi.uploadAvatar(options.file); options.onSuccess(res) }
  catch (e) { options.onError(e) }
}
function handleRemoveAvatar() { avatarList.value = [] }

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.status === null) delete params.status
    if (params.gender === null) delete params.gender
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await userApi.list(params)
    tableData.value = res.data.list || res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { query.current = 1; fetchData() }
function handleReset() {
  Object.assign(query, { current: 1, code: '', name: '', phone: '', gender: null, status: null })
  dateRange.value = []
  fetchData()
}

// 新增/编辑
function handleAdd() {
  dlgTitle.value = '新增用户'
  Object.assign(form, { id: null, code: '', name: '', gender: null, phone: '', email: '', birthday: '', address: '', remark: '', status: 1, password: '123' })
  avatarList.value = []
  dlgVisible.value = true
}
function handleEdit(row) {
  dlgTitle.value = '编辑用户'
  Object.assign(form, { ...row, password: '' })
  avatarList.value = row.avatar ? [{ uid: row.id, name: 'avatar', url: row.avatar }] : []
  dlgVisible.value = true
}
async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const avatarUrl = avatarList.value[0]?.url || avatarList.value[0]?.response?.data?.url || form.avatar || ''
  const data = { ...form, avatar: avatarUrl }
  if (form.id) await userApi.edit(data)
  else await userApi.add(data)
  ElMessage.success('保存成功'); dlgVisible.value = false; fetchData()
}

// 状态切换
async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}该用户？`, '提示', { type: 'warning' })
    await userApi.edit({ id: row.id, status: newStatus })
    ElMessage.success(`${action}成功`); fetchData()
  } catch { /* 取消 */ }
}

// 批量启用/禁用
async function handleBatchEnable(status) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定批量${action}选中用户？`, '提示', { type: 'warning' })
    await Promise.all(selIds.value.map(id => userApi.edit({ id, status })))
    ElMessage.success(`批量${action}成功`); fetchData()
  } catch { /* 取消 */ }
}

// 删除
async function handleDel(id) {
  try {
    await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
    await userApi.del(id); ElMessage.success('删除成功'); fetchData()
  } catch { /* 取消 */ }
}
async function handleBatchDel() {
  try {
    await ElMessageBox.confirm('确定批量删除？', '提示', { type: 'warning' })
    await userApi.deleteBatch(selIds.value.join(','))
    ElMessage.success('删除成功'); fetchData()
  } catch { /* 取消 */ }
}

// 导出
function handleExport() {
  const filename = '用户数据.xlsx'
  userApi.exp(filename).then(res => {
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a'); a.href = url; a.download = filename; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }).catch(() => ElMessage.error('导出失败'))
}

// 重置密码
function handleResetPwd(row) { form.id = row.id; pwdVisible.value = true }
async function savePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  await userApi.reset({ id: form.id, password: pwdForm.newPwd })
  ElMessage.success('密码重置成功')
  pwdVisible.value = false
  pwdForm.newPwd = ''; pwdForm.confirmPwd = ''
}

onMounted(fetchData)
</script>
