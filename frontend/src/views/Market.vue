<template>
  <div class="market-page">
    <div class="market-header">
      <div class="header-inner">
        <div class="brand-section">
          <span class="brand-text">二手淘</span>
          <span class="brand-divider">|</span>
          <span class="slogan">发现好物 · 循环利用</span>
        </div>
        
        <div class="header-right">
          <el-badge :value="pendingOrderCount" :max="99" :hidden="pendingOrderCount <= 0" class="pending-badge">
            <el-button type="primary" @click="$router.push('/user')">
              个人中心
            </el-button>
          </el-badge>
        </div>
      </div>
      
      <div class="search-section">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索您感兴趣的宝贝..." 
          clearable 
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          class="main-search-input"
          size="large"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="main-container">
      <div class="category-bar">
        <el-radio-group v-model="currentCategoryId" @change="handleSearch" size="large">
          <el-radio-button :label="0">全部</el-radio-button>
          <el-radio-button :label="1">书籍资料</el-radio-button>
          <el-radio-button :label="2">电子数码</el-radio-button>
          <el-radio-button :label="3">生活用品</el-radio-button>
          <el-radio-button :label="4">体育器材</el-radio-button>
          <el-radio-button :label="99">其它</el-radio-button>
        </el-radio-group>
      </div>

      <div class="sort-bar">
        <div class="sort-section">
          <span class="sort-label">排序方式：</span>
          <div class="sort-group">
            <el-button 
              :type="isLatest ? 'primary' : 'default'" 
              :plain="!isLatest"
              round 
              @click="toggleLatest"
            >
              最新发布
              <el-icon v-if="isLatest" class="el-icon--right"><Check /></el-icon>
            </el-button>

            <el-button 
              :type="priceSort ? 'primary' : 'default'" 
              :plain="!priceSort"
              round 
              @click="togglePrice"
            >
              价格
              <el-icon class="el-icon--right">
                <Top v-if="priceSort === 'asc'" />
                <Bottom v-else-if="priceSort === 'desc'" />
                <Sort v-else />
              </el-icon>
            </el-button>
          </div>
        </div>

        <div class="price-filter-section">
          <span class="sort-label">价格区间：</span>
          <el-input-number 
            v-model="minPrice" 
            :min="0" 
            :controls="false" 
            placeholder="最低价"
            style="width: 100px;"
            @change="handleSearch"
          />
          <span class="range-divider">-</span>
          <el-input-number 
            v-model="maxPrice" 
            :min="0" 
            :controls="false" 
            placeholder="最高价"
            style="width: 100px;"
            @change="handleSearch"
          />
          <el-button type="primary" plain round size="small" style="margin-left: 10px;" @click="handleSearch">筛选</el-button>
        </div>
      </div>

      <div v-loading="loading" class="product-area">
        <el-empty v-if="productList.length === 0" description="暂无相关商品" />
        
        <div class="product-grid" v-else>
          <div 
            v-for="item in productList" 
            :key="item.id" 
            class="product-card" 
            @click="openDetail(item)"
          >
            <div class="card-img-box">
              <img :src="getMainImage(item.image_url)" class="p-img" alt="商品图"/>
            </div>
            
            <div class="card-info">
              <div class="p-title" :title="item.title">{{ item.title }}</div>
              
              <div class="p-bottom">
                <div class="price-wrapper">
                  <div class="price-box">
                    <span class="symbol">¥</span>
                    <span class="num">{{ item.price }}</span>
                  </div>
                  <div v-if="item.original_price > 0" class="list-old-price">
                    ¥{{ item.original_price }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-box" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <el-dialog 
      v-model="detailVisible" 
      title="商品详情" 
      width="800px" 
      destroy-on-close
      align-center
      class="custom-dialog"
    >
      <div class="detail-container">
        <div class="gallery-section">
          <div class="main-image-box">
            <el-image 
              :src="currentMainImage" 
              :preview-src-list="currentImageList" 
              fit="contain"
              class="main-img"
              hide-on-click-modal
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="thumbnail-list" v-if="currentImageList.length > 1">
            <div 
              v-for="(img, index) in currentImageList" 
              :key="index"
              class="thumb-item"
              :class="{ active: currentMainImage === img }"
              @mouseenter="currentMainImage = img"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <div class="info-section">
          <div class="info-header">
            <h2 class="detail-title">{{ currentProduct.title }}</h2>
            
            <div class="detail-price-box">
              <div class="curr-price">
                <span class="cy">¥</span>
                <span :key="currentProduct.price" class="price-num">{{ currentProduct.price }}</span>
              </div>
              <span v-if="currentProduct.original_price" class="old-price">原价 ¥{{ currentProduct.original_price }}</span>
            </div>

            <div class="detail-meta">
              <el-tag effect="plain" type="info" round>浏览 {{ currentProduct.view_count || 0 }}</el-tag>
              <el-tag effect="light" type="primary" round style="margin-left: 8px;">{{ getCategoryName(currentProduct.category_id) }}</el-tag>
            </div>

            <div class="seller-info-block">
              <div class="seller-item">
                <el-icon class="s-icon"><User /></el-icon>
                <span class="s-label">卖家昵称：</span>
                <span class="s-value">{{ currentProduct.seller_name || '匿名卖家' }}</span>
              </div>
              <div class="seller-item">
                <el-icon class="s-icon"><Iphone /></el-icon>
                <span class="s-label">联系电话：</span>
                <span class="s-value select-enable">{{ currentProduct.seller_phone || '暂无联系方式' }}</span>
              </div>
               <div class="time-info-block" style="margin-top: 10px; color: #999; font-size: 13px;">
                <div style="margin-bottom: 4px;">发布时间：{{ currentProduct.create_time || '未知' }}</div>
                <div v-if="currentProduct.update_time">更新时间：{{ currentProduct.update_time }}</div>
              </div>
            </div>
            
          </div>
          
          <el-divider content-position="left">卖家描述</el-divider>
          
          <div class="description-content" v-loading="detailLoading">
            <p>{{ currentProduct.description || '卖家暂无详细描述...' }}</p>
          </div>

          <div class="action-footer">
            <div v-if="isSelfProduct" class="self-product-msg">
              <el-tag type="warning" size="large" effect="plain" style="width: 100%; justify-content: center;">
                此商品为自己发布，不可购买
              </el-tag>
            </div>
            <el-button v-else type="danger" size="large" class="buy-btn" @click="handleConfirmBuy" round>
              立即购买
            </el-button>
            
            <el-button size="large" @click="detailVisible = false" round>
              再逛逛
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="buyDialogVisible"
      title="填写收货信息"
      width="500px"
      destroy-on-close
      align-center
    >
      <el-form 
        :model="buyForm" 
        :rules="buyRules" 
        ref="buyFormRef" 
        label-width="90px" 
        size="large"
      >
        <el-form-item label="手机号码" prop="phone">
          <el-input 
            v-model="buyForm.phone" 
            placeholder="请输入联系电话" 
            maxlength="11" 
          />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input 
            v-model="buyForm.address" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入详细的收货地址" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="createOrderLoading">确认下单</el-button>
      </template>
    </el-dialog>

    <div class="ai-float-btn" @click="toggleChatWindow">
      <el-icon class="ai-icon"><ChatDotRound /></el-icon>
      <span class="ai-text">AI助手</span>
    </div>

    <transition name="el-zoom-in-bottom">
      <div v-if="aiChatVisible" class="ai-chat-window">
        <div class="chat-header">
          <div class="header-left">
            <el-icon><Service /></el-icon>
            <span>智能导购助手</span>
          </div>
          <el-icon class="close-btn" @click="toggleChatWindow"><Close /></el-icon>
        </div>
        
        <div class="chat-body">
          <div class="chat-sidebar">
            <div class="sidebar-title">历史会话</div>
            <div class="history-list" v-loading="historyLoading">
              <el-empty v-if="chatHistoryList.length === 0" description="暂无记录" :image-size="40" />
              <div 
                v-for="chatId in chatHistoryList" 
                :key="chatId" 
                class="history-item"
                :class="{ active: currentChatId === chatId }"
                @click="loadChatDetail(chatId)"
              >
                <div class="h-text-wrapper">
                  <el-icon><ChatLineRound /></el-icon>
                  <span class="h-text">会话 {{ chatId }}</span>
                </div>
                <el-icon class="delete-icon" @click.stop="deleteChat(chatId)"><Delete /></el-icon>
              </div>
            </div>
            <div class="new-chat-btn" @click="startNewChat">
              <el-button type="primary" plain size="small" style="width: 100%">+ 新对话</el-button>
            </div>
          </div>

          <div class="chat-main">
            <div class="chat-notice">
              <el-icon><InfoFilled /></el-icon>
              <span>提示：对话会在 24 小时后自动删除</span>
            </div>

            <div class="messages-container" ref="messageBoxRef">
              <div v-for="(msg, index) in currentMessages" :key="index" class="message-row" :class="msg.role === 'user' ? 'msg-right' : 'msg-left'">
                 <div class="avatar" v-if="msg.role !== 'user'">
                   <el-icon><Cpu /></el-icon>
                 </div>
                 <div class="bubble">
                    <div class="bubble-content" v-html="formatMessage(msg.content)"></div>
                 </div>
                 <div class="avatar" v-if="msg.role === 'user'">
                   <el-icon><User /></el-icon>
                 </div>
              </div>
              <div v-if="isAiThinking" class="message-row msg-left">
                 <div class="avatar"><el-icon><Cpu /></el-icon></div>
                 <div class="bubble thinking">
                    <span>.</span><span>.</span><span>.</span>
                 </div>
              </div>
            </div>

            <div class="input-area">
              <el-input
                v-model="aiInputContent"
                type="textarea"
                :rows="2"
                resize="none"
                placeholder="请输入您想查找的商品或问题..."
                @keydown.enter.prevent="sendAiMessage"
              />
              <div class="send-toolbar">
                <el-button type="primary" size="small" @click="sendAiMessage" :loading="isAiThinking">
                  发送 <el-icon class="el-icon--right"><Promotion /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, nextTick } from 'vue' 
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, View, Picture, User, Iphone, Top, Bottom, Sort, Check, 
  ChatDotRound, Close, Service, ChatLineRound, Cpu, Promotion,
  Delete, InfoFilled 
} from '@element-plus/icons-vue' 
import request from '@/utils/request'

