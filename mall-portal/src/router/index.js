import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authRequired = ['Cart', 'Checkout', 'OrderList', 'UserInfo']

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'category/:id', name: 'ProductList', component: () => import('../views/ProductList.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/ProductDetail.vue') },
      { path: 'search', name: 'Search', component: () => import('../views/Search.vue') },
      { path: 'seckill', name: 'Seckill', component: () => import('../views/Seckill.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/Cart.vue') },
      { path: 'checkout', name: 'Checkout', component: () => import('../views/Checkout.vue') },
      { path: 'orders', name: 'OrderList', component: () => import('../views/OrderList.vue') },
      { path: 'userinfo', name: 'UserInfo', component: () => import('../views/UserInfo.vue') }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if ((to.name === 'Login' || to.name === 'Register') && authStore.isLogin) {
    next('/')
  } else if (authRequired.includes(to.name) && !authStore.isLogin) {
    next('/login')
  } else {
    next()
  }
})

export default router
