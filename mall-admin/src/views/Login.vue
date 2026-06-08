<template>
  <div class="login-container">
    <div class="login-card">
      <h2>商城后台管理</h2>
      <el-form ref="formRef" :model="form" :rules="rules" size="default">
        <el-form-item prop="code"><el-input v-model="form.code" placeholder="用户名" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="form.password" type="password" placeholder="密码" show-password @keyup.enter="login" /></el-form-item>
        <el-form-item prop="verificationCode">
          <el-input v-model="form.verificationCode" placeholder="验证码" style="width:60%" />
          <img :src="codeUrl" @click="refreshCode" style="width:90px;height:38px;cursor:pointer;margin-left:10px" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%" @click="login">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import { useTokenStore } from '@/stores/modules/token'
import { setAuth } from '@/utils/auth'
import { login as apiLogin, getVerificationCode } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const tokenStore = useTokenStore()
const loading = ref(false)
const codeUrl = ref('')
const formRef = ref(null)
let verificationUuid = ''
const form = reactive({ code: 'admin', password: '123', verificationCode: '' })
const rules = { code: [{ required: true, message: '请输入用户名' }], password: [{ required: true, message: '请输入密码' }], verificationCode: [{ required: true, message: '请输入验证码' }] }

function refreshCode() {
  getVerificationCode().then(result => {
    const url = window.URL.createObjectURL(new Blob([result.blob]))
    codeUrl.value = url
    verificationUuid = result.uuid || ''
    const img = new Image()
    img.onload = () => URL.revokeObjectURL(url)
    img.src = url
  })
}

async function login() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await apiLogin({ ...form, uuid: verificationUuid })
      if (res.code === 200) {
        const user = res.data?.user || res.data
        const token = res.data?.token || res.token
        setAuth({ token, user })
        tokenStore.setToken(token)
        userStore.setUser(user)
        ElMessage.success('登录成功')
        router.push('/')
      } else { ElMessage.error(res.message || '登录失败') }
    } catch { ElMessage.error('登录失败') }
    finally { loading.value = false }
  })
}

onMounted(refreshCode)
</script>

<style scoped>
.login-container { height: 100vh; display: flex; align-items: center; justify-content: center; background: #f0f2f5; }
.login-card { width: 400px; padding: 30px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.login-card h2 { text-align: center; margin-bottom: 24px; color: #303133; }
</style>
