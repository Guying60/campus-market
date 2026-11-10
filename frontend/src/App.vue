<template>
  <RouterView />
  <div class="global-watermark">Guying60</div>
</template>


<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
/* 简单去除浏览器的默认边距 */
body { margin: 0; padding: 0; background-color: #f5f7fa;}

/* 水印样式 */
.global-watermark {
  position: fixed;
  bottom: 15px; /* 距离底部的高度 */
  right: 20px;  /* 放在右下角，避免遮挡页面中间的分页器或按钮 */
  z-index: 9999; /* 确保层级最高，显示在最上层 */
  
  font-size: 16px;
  font-weight: bold;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  letter-spacing: 1px;
  
  /* 视觉效果：半透明灰色，类似水印 */
  color: rgba(0, 0, 0, 0.15); 
  text-shadow: 1px 1px 0 rgba(255, 255, 255, 0.5); /* 轻微白边，增加立体感 */
  
  pointer-events: none; /* 关键属性：让鼠标穿透水印，不影响下方按钮点击 */
  user-select: none;    /* 禁止选中文字 */
}
</style>