<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- Left: Brand -->
      <div class="auth-brand">
        <router-link to="/" class="brand-logo">
          <img :src="logoImg" alt="最家家居" />
        </router-link>
        <h1>欢迎回来</h1>
        <p>登录您的账号，发现更多家居好物</p>
        <div class="brand-features">
          <div class="feature"><el-icon :size="18"><Check /></el-icon> 品质保证</div>
          <div class="feature"><el-icon :size="18"><Check /></el-icon> 7天无理由退货</div>
          <div class="feature"><el-icon :size="18"><Check /></el-icon> 满599包邮</div>
        </div>
      </div>

      <!-- Right: Form -->
      <div class="auth-form">
        <h2>用户登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="code">
            <el-input v-model="form.code" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item prop="verificationCode">
            <div class="captcha-row">
              <el-input v-model="form.verificationCode" placeholder="验证码" />
              <img v-if="verificationCodeImg" :src="verificationCodeImg" @click="refreshCode" class="captcha-img" title="点击刷新" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="danger" :loading="loading" @click="handleLogin" class="submit-btn">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <router-link to="/register">没有账号？去注册</router-link>
        </div>
      </div>
    </div>
    <p class="auth-copy">&copy; 2024 最家家居</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Check } from '@element-plus/icons-vue'
import logoImg from '@/assets/img/logo.png'
import { useAuthStore } from '@/stores/auth'
import { login, getVerificationCode } from '../api'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const verificationCodeImg = ref('')
let verificationUuid = ''
const form = reactive({ code: 'admin', password: '123', verificationCode: '' })
const rules = {
  code: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  verificationCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCode = async () => {
  try {
    const { blob, uuid } = await getVerificationCode()
    verificationUuid = uuid || ''
    const url = URL.createObjectURL(blob)
    verificationCodeImg.value = url
    const img = new Image()
    img.onload = () => URL.revokeObjectURL(url)
    img.src = url
  } catch {}
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ ...form, uuid: verificationUuid })
    authStore.loginSuccess(res.data.token, res.data.user)
    router.push('/')
  } catch {} finally { loading.value = false }
}
onMounted(refreshCode)
</script>

<style scoped>
.auth-page { min-height: 100vh; background: linear-gradient(135deg, #fef5f5 0%, #f9f0f0 50%, #f5f0f0 100%);
  display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px 20px; }
.auth-container { display: flex; background: #fff; border-radius: 20px; overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.06); max-width: 880px; width: 100%; min-height: 520px; }

/* Brand Side */
.auth-brand { flex: 1; background: linear-gradient(135deg, #A10000, #C10000);
  padding: 60px 48px; display: flex; flex-direction: column; justify-content: center; color: #fff; }
.brand-logo img { height: 40px; filter: brightness(0) invert(1); }
.auth-brand h1 { font-size: 32px; font-weight: 700; margin: 40px 0 12px; }
.auth-brand p { font-size: 14px; opacity: 0.85; margin: 0 0 40px; line-height: 1.6; }
.brand-features { display: flex; flex-direction: column; gap: 12px; }
.feature { display: flex; align-items: center; gap: 10px; font-size: 14px; opacity: 0.9; }

/* Form Side */
.auth-form { flex: 1; padding: 60px 48px; display: flex; flex-direction: column; justify-content: center; }
.auth-form h2 { font-size: 24px; font-weight: 600; color: #222; margin: 0 0 32px; }
.captcha-row { display: flex; gap: 12px; align-items: center; }
.captcha-row .el-input { flex: 1; }
.captcha-img { height: 40px; border-radius: 6px; cursor: pointer; border: 1px solid #eee; }
.submit-btn { width: 100%; height: 44px; border-radius: 8px; font-size: 16px; background: #A10000; border-color: #A10000; }
.submit-btn:hover { background: #C10000; border-color: #C10000; }
.form-footer { text-align: center; margin-top: 20px; }
.form-footer a { font-size: 13px; color: #888; text-decoration: none; }
.form-footer a:hover { color: #A10000; }
.auth-copy { margin-top: 24px; font-size: 12px; color: #bbb; }
</style>
