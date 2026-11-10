import request from '@/utils/request'

// 登录接口
// 实际请求路径：前端 /api/user/login -> 代理转发 -> 后端 /user/login
export function login(data: any) {
  return request({
    url: '/user/login', // 千万别在这里写 /api，baseURL 已经帮你加了
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}