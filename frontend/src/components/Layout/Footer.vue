<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <!-- 公司信息 -->
        <div class="footer-section">
          <div class="footer-brand">
            <div class="brand-icon">
              <el-icon size="24"><House /></el-icon>
            </div>
            <span class="brand-name">洗车预约系统</span>
          </div>
          <p class="footer-desc">
            专业的汽车洗车服务预约平台，为您提供便捷、高效的洗车服务体验。
          </p>
          <div class="social-links">
            <a href="#" class="social-link">
              <el-icon><ChatDotRound /></el-icon>
            </a>
            <a href="#" class="social-link">
              <el-icon><Message /></el-icon>
            </a>
            <a href="#" class="social-link">
              <el-icon><Phone /></el-icon>
            </a>
          </div>
        </div>
        
        <!-- 快速链接 -->
        <div class="footer-section">
          <h4 class="section-title">快速链接</h4>
          <ul class="footer-links">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/services">服务项目</router-link></li>
            <li><router-link to="/appointment">立即预约</router-link></li>
            <li><router-link to="/about">关于我们</router-link></li>
          </ul>
        </div>
        
        <!-- 服务支持 -->
        <div class="footer-section">
          <h4 class="section-title">服务支持</h4>
          <ul class="footer-links">
            <li><a href="#">帮助中心</a></li>
            <li><a href="#">服务条款</a></li>
            <li><a href="#">隐私政策</a></li>
            <li><a href="#">联系客服</a></li>
          </ul>
        </div>
        
        <!-- 联系信息 -->
        <div class="footer-section">
          <h4 class="section-title">联系我们</h4>
          <div class="contact-info">
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>400-888-8888</span>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>service@carwash.com</span>
            </div>
            <div class="contact-item">
              <el-icon><Location /></el-icon>
              <span>北京市朝阳区xxx街道xxx号</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 版权信息 -->
      <div class="footer-bottom">
        <div class="copyright">
          <p>&copy; 2025 汽车洗车服务预约系统. All rights reserved.</p>
          <p>毕业设计项目 - 基于SpringBoot的汽车洗车服务预约系统</p>
        </div>
        <div class="footer-meta">
          <span>ICP备案号：京ICP备xxxxxxxx号</span>
          <span>|</span>
          <span>版本 v1.0.0</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style scoped>
.app-footer {
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section {
  display: flex;
  flex-direction: column;
}

/* 品牌区域 */
.footer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--primary-gradient);
  border-radius: var(--radius-md);
  color: var(--text-white);
}

.brand-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
}

.footer-desc {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
  font-size: 14px;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--bg-light);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.social-link:hover {
  background: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-2px);
}

/* 链接区域 */
.section-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 8px;
}

.footer-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--primary-color);
}

/* 联系信息 */
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.contact-item .el-icon {
  color: var(--primary-color);
  flex-shrink: 0;
}

/* 底部版权 */
.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.copyright p {
  margin: 0;
  color: var(--text-light);
  font-size: 12px;
  line-height: 1.5;
}

.footer-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-light);
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .footer-bottom {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .footer-meta {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .footer-container {
    padding: 30px 16px 16px;
  }
  
  .footer-content {
    gap: 24px;
  }
  
  .social-links {
    justify-content: center;
  }
}
</style>