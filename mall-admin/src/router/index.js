import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Main.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'home', component: () => import('@/views/Home.vue') },
      { path: 'user', name: 'user', component: () => import('@/views/User.vue') },
      { path: 'category', name: 'category', component: () => import('@/views/Category.vue') },
      { path: 'product', name: 'product', component: () => import('@/views/Product.vue') },
      { path: 'order', name: 'order', component: () => import('@/views/Order.vue') },
      { path: 'seckill', name: 'seckill', component: () => import('@/views/seckill/Seckill.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.name !== 'login' && !userStore.user) {
    next({ name: 'login' })
    return
  }
  next()
})

export default router
