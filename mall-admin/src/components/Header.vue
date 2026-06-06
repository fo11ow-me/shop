<template>
  <div class="flex items-center justify-between h-full w-full">
    <div class="flex items-center gap-12px min-w-0">
      <el-button
        @click="menuStore.collapseMenu()"
        :icon="isCollapsed ? 'Expand' : 'Fold'"
        text
        class="!text-18px !text-#333"
      />
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">
          <el-icon><HomeFilled /></el-icon>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-for="crumb in breadcrumbs" :key="crumb.path">
          {{ crumb.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="flex items-center gap-8px">
      <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
        <el-button
          class="!text-18px !text-#909399 hover:!text-#409eff"
          text
          :icon="isFullscreen ? 'FullScreen' : 'FullScreen'"
          @click="toggleFullscreen"
        />
      </el-tooltip>

      <el-dropdown @command="handleCmd" trigger="click">
        <span class="flex items-center gap-6px cursor-pointer px-8px h-40px rounded-4px transition-bg duration-200 hover:bg-#f5f5f5">
          <el-avatar :size="28" :src="avatarUrl" />
          <span class="text-#303133 text-14px">{{ user?.name || '管理员' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="editPwd">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-dialog v-model="pwdVisible" title="修改密码" width="400px">
        <el-form :model="pwdForm" label-width="80px">
          <el-form-item label="原密码"><el-input v-model="pwdForm.oldPwd" type="password" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwdForm.newPwd" type="password" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="pwdVisible = false">取消</el-button><el-button type="primary" @click="changePwd">确认</el-button></template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { HomeFilled, ArrowDown, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import { useMenuStore } from '@/stores/modules/menu'
import { useTagStore } from '@/stores/modules/tag'
import { reset, getUserAvatar } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()
const tagStore = useTagStore()
const user = computed(() => userStore.user)
const avatarUrl = ref('')

watch(() => userStore.user, async (u) => {
  if (u?.id && u?.avatar) {
    try {
      const res = await getUserAvatar(u.id)
      const old = avatarUrl.value
      avatarUrl.value = URL.createObjectURL(res.data)
      if (old) URL.revokeObjectURL(old)
    } catch { avatarUrl.value = '' }
  } else {
    avatarUrl.value = ''
  }
}, { immediate: true })
const isCollapsed = computed(() => menuStore.isCollapsed)
const isFullscreen = ref(false)
const pwdVisible = ref(false)
const pwdForm = ref({ oldPwd: '', newPwd: '' })

const breadcrumbs = computed(() => {
  return route.matched
    .filter(r => r.meta?.title)
    .map(r => ({ title: r.meta.title, path: r.path }))
})

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

function onFullscreenChange() { isFullscreen.value = !!document.fullscreenElement }

onMounted(() => document.addEventListener('fullscreenchange', onFullscreenChange))
onUnmounted(() => document.removeEventListener('fullscreenchange', onFullscreenChange))

async function changePwd() {
  try {
    await reset({ id: user.value.id, password: pwdForm.value.newPwd })
    ElMessage.success('密码修改成功')
    pwdVisible.value = false
  } catch { ElMessage.error('修改失败') }
}

function handleCmd(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
  } else if (cmd === 'editPwd') {
    pwdVisible.value = true
  }
}
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  :deep(.el-breadcrumb__inner) {
    color: var(--text-secondary);
    font-size: 13px;
  }
  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: var(--text-primary);
    font-weight: 500;
  }
}
</style>
