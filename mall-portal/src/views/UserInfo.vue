<template>
  <div class="user-wrap">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <span class="current">个人信息</span>
    </div>

    <div class="user-layout">
      <Sidebar active="userinfo"/>

      <div class="main-area">
        <div class="profile-card">
          <div class="avatar-lg" @click="triggerUpload" :class="{ uploading }">
            <el-avatar v-if="avatarUrl" :src="avatarUrl" :size="80" class="avatar-img-el" />
            <el-icon v-else :size="40" color="#ccc"><User/></el-icon>
            <div class="avatar-overlay" v-if="!uploading">
              <el-icon :size="16"><Camera/></el-icon>
            </div>
            <div class="avatar-loading" v-if="uploading">
              <el-icon :size="24" class="spin"><Loading/></el-icon>
            </div>
          </div>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="handleFileChange"/>
          <div class="profile-info">
            <p class="profile-name">{{ userStore.user?.name || userStore.user?.code || '用户' }}</p>
            <a class="edit-link" @click="openEdit">编辑资料</a>
          </div>
        </div>

        <div class="info-rows">
          <div v-for="f in infoFields" :key="f.key" class="info-row">
            <span class="info-label">{{ f.label }}</span>
            <span class="info-value">{{ userStore.user?.[f.key] || '未填写' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="showEdit" title="编辑个人信息" width="440px" align-center>
      <el-form ref="formRef" :model="editForm" :rules="editRules" label-width="60px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="danger" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {User, Camera, Loading} from '@element-plus/icons-vue'
import Sidebar from '../components/Sidebar.vue'
import { useUserStore } from '@/stores/user'
import { getAvatar } from '@/api/user'

const userStore = useUserStore()

const avatarUrl = ref('')
const uploading = ref(false)
const showEdit = ref(false)
const formRef = ref(null)
const fileInput = ref(null)
const editForm = reactive({name: '', phone: '', address: ''})

const refreshAvatar = async () => {
  if (!userStore.user?.avatar) return
  try {
    const res = await getAvatar()
    const oldUrl = avatarUrl.value
    avatarUrl.value = URL.createObjectURL(res.data)
    if (oldUrl) URL.revokeObjectURL(oldUrl)
  } catch { avatarUrl.value = '' }
}

watch(() => userStore.user?.avatar, () => refreshAvatar())

const infoFields = [
  {label: '用户名', key: 'code'},
  {label: '姓名', key: 'name'},
  {label: '电话', key: 'phone'},
  {label: '地址', key: 'address'}
]

const editRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

;(async () => {
  await userStore.fetchUserInfo()
  if (userStore.user) {
    Object.assign(editForm, userStore.user)
    await refreshAvatar()
  }
})()

const triggerUpload = () => {
  if (uploading.value) return
  fileInput.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    await userStore.updateUserAvatar(file)
    await refreshAvatar()
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

const openEdit = () => {
  if (userStore.user) {
    Object.assign(editForm, {
      name: userStore.user.name || '',
      phone: userStore.user.phone || '',
      address: userStore.user.address || ''
    })
  }
  showEdit.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await userStore.updateInfo(editForm)
    showEdit.value = false
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.user-wrap {
  width: 1160px;
  margin: 0 auto;
  padding: 24px 0;
}

.breadcrumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 24px;
}

.breadcrumb a {
  color: #666;
  text-decoration: none;
}

.breadcrumb a:hover {
  color: #A10000;
}

.sep {
  margin: 0 8px;
  color: #ccc;
}

.current {
  color: #333;
}

.user-layout {
  display: flex;
  gap: 24px;
}

/* Main */
.main-area {
  flex: 1;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eee;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}

.avatar-lg {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  transition: opacity .2s;
}

.avatar-lg:hover .avatar-overlay {
  opacity: 1;
}

.avatar-lg.uploading {
  cursor: default;
  opacity: .7;
}

.avatar-img-el {
  width: 100%;
  height: 100%;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, .35);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity .2s;
}

.avatar-loading {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, .6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.spin {
  animation: spin 1s linear infinite;
  color: #A10000;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: #222;
  margin: 0 0 4px;
}

.profile-meta {
  font-size: 13px;
  color: #999;
  margin: 0 0 8px;
}

.edit-link {
  font-size: 13px;
  color: #A10000;
  text-decoration: none;
  cursor: pointer;
}

.edit-link:hover {
  text-decoration: underline;
}

.info-rows {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eee;
  padding: 8px 32px;
}

.info-row {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 72px;
  font-size: 14px;
  color: #999;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #333;
}
</style>