const router = useRouter()
const productList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const currentCategoryId = ref(0)
const currentUserId = ref(null)

const isLatest = ref(false)   
const priceSort = ref('')     

const minPrice = ref(undefined)
const maxPrice = ref(undefined)

const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentProduct = ref({})
const currentMainImage = ref('')
const currentImageList = ref([])

const buyDialogVisible = ref(false)
const createOrderLoading = ref(false)
const buyFormRef = ref(null)
const buyForm = reactive({
  phone: '',
  address: ''
})

const buyRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

// --- AI 助手相关状态 ---
const aiChatVisible = ref(false)
const historyLoading = ref(false)
const chatHistoryList = ref([])
const currentChatId = ref('')
const currentMessages = ref([])
const aiInputContent = ref('')
const isAiThinking = ref(false)
const messageBoxRef = ref(null)

// --- 新增：待处理订单状态 ---
const pendingOrderCount = ref(0)

// ---------------------------
// 待处理订单红点逻辑
// ---------------------------
const fetchPendingOrderCount = async () => {
  const token = localStorage.getItem('access_token')
  if (!token) return // 未登录则不请求
  try {
    const res = await request.get('/order/pending-order')
    // result的data结构解构
    if (res && res.data !== undefined && res.data !== null) {
      pendingOrderCount.value = Number(res.data)
    }
  } catch (error) {
    console.error('获取待处理订单数失败', error)
  }
}

