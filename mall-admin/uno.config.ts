import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons(),
  ],
  shortcuts: {
    'page-container': 'p-16px',
    'page-search': 'p-16px pb-0 bg-white rounded-4px shadow-sm mb-12px',
    'page-search-form': 'flex flex-wrap',
    'page-search-item': 'mr-24px mb-16px',
    'page-search-action': 'ml-auto !mr-0',
    'page-table-actions': 'mb-12px flex items-center justify-between',
    'btn-group': 'flex gap-8px',
    'page-table': 'p-8px px-16px pb-16px bg-white rounded-4px shadow-sm',
    'page-pagination': 'mt-16px flex justify-end',
    'card-stat': 'bg-white rounded-6px p-16px shadow-sm cursor-pointer transition-all duration-300 hover:-translate-y-2px',
    'layout-sidebar': 'flex-shrink-0 overflow-hidden transition-[width] shadow-lg z-10',
    'layout-right': 'flex-1 flex flex-col overflow-hidden min-w-0',
    'layout-header': 'h-50px bg-white border-b border-#e8e8e8 flex items-center justify-between px-16px flex-shrink-0 z-9 shadow-sm',
    'layout-tags': 'flex-shrink-0',
    'layout-main': 'flex-1 overflow-auto px-16px pt-16px pb-[50px] bg-#f0f2f5',
  },
  theme: {
    colors: {
      primary: '#409eff',
      'primary-light': '#66b1ff',
      'primary-dark': '#337ecc',
      success: '#67c23a',
      warning: '#e6a23c',
      danger: '#f56c6c',
    },
  },
})
