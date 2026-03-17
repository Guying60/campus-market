<template>
  <div class="user-center-page">
    <div class="profile-header">
      <div class="cover-banner"></div>
      <div class="user-info-bar">
        <div class="info-content">
          <div class="avatar-wrapper">
             <el-upload
              class="avatar-uploader"
              action="#"
              :http-request="customUpload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeUpload"
              :on-error="handleUploadError"
            >
              <div class="avatar-box" v-loading="avatarLoading" element-loading-background="rgba(255, 255, 255, 0.7)">
                <img v-if="userInfo.avatar" :src="userInfo.avatar" class="real-avatar" />
                <div v-else class="default-avatar">
                   <el-icon><UserFilled /></el-icon>
                </div>
                <div class="upload-mask"><el-icon><Camera /></el-icon></div>
              </div>
            </el-upload>
          </div>
          <div class="text-info">
            <div class="name-row">
              <span class="nickname">{{ userInfo.nickname || '未设置昵称' }}</span>
              <el-tag size="small" effect="dark" round type="warning" class="role-tag">普通用户</el-tag>
            </div>
            <div class="detail-row">
              <span class="detail-item"><el-icon><Iphone /></el-icon> {{ userInfo.phone || '暂无手机号' }}</span>
              <el-divider direction="vertical" />
              <span class="detail-item"><el-icon><LocationInformation /></el-icon> {{ userInfo.address || '暂无地址' }}</span>
            </div>
          </div>
        </div>
        <div class="action-group">
          <el-button type="primary" plain round @click="openEditDialog">编辑资料</el-button>
          <el-button type="danger" plain round @click="handleLogout">退出登录</el-button>
          <el-button plain round @click="$router.push('/market')">返回大厅</el-button>
        </div>
      </div>
    </div>

    <div class="main-container">
      <el-tabs type="border-card" class="content-tabs">
        <el-tab-pane label="交易中心">
          <template #label>
            <span class="custom-tab-label"><el-icon><List /></el-icon> 交易中心</span>
          </template>

          <el-tabs v-model="transactionTab" class="sub-tabs">
            <el-tab-pane name="buy" label="我买到的">
              <div class="filter-bar">
                 <el-input v-model="buyKeyword" placeholder="搜索已购商品..." prefix-icon="Search" style="width: 220px;" clearable />
              </div>
              <el-table :data="filteredBuyOrders" stripe style="width: 100%" v-loading="loadingBuy">
                <el-table-column label="订单编号" width="140" align="center">
                  <template #default="{row}">{{ row.order_no || row.id }}</template>
                </el-table-column>
                <el-table-column label="商品信息" min-width="180">
                  <template #default="{row}">
                    <div class="product-cell">
                      <div class="cell-text">
                        <div class="p-title" :title="row.product_title">{{ row.product_title }}</div>
                        <div class="p-time">{{ row.create_time }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="卖家信息" width="160">
                   <template #default="{row}">
                      <div class="user-info-col">
                        <div class="u-item"><el-icon><User /></el-icon> {{ row.seller_name || '未知' }}</div>
                        <div class="u-item"><el-icon><Iphone /></el-icon> {{ row.seller_phone || '暂无' }}</div>
                      </div>
                   </template>
                </el-table-column>
                <el-table-column label="实付" width="100">
                  <template #default="{row}"><span class="price-text">¥{{row.price}}</span></template>
                </el-table-column>
                <el-table-column label="状态" width="90">
                   <template #default="{row}">
                      <el-tag :type="getOrderStatusType(row.status)" effect="light">{{ formatOrderStatus(row.status) }}</el-tag>
                   </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="{row}">
                    <el-button link type="primary" @click="handleOrderDetail(row)">详情</el-button>
                    <el-button v-if="row.status === 0" size="small" type="primary" @click="handlePay(row)">去支付</el-button>
                    <el-button v-if="row.status === 2" size="small" type="success" @click="confirmReceive(row)">确认收货</el-button>
                    <el-button v-if="[0,1].includes(row.status)" size="small" type="danger" plain @click="cancelOrder(row)">取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!loadingBuy && filteredBuyOrders.length===0" description="暂无购买记录" />
            </el-tab-pane>

            <el-tab-pane name="sell" label="我卖出的">
              <div class="filter-bar">
                 <el-input v-model="sellKeyword" placeholder="搜索售出订单..." prefix-icon="Search" style="width: 220px;" clearable />
              </div>
              <el-table :data="filteredSellOrders" stripe style="width: 100%" v-loading="loadingSell">
                <el-table-column label="订单编号" width="140" align="center">
                  <template #default="{row}">{{ row.order_no || row.id }}</template>
                </el-table-column>
                <el-table-column label="商品" min-width="150">
                  <template #default="{row}">
                    <div class="product-cell">
                      <span class="p-title" :title="row.product_title">{{ row.product_title }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="买家信息" min-width="220">
                  <template #default="{row}">
                    <div class="user-info-col">
                      <div class="u-item">
                        <el-icon><User /></el-icon> {{ row.customer_name || '买家' }}
                      </div>
                      <div class="u-item">
                        <el-icon><Iphone /></el-icon> {{ row.customer_phone || '暂无电话' }}
                      </div>
                      <div class="u-item addr">
                        <el-icon><Location /></el-icon> 
                        <span v-if="row.address">{{ row.address }}</span>
                        <span v-else style="color:#999">待买家填写地址</span>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="成交价" width="100">
                  <template #default="{row}"><span class="price-text">¥{{row.price}}</span></template>
                </el-table-column>
                <el-table-column label="状态" width="90">
                   <template #default="{row}">
                      <el-tag :type="getOrderStatusType(row.status)">{{ formatOrderStatus(row.status) }}</el-tag>
                   </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right">
                   <template #default="{row}">
                      <el-button link type="primary" @click="handleOrderDetail(row)">详情</el-button>
                      <el-button v-if="row.status === 1" type="success" size="small" @click="shipOrder(row)">
                        <el-icon><Van /></el-icon> 发货
                      </el-button>
                      <span v-else class="status-tip">
                        {{ row.status === 0 ? '待付款' : (row.status===2 ? '已发货' : '—') }}
                      </span>
                   </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!loadingSell && filteredSellOrders.length===0" description="暂无卖出记录" />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <el-tab-pane label="我的发布">
           <template #label>
            <span class="custom-tab-label"><el-icon><Goods /></el-icon> 我的发布</span>
          </template>

           <div class="inventory-header">
              <el-button type="primary" size="large"  @click="showPublishDialog" class="pub-btn">发布闲置宝贝</el-button>
              <el-input v-model="productKeyword" placeholder="搜索库存..." prefix-icon="Search" style="width: 250px;" clearable />
           </div>
           
           <div class="product-grid-view" v-loading="loadingProduct">
             <el-empty v-if="filteredProducts.length === 0" description="您还没发布过商品" />
             <div v-else class="grid-container">
               <div v-for="item in filteredProducts" :key="item.id" class="grid-item">
                 <div class="img-box">
                   <el-image :src="getMainImage(item.image_url)" fit="cover" class="p-img"/>
                   <div class="status-overlay" v-if="item.status !== 0">
                     {{ formatStatus(item.status) }}
                   </div>
                 </div>
                 <div class="info-box">
                   <div class="g-title" :title="item.title">{{ item.title }}</div>
                   <div class="g-desc" :title="item.description">{{ item.description || '暂无描述' }}</div>
                   <div class="g-price">¥{{ item.price }}</div>
                   <div class="g-actions">
                     <el-button v-if="item.status !== 1 && item.status !== 3" size="small" type="primary" link @click="editProduct(item)">编辑</el-button>
                     <el-button v-if="item.status === 0" size="small" type="warning" link @click="handleStatusChange(item, 2)">下架</el-button>
                     <el-button v-if="item.status === 2" size="small" type="success" link @click="handleStatusChange(item, 0)">上架</el-button>
                     <el-button v-if="item.status !== 1" size="small" type="danger" link @click="handleDelete(item)">删除</el-button>
                   </div>
                 </div>
               </div>
             </div>
           </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editProfileVisible" title="编辑个人资料" width="500px" destroy-on-close>
      <el-form :model="userInfo" label-width="80px" class="edit-form">
        <el-form-item label="昵称"><el-input v-model="userInfo.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="userInfo.phone" maxlength="11" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="userInfo.email" /></el-form-item>
        <el-form-item label="收货地址"><el-input v-model="userInfo.address" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editProfileVisible = false">取消</el-button>
        <el-button type="primary" @click="updateProfile" :loading="loadingSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="publishVisible" :title="productForm.id ? '编辑商品' : '发布宝贝'" width="600px" top="5vh">
      <el-form :model="productForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="productForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="productForm.description" type="textarea" :rows="4" show-word-limit maxlength="200"/></el-form-item>
        <el-form-item label="价格">
          <el-row :gutter="20">
             <el-col :span="12"><el-input-number v-model="productForm.price" :min="0" :precision="2" style="width:100%" controls-position="right"><template #prefix>¥</template></el-input-number></el-col>
             <el-col :span="12"><el-input-number v-model="productForm.original_price" :min="0" :precision="2" style="width:100%" controls-position="right"><template #prefix>原</template></el-input-number></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="分类">
           <el-radio-group v-model="productForm.category_id">
             <el-radio-button :label="1">书籍</el-radio-button>
             <el-radio-button :label="2">数码</el-radio-button>
             <el-radio-button :label="3">生活</el-radio-button>
             <el-radio-button :label="4">运动</el-radio-button>
             <el-radio-button :label="99">其它</el-radio-button>
           </el-radio-group>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload 
            v-model:file-list="fileList" 
            action="#" 
            list-type="picture-card" 
            :http-request="customUpload" 
            :limit="5" 
            :on-exceed="handleExceed" 
            :on-success="handleUploadSuccess" 
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProduct">提交发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="550px" destroy-on-close>
      <div v-if="currentOrder.id || currentOrder.order_no" class="detail-box">
        <div class="d-header">
           <div class="d-status">{{ formatOrderStatus(currentOrder.status) }}</div>
           <div class="d-id">订单号：{{ currentOrder.order_no || currentOrder.id }}</div>
        </div>
        
        <div class="d-product">
           <el-image 
             :src="getMainImage(currentOrder.image_url)" 
             class="d-img" 
             fit="cover"
             :preview-src-list="detailPreviewList"
             :initial-index="0"
             hide-on-click-modal
           >
             <template #error><el-icon><Picture /></el-icon></template>
           </el-image>
           
           <div class="d-info">
             <div class="d-title">{{ currentOrder.product_title }}</div>
             <div class="d-price">¥{{ currentOrder.price }}</div>
           </div>
        </div>
        
        <div class="d-desc-section" v-loading="loadingDesc">
           <div class="desc-label">商品描述</div>
           <div class="desc-text">{{ currentOrder.description || '暂无描述' }}</div>
        </div>

        <div class="d-divider"></div>

        <div class="d-cell-group">
           <div class="d-cell"><span class="label">下单时间</span><span class="value">{{ currentOrder.create_time }}</span></div>
           <div class="d-cell" v-if="currentOrder.pay_time">
             <span class="label">付款时间</span><span class="value">{{ currentOrder.pay_time }}</span>
           </div>
           <div class="d-cell" v-if="currentOrder.delivery_time">
             <span class="label">发货时间</span><span class="value">{{ currentOrder.delivery_time }}</span>
           </div>
           <div class="d-cell" v-if="currentOrder.complete_time">
             <span class="label">成交时间</span><span class="value">{{ currentOrder.complete_time }}</span>
           </div>
           <div class="d-cell" v-if="currentOrder.cancel_time">
             <span class="label">取消时间</span><span class="value">{{ currentOrder.cancel_time }}</span>
           </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Camera, Location, Van, 
  UserFilled, Iphone, LocationInformation, List, Goods, User, Picture
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// --- 状态数据 ---
const transactionTab = ref('buy')
const loadingSave = ref(false)
const loadingProduct = ref(false)
const loadingBuy = ref(false)
const loadingSell = ref(false)
const editProfileVisible = ref(false)
const loadingDesc = ref(false)
const avatarLoading = ref(false)

const userInfo = reactive({ nickname: '', avatar: '', phone: '', email: '', address: '' })
const myProducts = ref([])
const buyOrders = ref([])
const sellOrders = ref([])

const productKeyword = ref('')
const buyKeyword = ref('')
const sellKeyword = ref('')

const publishVisible = ref(false)
const detailVisible = ref(false)
const currentOrder = ref({})

const detailPreviewList = ref([])

const productForm = reactive({ id: null, title: '', description: '', price: 0, original_price: 0, image_url: '', category_id: 1 })
const fileList = ref([])

// --- 自定义上传逻辑，强制走我们封装的 Axios ---
const customUpload = async (options) => {
  const { file, onSuccess, onError, filename } = options
  const formData = new FormData()
  // 后端接收文件的字段名，通常是 file
  formData.append(filename || 'file', file)
  
  try {
    // 强制走统一封装的 request 实例，这样会自动拥有无感刷新 Token 和统一拦截拦截的能力
    const res = await request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    // 成功后手动触发组件内部的 success 钩子，它会自动接着调用我们下面定义的 handleAvatarSuccess 等方法
    // 注意：这里的 res 已经是经过 request.js 拦截器剥离出来的 data 层了
    onSuccess(res, file)
  } catch (error) {
    // 失败触发 error 钩子
    onError(error)
  }
}

// --- Computed 筛选 ---
const filteredProducts = computed(() => {
  if (!productKeyword.value) return myProducts.value
  return myProducts.value.filter(item => item.title?.includes(productKeyword.value))
})
const filteredBuyOrders = computed(() => {
  if (!buyKeyword.value) return buyOrders.value
  return buyOrders.value.filter(item => item.product_title?.includes(buyKeyword.value))
})
const filteredSellOrders = computed(() => {
  if (!sellKeyword.value) return sellOrders.value
  return sellOrders.value.filter(item => item.product_title?.includes(sellKeyword.value))
})

// --- 初始化加载 ---
const loadAllData = async () => {
  fetchUserInfo()
  fetchMyProducts()
  fetchBuyOrders()
  fetchSellOrders()
}
onMounted(() => { loadAllData() })

const fetchUserInfo = async () => { try { const res = await request.get('/user/info'); if(res.data) Object.assign(userInfo, res.data) } catch(e) {} }
const fetchMyProducts = async () => { loadingProduct.value = true; try { const res = await request.get('/product/my-list'); myProducts.value = res.data || [] } finally { loadingProduct.value = false } }
const fetchBuyOrders = async () => { loadingBuy.value = true; try { const res = await request.get('/order/list'); buyOrders.value = res.data || [] } finally { loadingBuy.value = false } }
const fetchSellOrders = async () => { loadingSell.value = true; try { const res = await request.get('/order/sold-list'); sellOrders.value = res.data || [] } catch(e) { sellOrders.value = [] } finally { loadingSell.value = false } }

// --- 动作逻辑 ---
const openEditDialog = () => { editProfileVisible.value = true }
const updateProfile = async () => { loadingSave.value = true; try { await request.post('/user/update', userInfo); ElMessage.success('保存成功'); editProfileVisible.value = false } finally { loadingSave.value = false } }
const showPublishDialog = () => { resetForm(); publishVisible.value = true }
const editProduct = (row) => { Object.assign(productForm, row); fileList.value = (row.image_url || '').split(',').filter(Boolean).map(url => ({ name: 'img', url })); publishVisible.value = true }
const submitProduct = async () => {
  if (!productForm.title) return ElMessage.warning('请输入标题')
  const urls = fileList.value.map(f => f.response ? f.response.data : f.url).filter(Boolean)
  productForm.image_url = urls.join(',')
  await request.post('/product/save', productForm)
  ElMessage.success('提交成功'); publishVisible.value = false; fetchMyProducts()
}

const handleStatusChange = (item, newStatus) => {
  const actionName = newStatus === 2 ? '下架' : '上架'
  ElMessageBox.confirm(newStatus === 2 ? '确定要下架吗？' : '确定上架？', '提示', { type: 'warning' }).then(async () => {
    await request.post('/product/save', { ...item, status: newStatus })
    ElMessage.success(`${actionName}成功`)
    fetchMyProducts()
  }).catch(() => {})
}

const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除吗？', '警告', { type: 'error' }).then(async () => {
    await request.post(`/product/delete/${item.id}`)
    ElMessage.success('已删除')
    fetchMyProducts()
  }).catch(() => {})
}