// ---------------------------
// AI 助手逻辑
// ---------------------------
const toggleChatWindow = () => {
  aiChatVisible.value = !aiChatVisible.value
  if (aiChatVisible.value) {
    fetchChatHistory()
    // 增加这一行：窗口重新渲染后，确保聊天记录滚动到最底部
    scrollToBottom()
  }
}

const fetchChatHistory = async () => {
  historyLoading.value = true
  try {
    const res = await request.get('/ai/history')
    if (res && Array.isArray(res)) {
       chatHistoryList.value = res.reverse() // 最新的在上面
    } else if (res.data && Array.isArray(res.data)) {
       chatHistoryList.value = res.data.reverse()
    } else {
       chatHistoryList.value = []
    }
  } catch (error) {
    console.error('获取AI历史记录失败', error)
  } finally {
    historyLoading.value = false
  }
}

const loadChatDetail = async (chatId) => {
  currentChatId.value = chatId
  // 切换会话时，先清空当前视图
  currentMessages.value = []
  
  try {
    const res = await request.get(`/ai/history/${chatId}`)
    // 假设返回结构是 List<MessageVO>，包含 role 和 content
    const list = Array.isArray(res) ? res : (res.data || [])
    currentMessages.value = list.map(item => ({
       role: item.role || (item.messageType === 'USER' ? 'user' : 'assistant'),
       content: item.content || item.message
    }))
    scrollToBottom()
  } catch (error) {
    ElMessage.error('获取会话详情失败')
  }
}

