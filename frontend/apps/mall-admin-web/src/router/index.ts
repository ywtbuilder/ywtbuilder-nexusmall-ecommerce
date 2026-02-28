import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/views/layout/LayoutView.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
        },
        // ===== PMS =====
        {
          path: 'pms/product',
          name: 'ProductList',
          component: () => import('@/views/pms/ProductListView.vue'),
        },
        {
          path: 'pms/brand',
          name: 'BrandList',
          component: () => import('@/views/pms/BrandListView.vue'),
        },
        {
          path: 'pms/productCategory',
          name: 'ProductCategory',
          component: () => import('@/views/pms/ProductCategoryView.vue'),
        },
        {
          path: 'pms/productAttrCategory',
          name: 'ProductAttrCategory',
          component: () => import('@/views/pms/ProductAttrCategoryView.vue'),
        },
        // ===== OMS =====
        {
          path: 'oms/order',
          name: 'OrderList',
          component: () => import('@/views/oms/OrderListView.vue'),
        },
        {
          path: 'oms/returnApply',
          name: 'ReturnApplyList',
          component: () => import('@/views/oms/ReturnApplyListView.vue'),
        },
        // ===== SMS =====
        {
          path: 'sms/coupon',
          name: 'CouponList',
          component: () => import('@/views/sms/CouponListView.vue'),
        },
        {
          path: 'sms/flash',
          name: 'FlashList',
          component: () => import('@/views/sms/FlashListView.vue'),
        },
        {
          path: 'sms/advertise',
          name: 'AdvertiseList',
          component: () => import('@/views/sms/AdvertiseListView.vue'),
        },
        // ===== UMS =====
        {
          path: 'ums/admin',
          name: 'AdminList',
          component: () => import('@/views/ums/AdminListView.vue'),
        },
        {
          path: 'ums/role',
          name: 'RoleList',
          component: () => import('@/views/ums/RoleListView.vue'),
        },
        {
          path: 'ums/menu',
          name: 'MenuList',
          component: () => import('@/views/ums/MenuListView.vue'),
        },
        {
          path: 'ums/resource',
          name: 'ResourceList',
          component: () => import('@/views/ums/ResourceListView.vue'),
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