const handlePay = (row) => { 
  const orderId = row.id; 
  ElMessageBox.confirm('确认支付？', '提示', { type:'success' }).then(async() => { 
    await request.post(`/order/pay/${orderId}`); 
    ElMessage.success('支付成功'); 
    refreshOrders() 
  }) 
}

const cancelOrder = (row) => { 
  const orderId = row.id;
  ElMessageBox.confirm('确认取消订单？', '提示', { type:'warning' }).then(async() => { 
    await request.post(`/order/cancel/${orderId}`); 
    ElMessage.success('已取消'); 
    refreshOrders() 
  }) 
}

const confirmReceive = (row) => { 
  const orderId = row.id;
  ElMessageBox.confirm('确认收货？', '提示', { type:'success' }).then(async() => { 
    await request.post(`/order/confirm/${orderId}`); 
    ElMessage.success('交易完成'); 
    refreshOrders() 
  }) 
}

const shipOrder = (row) => { 
  const orderId = row.id;
  ElMessageBox.confirm('确认发货？', '提示', { type:'primary' }).then(async() => { 
    await request.post(`/order/ship/${orderId}`); 
    ElMessage.success('发货成功'); 
    refreshOrders() 
  }) 
}

const handleOrderDetail = async (row) => {
  detailVisible.value = true
  currentOrder.value = { ...row, description: '' }
  detailPreviewList.value = (row.image_url || '').split(',').filter(Boolean)
  
  loadingDesc.value = true
  const orderId = row.id;
  
  try {
     const res = await request.get(`/order/detail/${orderId}`)
     if(res.data) {
        Object.assign(currentOrder.value, res.data)
        if (res.data.image_url) {
           detailPreviewList.value = res.data.image_url.split(',').filter(Boolean)
        }
     }
     
     const pid = row.product_id || currentOrder.value.product_id
     if (pid) {
        const pRes = await request.get(`/product/detail/${pid}`)
        if (pRes.data) {
           currentOrder.value.description = pRes.data.description
        }
     }
  } catch(e){
    console.error(e)
  } finally {
    loadingDesc.value = false
  }
}

