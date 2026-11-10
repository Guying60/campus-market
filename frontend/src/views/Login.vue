<template>
  <div class="login-page">
    <div class="login-content">
      <div class="login-intro">
        <div class="app-title">二手交易平台</div>
        <div class="app-slogan">闲置流转 · 价值再生</div>
        <div class="app-desc">
          让你的闲置物品找到新主人，<br>
          发现身边的高性价比好物。
        </div>
      </div>

      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <span class="header-title">{{ isLogin ? '欢迎回来' : '创建账号' }}</span>
            <el-button link type="primary" @click="toggleMode" class="toggle-btn">
              {{ isLogin ? '去注册 >' : '去登录 >' }}
            </el-button>
          </div>
        </template>

        <el-form 
          :model="form" 
          :rules="rules" 
          ref="formRef" 
          label-position="top"
          size="large"
          status-icon
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password 
              :prefix-icon="Lock"
            />
          </el-form-item>

          <template v-if="!isLogin">
            <el-form-item label="昵称" prop="nickname">
              <el-input 
                v-model="form.nickname" 
                placeholder="您的昵称" 
                :prefix-icon="Postcard"
              />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input 
                v-model="form.phone" 
                placeholder="请输入11位手机号" 
                maxlength="11" 
                :prefix-icon="Iphone"
              />
            </el-form-item>
          </template>

          <el-form-item style="margin-top: 30px;">
            <el-button type="primary" :loading="loading" @click="handleSubmit" round class="submit-btn">
              {{ isLogin ? '立即登录' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Postcard, Iphone } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const isLogin = ref(true)
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: ''
})

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ]
})

const toggleMode = () => {
  isLogin.value = !isLogin.value
  if(formRef.value) formRef.value.resetFields()
}

const handleSubmit = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isLogin.value) {
          // --- 登录逻辑 ---
          const res = await request.post('/user/login', {
            username: form.username,
            password: form.password
          })
          
          ElMessage.success('登录成功')
          
          // 【核心修改】解构后端返回的数据
          // 假设后端返回结构：data: { access_token: '...', refresh_token: '...', id: 1, nickname: '...', ... }
          const { access_token, refresh_token, ...otherUserInfo } = res.data || {}

          if (access_token) {
            // 1. 存储 Access Token (短期令牌)
            localStorage.setItem('access_token', access_token)
            
            // 2. 存储 Refresh Token (长期令牌)
            if (refresh_token) {
              localStorage.setItem('refresh_token', refresh_token)
            }
            
            // 3. 存储用户信息 (用于页面展示)
            localStorage.setItem('user_info', JSON.stringify(otherUserInfo))
            
            // 4. 跳转到大厅
            router.push('/market')
          } else {
            ElMessage.error('登录异常：未获取到令牌')
          }

        } else {
          // --- 注册逻辑 ---
          await request.post('/user/register', {
            username: form.username,
            password: form.password,
            nickname: form.nickname,
            phone: form.phone
          })
          ElMessage.success('注册成功，请直接登录')
          // 注册成功后切换回登录模式，并清空密码
          isLogin.value = true 
          form.password = ''
        }
      } catch (error) {
        console.error('操作失败', error)
        // 错误提示通常由 request 拦截器统一处理，这里记录日志即可
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请检查输入项是否正确')
      return false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 统一使用个人中心的渐变色风格，保持 UI 一致性 */
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  overflow: hidden;
}

.login-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 900px;
  max-width: 90%;
  padding: 40px;
}

.login-intro {
  color: #fff;
  width: 45%;
  padding-right: 40px;
}

.app-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.app-slogan {
  font-size: 20px;
  margin-bottom: 20px;
  opacity: 0.9;
}
.app-desc {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.8;
}

.login-card {
  width: 420px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0 0;
}
.header-title {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}
.toggle-btn {
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
}

/* 响应式调整：手机端隐藏左侧介绍 */
@media (max-width: 768px) {
  .login-content {
    justify-content: center;
    padding: 20px;
  }
  .login-intro {
    display: none;
  }
  .login-card {
    width: 100%;
  }
}
</style>