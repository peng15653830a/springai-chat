import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../shared/stores/auth'

const Login = () => import('../pages/Login.vue')
const Home = () => import('../pages/Home.vue')

const Chat = () => import('../features/chat/views/Chat.vue')
const Mcp = () => import('../features/mcp/views/Mcp.vue')

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true } },
  { path: '/chat', name: 'Chat', component: Chat, meta: { requiresAuth: true } },
  { path: '/mcp', name: 'Mcp', component: Mcp, meta: { requiresAuth: true } },
  // 预留占位：/chat/** 与 /mcp/** 将在迁移后注册
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    auth.loadFromStorage()
  }
  const authed = auth.isLoggedIn
  if (to.path === '/login' && authed) return next('/home')
  if (to.meta.requiresAuth && !authed) return next('/login')
  return next()
})

export default router
