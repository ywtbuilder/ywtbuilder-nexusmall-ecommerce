import { createRouter, createWebHistory } from 'vue-router'
import { markRouteEnd, markRouteStart, rotateTraceId } from '@/utils/rum'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue') },
    { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
    { path: '/category', name: 'Category', component: () => import('@/views/CategoryView.vue') },
    { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetailView.vue') },
    // JD 离线镜像详情页（支持 /jd-item 或 /jd-item/:sku）
    { path: '/jd-item', name: 'JdItem', component: () => import('@/views/JdItemView.vue') },
    { path: '/jd-item/:sku', name: 'JdItemSku', component: () => import('@/views/JdItemView.vue') },
    { path: '/cart', name: 'Cart', component: () => import('@/views/CartView.vue'), meta: { auth: true } },
    { path: '/order/confirm', name: 'OrderConfirm', component: () => import('@/views/OrderConfirmView.vue'), meta: { auth: true } },
    { path: '/order/list', name: 'OrderList', component: () => import('@/views/OrderListView.vue'), meta: { auth: true } },
    { path: '/order/detail/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetailView.vue'), meta: { auth: true } },
    { path: '/mine', name: 'Mine', component: () => import('@/views/MineView.vue'), meta: { auth: true } },
    { path: '/search', name: 'Search', component: () => import('@/views/SearchView.vue') },
    { path: '/upcoming', name: 'Upcoming', component: () => import('@/views/UpcomingView.vue') },
    { path: '/register', name: 'Register', component: () => import('@/views/RegisterView.vue') },
    { path: '/address', name: 'Address', component: () => import('@/views/AddressView.vue'), meta: { auth: true } },
    { path: '/collection', name: 'Collection', component: () => import('@/views/CollectionView.vue'), meta: { auth: true } },
    { path: '/readHistory', name: 'ReadHistory', component: () => import('@/views/ReadHistoryView.vue'), meta: { auth: true } },
    { path: '/attention', name: 'Attention', component: () => import('@/views/AttentionView.vue'), meta: { auth: true } },
    { path: '/coupon', name: 'Coupon', component: () => import('@/views/CouponView.vue'), meta: { auth: true } },
  ],
})

router.beforeEach((to, _from, next) => {
  rotateTraceId()
  markRouteStart(to.fullPath)

  if (to.meta.auth && !localStorage.getItem('token')) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach((to) => {
  markRouteEnd(to.fullPath)
})

export default router