const refreshOrders = () => { fetchBuyOrders(); fetchSellOrders(); fetchMyProducts() }
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
        await request.post('/user/logout') 
    } catch(e) {}
    
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_info')
    
    router.replace('/login')
    ElMessage.success('已安全退出')
  })
}
const resetForm = () => { Object.assign(productForm, { id: null, title: '', description: '', price: 0, original_price: 0, image_url: '', category_id: 1 }); fileList.value = [] }
const getMainImage = (url) => url ? url.split(',')[0] : ''
const getOrderStatusType = (s) => { if(s===0) return 'danger'; if(s===1) return 'warning'; if(s===2) return 'primary'; if(s===3) return 'success'; return 'info' }
const formatOrderStatus = (s) => ['待付款','待发货','待收货','已完成','已取消'][s] || '未知'
const formatStatus = (s) => ['出售中','交易中','已下架','交易完成'][s] || '未知'

const handleAvatarSuccess = async (res) => { 
  avatarLoading.value = false 
  if(res.code === 200) { 
    userInfo.avatar = res.data 
    
    try {
      await request.post('/user/update', { 
        ...userInfo,
        avatar: res.data 
      })
      ElMessage.success('头像更换成功')
    } catch (e) {
      ElMessage.warning('头像上传成功但保存失败，请手动保存')
    }
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const beforeUpload = (file) => { 
  const isImg = ['image/jpeg','image/png'].includes(file.type); 
  const isLt2M = file.size / 1024 / 1024 < 10; 
  
  if(!isImg || !isLt2M) {
    ElMessage.error('请上传10MB以内的JPG/PNG/JPEG/WEBP格式图片')
    return false
  }
  
  avatarLoading.value = true 
  return true
}

const handleUploadError = () => {
  avatarLoading.value = false
  ElMessage.error('网络异常，上传失败')
}

const handleExceed = () => ElMessage.warning('最多5张图片')
const handleUploadSuccess = (res, file) => { if(res.code!==200) { ElMessage.error(res.msg); const idx=fileList.value.indexOf(file); if(idx>-1) fileList.value.splice(idx,1) } }
</script>

<style scoped>
.user-center-page { min-height: 100vh; background-color: #f0f2f5; padding-bottom: 40px; }
.profile-header { position: relative; background: #fff; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.cover-banner { height: 180px; background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); }
.user-info-bar { max-width: 1200px; margin: 0 auto; position: relative; padding: 0 20px 20px; display: flex; align-items: flex-end; justify-content: space-between; }
.info-content { display: flex; align-items: flex-end; }
.avatar-wrapper { margin-top: -50px; margin-right: 20px; position: relative; z-index: 2; }
.avatar-box { width: 110px; height: 110px; border-radius: 50%; border: 4px solid #fff; background: #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.1); overflow: hidden; position: relative; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.real-avatar { width: 100%; height: 100%; object-fit: cover; }
.default-avatar { font-size: 50px; color: #cbd5e1; }
.upload-mask { position: absolute; top:0; left:0; width:100%; height:100%; background: rgba(0,0,0,0.4); color: #fff; font-size: 24px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; }
.avatar-box:hover .upload-mask { opacity: 1; }
.text-info { margin-bottom: 5px; }
.name-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.nickname { font-size: 24px; font-weight: bold; color: #333; }
.role-tag { font-weight: normal; }
.detail-row { color: #666; font-size: 14px; display: flex; align-items: center; gap: 10px; }
.detail-item { display: flex; align-items: center; gap: 4px; }
.action-group { margin-bottom: 5px; }
.main-container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
.content-tabs { min-height: 500px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.02); }
.custom-tab-label { font-size: 16px; display: flex; align-items: center; gap: 5px; }
.sub-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #f0f0f0; }
.filter-bar { display: flex; justify-content: flex-end; margin-bottom: 15px; }
.product-cell { display: flex; align-items: center; gap: 12px; }
.cell-img { width: 50px; height: 50px; border-radius: 4px; flex-shrink: 0; background: #f9f9f9; border: 1px solid #eee;}
.cell-text { display: flex; flex-direction: column; }
.p-title { font-size: 14px; color: #333; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px;}
.p-time { font-size: 12px; color: #999; margin-top: 2px; }
.user-info-col { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #555; }
.u-item { display: flex; align-items: center; gap: 4px; }
.u-item.addr { font-size: 12px; color: #888; margin-top: 2px; line-height: 1.3; }
.price-text { color: #f56c6c; font-weight: bold; }
.status-tip { color: #aaa; font-size: 12px; }
.inventory-header { display: flex; justify-content: space-between; margin-bottom: 20px; align-items: center;}
.grid-container { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.grid-item { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; transition: transform 0.2s, box-shadow 0.2s; }
.grid-item:hover { transform: translateY(-3px); box-shadow: 0 8px 16px rgba(0,0,0,0.08); }
.img-box { height: 150px; position: relative; background: #f8f8f8; }
.p-img { width: 100%; height: 100%; }
.status-overlay { position: absolute; top: 8px; right: 8px; background: rgba(0,0,0,0.6); color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.info-box { padding: 12px; }
.g-title { font-size: 15px; color: #333; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.g-desc { font-size: 12px; color: #999; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; height: 18px; }
.g-price { color: #ff5000; font-size: 18px; font-weight: bold; margin-bottom: 10px; }
.g-actions { display: flex; justify-content: flex-end; gap: 8px; align-items: center; }
.detail-box { padding: 0 10px; }
.d-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 15px; border-bottom: 1px dashed #eee; margin-bottom: 15px; }
.d-status { font-size: 22px; font-weight: bold; color: #409EFF; }
.d-id { color: #666; font-size: 14px; }
.d-product { display: flex; gap: 15px; background: #f8f9fa; padding: 12px; border-radius: 6px; margin-bottom: 20px; }
.d-img { width: 80px; height: 80px; border-radius: 4px; border: 1px solid #eee; cursor: zoom-in; }
.d-info { display: flex; flex-direction: column; justify-content: center; }
.d-title { font-weight: 600; font-size: 15px; margin-bottom: 6px; }
.d-price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.d-desc-section { background: #fff; margin-bottom: 15px; }
.desc-label { font-size: 14px; font-weight: bold; color: #333; margin-bottom: 6px; }
.desc-text { font-size: 13px; color: #666; line-height: 1.5; background: #fafafa; padding: 10px; border-radius: 4px; white-space: pre-wrap;}
.d-divider { height: 10px; border-bottom: 1px dashed #eee; margin-bottom: 10px; }
.d-cell-group { background: #fff; }
.d-cell { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.d-cell:last-child { border-bottom: none; }
.d-cell .label { color: #909399; }
.d-cell .value { color: #303133; max-width: 70%; text-align: right; }
</style>