<template>
  <div class="h-screen flex">
    <div
      class="layout-sidebar"
      :style="{ width: isCollapsed ? 'var(--sidebar-collapsed-width)' : 'var(--sidebar-width)' }"
    >
      <Aside />
    </div>
    <div class="layout-right">
      <header class="layout-header"><Header /></header>
      <div class="layout-tags"><Tag /></div>
      <main class="layout-main"><router-view /></main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import Aside from '@/components/Aside.vue'
import Header from '@/components/Header.vue'
import Tag from '@/components/Tag.vue'
import { useMenuStore } from '@/stores/modules/menu'
import { useEnumStore } from '@/stores/modules/enum'

const menuStore = useMenuStore()
const isCollapsed = computed(() => menuStore.isCollapsed)

onMounted(() => { useEnumStore().fetchEnums() })
</script>
