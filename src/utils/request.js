import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  // 开发环境走本地 /api 触发代理，生产环境直连线上
  baseURL: import.meta.env.PROD ? 'https://api.guying.xyz' : '/api',
  timeout: 5000
})

// --- 变量定义 ---
let isRefreshing = false // 是否正在刷新 Token
let requests = []        // 重试队列

// --- 辅助函数：强制登出 ---
function handleLogout() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('user_info')
  
  router.replace('/login')
  
  // 防止重复弹窗
  if (!document.querySelector('.el-message')) {
    ElMessage.warning('登录已过期，请重新登录')
  }
}

// --- 核心逻辑：刷新 Token ---
async function handleRefreshToken(config) {
  if (isRefreshing) {
    // 正在刷新中，挂起当前请求
    return new Promise((resolve) => {
      requests.push((token) => {
        config.headers['Authorization'] = token
        resolve(service(config))
      })
    })
  }

  isRefreshing = true
  
  try {
    const refreshToken = localStorage.getItem('refresh_token')
    if (!refreshToken) {
      throw new Error('本地无刷新令牌')
    }

    // 发送刷新请求
    // 注意：这里保留了下划线格式 refresh_token，适配你后端的“下划线转驼峰”配置
    const { data } = await axios.post('/api/user/refresh-token', {
      refresh_token: refreshToken 
    })

    // 兼容取值
    const resData = data.data || data
    const newAccessToken = resData.access_token || resData.accessToken || resData.token

    if (newAccessToken) {
      // 1. 存新 Token
      localStorage.setItem('access_token', newAccessToken)
      
      // 2. 更新默认头
      service.defaults.headers.common['Authorization'] = newAccessToken
      
      // 3. 执行队列（重试之前挂起的请求）
      requests.forEach(cb => cb(newAccessToken))
      requests = []
      
      // 4. 重试当前请求
      config.headers['Authorization'] = newAccessToken
      return service(config)
    } else {
      throw new Error('刷新接口未返回有效 Token')
    }

  } catch (e) {
    console.error('Token 刷新失败', e)
    // 刷新也失败了，必须登出
    handleLogout()
    return Promise.reject(e)
  } finally {
    isRefreshing = false
  }
}

// --- 请求拦截器 ---
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// --- 响应拦截器 ---
service.interceptors.response.use(
  async response => {
    const res = response.data
    const config = response.config

    // 1. 获取业务状态码 (兼容 code 和 status)
    const code = res.code !== undefined ? res.code : (res.status !== undefined ? res.status : 200)

    // 2. 成功直接返回
    if (code === 200 || code === 0) {
      return res
    }

    // 3. 【核心】精准拦截 401 (Token 过期)
    // 这里保留了 403 以防万一，如果你的后端严格返回 401，这里只写 401 也可以
    if (code == 401 || code == 403) {
      // 如果刷新接口本身报 401，直接登出（死循环防护）
      if (config.url.includes('/refresh-token')) {
        handleLogout()
        return Promise.reject(new Error(res.msg || '会话已失效'))
      }
      
      // 进入刷新流程
      return handleRefreshToken(config)
    }

    // 其他业务错误
    ElMessage.error(res.msg || '系统错误')
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  async error => {
    // 处理 HTTP 层面的错误 (如 404, 500, 或后端直接返回 HTTP 401)
    if (!error.response) {
      ElMessage.error('网络连接异常')
      return Promise.reject(error)
    }

    const status = error.response.status
    const config = error.config

    if (status === 401 || status === 403) {
      if (config.url.includes('/refresh-token')) {
        handleLogout()
      } else {
        return handleRefreshToken(config)
      }
    } else {
      let msg = error.message || '请求失败'
      if (status === 404) msg = '接口不存在'
      if (status === 500) msg = '服务器内部错误'
      ElMessage.error(msg)
    }
    
    return Promise.reject(error)
  }
)

export default service