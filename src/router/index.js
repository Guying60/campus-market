import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Market from '../views/Market.vue'
import UserCenter from '../views/UserCenter.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. 修改默认重定向：
    // 原来是去 login，现在改为去 market（大厅）。
    // 放心，如果没登录，下面的守卫会把它拦截回 login 的。
    { path: '/', redirect: '/market' }, 
    
    { path: '/login', name: 'login', component: Login },
    { path: '/market', name: 'market', component: Market },
    { path: '/user', name: 'user', component: UserCenter }
  ]
})

// 2. 新增：全局路由守卫
router.beforeEach((to, from, next) => {
  // 从本地获取 token
const token = localStorage.getItem('access_token')
  if (token) {
    // A. 【已登录状态】
    if (to.path === '/login') {
      // 如果已登录还要去登录页，强制跳转到大厅
      next('/market')
    } else {
      // 去其他页面，放行
      next()
    }
  } else {
    // B. 【未登录状态】
    if (to.path === '/login') {
      // 去登录页，放行
      next()
    } else {
      // 去其他页面（如 /market, /user），强制重定向到登录页
      next('/login')
    }
  }
})

export default router