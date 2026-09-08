<template>
  <div class="flex items-center h-34px bg-white border-b border-#e8e8e8 px-4px">
    <div v-if="showScroll" class="flex-shrink-0 w-24px h-24px flex items-center justify-center cursor-pointer text-#909399 rounded-4px transition-all duration-200 hover:bg-#f0f0f0 hover:text-#409eff" @click="scrollLeft">
      <el-icon><ArrowLeft /></el-icon>
    </div>

    <div class="flex-1 overflow-x-auto overflow-y-hidden whitespace-nowrap scrollbar-none" ref="scrollRef">
      <div class="inline-flex items-center gap-4px px-2px">
        <el-tag
          v-for="tag in tagStore.tagList"
          :key="tag.code"
          :effect="route.path === (tag.path || '/' + tag.code) ? 'dark' : 'plain'"
          :closable="tag.code !== 'home'"
          @click="router.push(tag.path || '/' + tag.code)"
          @close="tagStore.closeTag(tag.code)"
          class="tag-item"
          :class="{ 'is-active': route.path === (tag.path || '/' + tag.code) }"
          size="small"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </div>

    <div v-if="showScroll" class="flex-shrink-0 w-24px h-24px flex items-center justify-center cursor-pointer text-#909399 rounded-4px transition-all duration-200 hover:bg-#f0f0f0 hover:text-#409eff" @click="scrollRight">
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagStore } from '@/stores/modules/tag'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tagStore = useTagStore()
const scrollRef = ref(null)
const showScroll = ref(false)

function checkOverflow() {
  const el = scrollRef.value
  if (!el) return
  showScroll.value = el.scrollWidth > el.clientWidth
}

function scrollLeft() { scrollRef.value?.scrollBy({ left: -150, behavior: 'smooth' }) }
function scrollRight() { scrollRef.value?.scrollBy({ left: 150, behavior: 'smooth' }) }

let observer = null
onMounted(() => {
  checkOverflow()
  observer = new ResizeObserver(checkOverflow)
  if (scrollRef.value) observer.observe(scrollRef.value)
})
onUnmounted(() => observer?.disconnect())
</script>

<style lang="scss" scoped>
.tag-item {
  cursor: pointer;
  border: 1px solid var(--border-light) !important;
  background: var(--tag-item-bg) !important;
  color: var(--tag-item-text) !important;
  border-radius: 3px;
  font-size: 12px;
  white-space: nowrap;

  &.is-active,
  &.el-tag--dark {
    background: var(--tag-item-active) !important;
    color: #fff !important;
    border-color: var(--tag-item-active) !important;
  }

  &:hover { border-color: var(--color-primary-light) !important; }
}

.scrollbar-none {
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}
</style>