const startNewChat = () => {
  currentChatId.value = ''
  currentMessages.value = []
}

// 删除对话逻辑
const deleteChat = async (chatId) => {
  try {
    await ElMessageBox.confirm('确认删除该会话记录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    // 1. 发送删除请求 (路径参数)
    await request.delete(`/ai/history/${chatId}`)
    ElMessage.success('删除成功')
    
    // 2. 如果删除的是当前正在查看的会话，则清空右侧视图
    if (currentChatId.value === chatId) {
      startNewChat()
    }
    
    // 3. 重新拉取历史列表
    fetchChatHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败', error)
    }
  }
}

const sendAiMessage = async () => {
  const prompt = aiInputContent.value.trim()
  if (!prompt) return
  if (isAiThinking.value) return

  // 如果当前没有 chatId（新对话），前端生成一个随机的 chatId
  if (!currentChatId.value) {
    currentChatId.value = 'chat_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
  }

  // 1. 添加用户消息到界面
  currentMessages.value.push({ role: 'user', content: prompt })
  aiInputContent.value = ''
  scrollToBottom()
  
  isAiThinking.value = true
  let aiMsgIndex = -1

  // 将实际的请求逻辑封装在内部函数中，方便在 Token 刷新后进行重试
  const executeFetch = async (isRetry = false) => {
    // 每次执行时都实时从 localStorage 获取最新的 token
    const apiBase = import.meta.env.PROD ? import.meta.env.VITE_API_BASE_URL : '/api'
    let url = `${apiBase}/ai/service?prompt=${encodeURIComponent(prompt)}&chatId=${currentChatId.value}`

    const response = await fetch(url, {
      method: 'GET', 
      headers: {
        'Authorization': token || ''
      }
    })

    // 检查是否发生鉴权失败 (401/403)
    let isUnauthorized = false
    const contentType = response.headers.get('content-type') || ''
    
    if (contentType.includes('application/json')) {
      const resJson = await response.json()
      if (resJson.code === 401 || resJson.code === 403) {
        isUnauthorized = true
      } else {
        throw new Error(resJson.msg || '系统异常')
      }
    } else if (!response.ok && (response.status === 401 || response.status === 403)) {
      isUnauthorized = true
    }

    // ================== 【核心：双 Token 无感刷新触发点】 ==================
    if (isUnauthorized) {
      if (isRetry) {
        // 如果已经是重试了依然 401，说明长期 Token 也过期了，抛出特定错误终止
        throw new Error('AUTH_FAILED')
      }
      
      console.log('检测到 AI 助手短期 Token 过期，正在触发无感刷新...')
      
      // 巧妙借用现有的 axios 实例发一个请求，触发 utils/request.js 的无感刷新队列
      // request.js 会在内部自动拦截 401，用 refresh_token 换取新 token
      await request.get('/user/info') 
      
      // 如果上一行没有报错抛出，说明无感刷新完美成功！
      // 此时 localStorage 里已经是崭新的 access_token 了，我们带着新 token 重试一次
      return await executeFetch(true)
    }
    // ======================================================================

    if (!response.ok) {
      throw new Error('网络请求错误')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let done = false

    while (!done) {
      const { value, done: readerDone } = await reader.read()
      done = readerDone
      if (value) {
        // 当收到第一波流式数据时，关闭 loading 动画并推入真正的对话气泡
        if (isAiThinking.value) {
          isAiThinking.value = false
          aiMsgIndex = currentMessages.value.length
          currentMessages.value.push({ role: 'assistant', content: '' })
        }

        const chunk = decoder.decode(value, { stream: true })
        currentMessages.value[aiMsgIndex].content += chunk
        scrollToBottom()
      }
    }
  }

  // 尝试执行请求
  try {
    await executeFetch()
    // 对话结束后，刷新左侧历史列表
    await fetchChatHistory()
  } catch (error) {
    if (error.message === 'AUTH_FAILED' || error.message.includes('会话已失效')) {
       // 如果走到这里，说明长期 refresh_token 也过期了
       // request.js 里的拦截器其实已经自动处理了清除本地缓存并跳回 /login 的操作
       // 所以这里我们只需要静默撤回界面上刚才推入的用户消息气泡即可
       currentMessages.value.pop()
    } else {
       console.error('AI请求失败', error)
       if (aiMsgIndex === -1) {
          currentMessages.value.push({ role: 'assistant', content: '\n[网络异常，请稍后重试]' })
       } else {
          currentMessages.value[aiMsgIndex].content += '\n[网络异常，请稍后重试]'
       }
    }
  } finally {
    isAiThinking.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageBoxRef.value) {
      messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// ---------------------------
// 原有业务逻辑
// ---------------------------

const getMainImage = (urlStr) => {
  if (!urlStr) return ''
  return urlStr.split(',')[0]
}

const getCategoryName = (id) => {
  const map = { 1: '书籍资料', 2: '电子数码', 3: '生活用品', 4: '体育器材', 99: '其它' }
  return map[id] || '闲置宝贝'
}

const toggleLatest = () => {
  isLatest.value = !isLatest.value 
  handleSearch()
}

const togglePrice = () => {
  if (priceSort.value === '') {
    priceSort.value = 'asc'
  } else if (priceSort.value === 'asc') {
    priceSort.value = 'desc'
  } else {
    priceSort.value = ''
  }
  handleSearch()
}

const isSelfProduct = computed(() => {
  if (!currentProduct.value || !currentUserId.value) return false
  const productUserId = currentProduct.value.user_id || currentProduct.value.seller_id || currentProduct.value.userId
  return String(productUserId) === String(currentUserId.value)
})

const fetchProducts = async () => {
  loading.value = true
  
  const sortFields = []
  const orderFields = []

  if (isLatest.value) {
    sortFields.push('create_time')
    orderFields.push('desc')
  }

  if (priceSort.value) {
    sortFields.push('price')
    orderFields.push(priceSort.value)
  }

  try {
    const res = await request.get('/product/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value,
        categoryId: currentCategoryId.value === 0 ? null : currentCategoryId.value,
        sort: sortFields.join(',') || undefined,
        order: orderFields.join(',') || undefined,
        minPrice: minPrice.value,
        maxPrice: maxPrice.value
      }
    })
    
    if (res.data) {
      const listData = res.data.list || res.data.records
      if (listData) {
        productList.value = listData
        total.value = res.data.total || 0
      } else {
        if (Array.isArray(res.data)) {
           productList.value = res.data
           total.value = res.data.length
        } else {
           productList.value = []
           total.value = 0
        }
      }
    } else {
      productList.value = []
      total.value = 0
    }

  } catch (error) {
    console.error('获取商品列表失败', error)
    productList.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchProducts()
}

const fetchProductDynamic = async (id) => {
  try {
    const res = await request.get(`/product/dynamic/${id}`) 
    
    if (res.data && currentProduct.value.id === id) {
      const { price, view_count } = res.data
      
      if (price !== undefined && price !== null) {
        currentProduct.value.price = price
      }
      
      if (view_count !== undefined && view_count !== null) {
        currentProduct.value.view_count = view_count
      }
    }
  } catch (error) {
    console.error('实时动态数据加载失败，维持缓存显示', error)
  }
}

const openDetail = async (item) => {
  currentProduct.value = { ...item } 
  
  const initImgs = (item.image_url || '').split(',').filter(Boolean)
  currentImageList.value = initImgs.length > 0 ? initImgs : [''] 
  currentMainImage.value = initImgs[0] || ''
  
  detailVisible.value = true 
  detailLoading.value = true

  const staticRequest = request.get(`/product/detail/${item.id}`).then(res => {
      const { description, 
        image_url, 
        seller_name, 
        seller_phone, 
        create_time, 
        update_time,
        category_id } = res.data || {}
      
      if (description) currentProduct.value.description = description
      if (seller_name) currentProduct.value.seller_name = seller_name
      if (seller_phone) currentProduct.value.seller_phone = seller_phone
      if (create_time) currentProduct.value.create_time = create_time
      if (update_time) currentProduct.value.update_time = update_time
      if (category_id) currentProduct.value.category_id = category_id
      
      if (image_url) {
         const newImgs = image_url.split(',').filter(Boolean)
         if (newImgs.length > 0) {
            currentImageList.value = newImgs
            if (!currentMainImage.value) currentMainImage.value = newImgs[0]
         }
      }
  }).catch(e => console.error('详情加载失败', e))

  const dynamicRequest = fetchProductDynamic(item.id)

  try {
    await staticRequest 
  } finally {
    detailLoading.value = false
  }
}

const handleConfirmBuy = async () => {
  buyDialogVisible.value = true
  
  if (buyFormRef.value) {
    buyFormRef.value.resetFields()
  }

  try {
    const res = await request.get('/user/info')
    if (res.data) {
      if (res.data.phone) buyForm.phone = res.data.phone
      if (res.data.address) buyForm.address = res.data.address
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const submitOrder = async () => {
  if (!buyFormRef.value) return
  
  await buyFormRef.value.validate(async (valid) => {
    if (valid) {
      createOrderLoading.value = true
      try {
        const res = await request.post('/order/create', { 
          product_id: currentProduct.value.id,
          address: buyForm.address,
          phone: buyForm.phone
        })
        
        const orderId = res.data ? res.data.order_id : null
        
        buyDialogVisible.value = false 
        detailVisible.value = false 

        ElMessageBox.confirm('订单创建成功！是否立即进行支付？', '支付提醒', {
          confirmButtonText: '立即支付',
          cancelButtonText: '稍后支付',
          type: 'success',
          distinguishCancelAndClose: true
        }).then(async () => {
           if (orderId) {
               await request.post(`/order/pay/${orderId}`)
               ElMessage.success('支付成功！')
               fetchProducts() 
               fetchPendingOrderCount() // 刷新订单红点
           } else {
               ElMessage.warning('无法获取订单ID')
           }
        }).catch(() => {
           ElMessage.info('请在【个人中心-我的订单】中支付')
           fetchProducts()
           fetchPendingOrderCount() // 刷新订单红点
        })
        
      } catch (e) {
        // request 拦截器已处理错误
      } finally {
        createOrderLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchProducts()
  fetchPendingOrderCount() // 新增：挂载时获取待处理订单角标数据
  
  const token = localStorage.getItem('access_token')
  if (token) {
    try {
      const payloadPart = token.split('.')[1]
      const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      }).join(''))

      const payload = JSON.parse(jsonPayload)
      currentUserId.value = payload.id || payload.userId || payload.sub || payload.uid
    } catch (e) {
      console.error('Token 解析失败', e)
    }
  }
})
</script>

<style scoped>
/* 保持原有样式不变 */
.market-page {
  min-height: 100vh;
  background-color: #f0f2f5; 
  display: flex;
  flex-direction: column;
}

.market-header {
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); 
  padding: 20px 0 40px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}
.brand-section {
  display: flex;
  align-items: center;
  color: #fff;
}
.brand-text {
  font-size: 28px;
  font-weight: bold;
  letter-spacing: 1px;
}
.brand-divider {
  margin: 0 15px;
  opacity: 0.6;
}
.slogan {
  font-size: 14px;
  opacity: 0.9;
  letter-spacing: 1px;
}

/* 调整右上角红点布局样式 */
.header-right .pending-badge {
  margin-right: 10px;
}

.search-section {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}
.main-search-input :deep(.el-input__wrapper) {
  border-radius: 30px 0 0 30px;
  box-shadow: none;
}
.main-search-input :deep(.el-input-group__append) {
  border-radius: 0 30px 30px 0;
  background-color: #fff;
  color: #409EFF;
  font-weight: bold;
  box-shadow: none;
}
.main-search-input :deep(.el-input-group__append button:hover) {
  color: #66b1ff;
}

.main-container {
  max-width: 1200px;
  margin: -20px auto 20px; 
  padding: 0 20px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

.category-bar {
  background: #fff;
  padding: 15px;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  display: flex;
  justify-content: center;
  border-bottom: 1px solid #f5f5f5;
}

.sort-bar {
  background: #fff;
  padding: 10px 15px;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap; 
  gap: 15px;
}
.sort-section, .price-filter-section {
  display: flex;
  align-items: center;
}
.sort-label {
  font-size: 14px;
  color: #666;
  margin-right: 15px;
}
.sort-group {
  display: flex;
  gap: 15px;
}
.price-filter-section .range-divider {
  margin: 0 8px;
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border-color: #e6e6e6;
}
.card-img-box {
  width: 100%;
  height: 200px;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.p-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.product-card:hover .p-img {
  transform: scale(1.05);
}
.card-info {
  padding: 12px;
}
.p-title {
  font-size: 15px;
  color: #333;
  margin-bottom: 10px;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.p-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.price-box {
  color: #ff5000;
  font-weight: bold;
}
.price-wrapper {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.list-old-price {
  font-size: 12px;
  color: #ccc; 
  text-decoration: line-through; 
}
.symbol { font-size: 12px; margin-right: 2px; }
.num { font-size: 18px; }

.pagination-box {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 40px;
}

.detail-container {
  display: flex;
  gap: 30px;
  min-height: 400px;
}
.gallery-section {
  width: 380px;
  flex-shrink: 0;
}
.main-image-box {
  width: 100%;
  height: 350px;
  background: #f8f8f8;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  overflow: hidden;
}
.thumbnail-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}
.thumb-item {
  width: 60px;
  height: 60px;
  border: 2px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0.7;
}
.thumb-item.active {
  border-color: #409EFF;
  opacity: 1;
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 2px;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.detail-title {
  font-size: 20px;
  margin: 0 0 15px;
  line-height: 1.4;
}
.detail-price-box {
  background: #fff5f5;
  padding: 10px 15px;
  border-radius: 6px;
  margin-bottom: 15px;
  color: #f56c6c;
  display: flex;
  align-items: baseline;
}
.curr-price {
  font-size: 24px;
  font-weight: bold;
}
.curr-price .cy { font-size: 16px; margin-right: 2px; }
.old-price { margin-left: 10px; color: #999; text-decoration: line-through; font-size: 13px; }
.description-content {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  min-height: 100px;
  margin-bottom: 20px;
}
.action-footer {
  margin-top: auto;
  display: flex;
  gap: 15px;
}
.buy-btn {
  flex: 1;
  letter-spacing: 1px;
  font-weight: bold;
}
.self-product-msg {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* --- AI 悬浮球 & 聊天窗口样式 --- */
.ai-float-btn {
  position: fixed;
  bottom: 50px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409EFF 0%, #a1c4fd 100%);
  border-radius: 50%;
  box-shadow: 0 6px 16px rgba(64,158,255,0.4);
  cursor: pointer;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(64,158,255,0.5);
}
.ai-icon {
  font-size: 24px;
  margin-bottom: 2px;
}
.ai-text {
  font-size: 10px;
}

.ai-chat-window {
  position: fixed;
  bottom: 120px;
  right: 30px;
  width: 800px;
  height: 600px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  z-index: 2001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eee;
}

.chat-header {
  height: 50px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #333;
}
.close-btn {
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
}
.close-btn:hover { background: #e6e6e6; }

.chat-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 侧边栏 */
.chat-sidebar {
  width: 220px;
  background: #fcfcfc;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.sidebar-title {
  padding: 15px;
  font-size: 14px;
  color: #999;
  border-bottom: 1px solid #f0f0f0;
}
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}
.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between; 
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  color: #555;
  font-size: 13px;
  transition: background 0.2s;
  margin-bottom: 4px;
}
.history-item:hover {
  background: #f0f5ff;
}
.history-item.active {
  background: #ecf5ff;
  color: #409EFF;
}
.h-text-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}
.h-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.delete-icon {
  font-size: 14px;
  color: #c0c4cc;
  transition: color 0.2s;
}
.delete-icon:hover {
  color: #f56c6c;
}

.new-chat-btn {
  padding: 10px;
  border-top: 1px solid #f0f0f0;
}

/* 主聊天区 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  position: relative;
}

.chat-notice {
  padding: 8px 15px;
  background-color: #fdf6ec;
  color: #e6a23c;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1px solid #faecd8;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fff;
}
.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.msg-left { justify-content: flex-start; }
.msg-right { justify-content: flex-end; }

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f2f2;
  color: #666;
  flex-shrink: 0;
}
.msg-right .avatar {
  background: #409EFF;
  color: #fff;
  order: 2; /* 头像在右 */
}

.bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}
.msg-left .bubble {
  background: #f4f4f5;
  color: #333;
  border-top-left-radius: 0;
}
.msg-right .bubble {
  background: #ecf5ff;
  color: #409EFF;
  border-top-right-radius: 0;
}
.thinking span {
  display: inline-block;
  animation: dot-blink 1.4s infinite;
  margin: 0 2px;
  font-weight: bold;
}
.thinking span:nth-child(2) { animation-delay: 0.2s; }
.thinking span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dot-blink {
  0% { opacity: 0.2; }
  20% { opacity: 1; }
  100% { opacity: 0.2; }
}

.input-area {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}
.send-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>