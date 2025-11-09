// 检查页面状态和认证信息的脚本
(function() {
    console.log('🔍 开始检查页面状态...');
    
    // 检查localStorage中的token
    const token = localStorage.getItem('token');
    const tokenType = localStorage.getItem('tokenType');
    
    console.log('📱 Token状态:');
    console.log('  - Token存在:', !!token);
    console.log('  - Token类型:', tokenType);
    if (token) {
        console.log('  - Token前20字符:', token.substring(0, 20) + '...');
    }
    
    // 检查当前页面URL
    console.log('🌐 当前页面信息:');
    console.log('  - URL:', window.location.href);
    console.log('  - 路径:', window.location.pathname);
    console.log('  - 查询参数:', window.location.search);
    
    // 检查Vue应用状态
    console.log('⚡ Vue应用状态:');
    console.log('  - Vue实例存在:', !!window.__VUE__);
    
    // 检查页面元素
    const ordersContainer = document.querySelector('.orders-container');
    const loadingElement = document.querySelector('.loading');
    const errorElement = document.querySelector('.error');
    
    console.log('📋 页面元素状态:');
    console.log('  - 订单容器存在:', !!ordersContainer);
    console.log('  - 加载元素存在:', !!loadingElement);
    console.log('  - 错误元素存在:', !!errorElement);
    
    // 尝试手动触发API调用测试
    if (token) {
        console.log('🔄 尝试手动测试API调用...');
        fetch('http://localhost:8080/api/bookings/user/2', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            console.log('📡 API响应状态:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('📊 API响应数据:', data);
            if (data.success) {
                console.log('✅ API调用成功，订单数量:', data.data.length);
            } else {
                console.log('❌ API调用失败:', data.message);
            }
        })
        .catch(error => {
            console.error('💥 API调用错误:', error);
        });
    } else {
        console.log('⚠️ 没有token，无法测试API调用');
    }
    
    // 检查网络请求
    console.log('🌐 监听网络请求...');
    const originalFetch = window.fetch;
    window.fetch = function(...args) {
        console.log('📡 发起网络请求:', args[0]);
        return originalFetch.apply(this, args)
            .then(response => {
                console.log('📡 网络请求响应:', args[0], '状态:', response.status);
                return response;
            })
            .catch(error => {
                console.error('💥 网络请求错误:', args[0], error);
                throw error;
            });
    };
    
    console.log('✅ 状态检查完成');
})();