-- AI智能客服模块数据库表
-- 创建日期: 2025-12-12
-- 版本: 1.0.0

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` varchar(64) NOT NULL COMMENT '会话ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID（游客为null）',
    `role` varchar(20) NOT NULL COMMENT '消息角色：user-用户, assistant-AI助手, system-系统',
    `content` text NOT NULL COMMENT '消息内容',
    `intent` varchar(50) DEFAULT NULL COMMENT '意图分类',
    `confidence` decimal(3,2) DEFAULT NULL COMMENT '置信度分数（0-1）',
    `resolved` tinyint DEFAULT '0' COMMENT '是否已解决：0-未解决, 1-已解决',
    `rating` tinyint DEFAULT NULL COMMENT '用户满意度评分（1-5）',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服聊天消息表';

-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识ID',
    `category` varchar(50) NOT NULL COMMENT '问题分类',
    `keywords` varchar(500) DEFAULT NULL COMMENT '问题关键词（逗号分隔）',
    `question` varchar(500) NOT NULL COMMENT '问题内容',
    `answer` text NOT NULL COMMENT '标准答案',
    `priority` int DEFAULT '0' COMMENT '匹配优先级',
    `hit_count` int DEFAULT '0' COMMENT '匹配次数统计',
    `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用, 1-启用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    FULLTEXT KEY `idx_keywords` (`keywords`, `question`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服知识库表';

-- 插入初始知识库数据
INSERT INTO `knowledge_base` (`category`, `keywords`, `question`, `answer`, `priority`, `status`) VALUES
-- 服务相关
('service', '洗车,服务,项目,类型', '你们有哪些洗车服务？', '我们提供多种专业洗车服务：\n\n🚗 **基础洗车** - ¥38\n全车外部清洗，轮毂清洁\n\n🚙 **精致洗车** - ¥68\n内外全面清洗，仪表台护理\n\n✨ **深度养护** - ¥128\n深度清洁+漆面养护\n\n🌟 **豪华套餐** - ¥198\n全套养护+内饰消毒\n\n您可以根据需求选择合适的服务~', 100, 1),
('service', '打蜡,抛光,美容,漆面', '有打蜡抛光服务吗？', '当然有！我们的漆面美容服务包括：\n\n✨ **漆面打蜡** - ¥98\n巴西棕榈蜡，持久保护\n\n💎 **漆面抛光** - ¥168\n去除轻微划痕，恢复光泽\n\n🛡️ **镀晶服务** - ¥688\n9H硬度保护，持久1年\n\n建议每2-3个月做一次打蜡养护哦~', 90, 1),

-- 预约相关
('booking', '预约,预订,怎么约,如何', '怎么预约洗车？', '预约洗车非常简单！只需3步：\n\n**第1步：选择服务** 🚗\n在服务列表中选择您需要的洗车项目\n\n**第2步：选择时间** 📅\n选择方便的日期和时间段\n\n**第3步：确认预约** ✅\n填写车辆信息，完成预约\n\n预约成功后会收到确认通知，按时到店即可！', 100, 1),
('booking', '取消,退订,不想去', '可以取消预约吗？', '可以的！取消预约请注意：\n\n⏰ **提前2小时** - 免费取消\n📍 **2小时内** - 收取10%手续费\n🚫 **未到店** - 收取30%费用\n\n取消方式：\n1. 在"我的订单"中点击取消\n2. 联系客服帮您取消\n\n建议提前规划好时间哦~', 90, 1),
('booking', '改约,修改,换时间', '能修改预约时间吗？', '可以修改！在订单状态为"待确认"时：\n\n1. 进入"我的订单"\n2. 找到对应订单\n3. 点击"修改预约"\n4. 选择新的时间段\n\n注意：已确认的订单需先取消再重新预约。\n如有特殊情况，可联系客服协助处理~', 85, 1),

-- 支付相关
('payment', '支付,付款,微信,支付宝,怎么付', '支持哪些支付方式？', '我们支持多种支付方式：\n\n💳 **在线支付**\n• 支付宝支付\n• 微信支付\n• 银行卡支付\n\n💰 **其他方式**\n• 到店现金支付\n• 会员余额支付\n\n在线支付即时到账，安全便捷！', 100, 1),
('payment', '退款,退钱,什么时候退', '退款多久到账？', '退款处理时间如下：\n\n⏱️ **审核时间**: 1-2个工作日\n💳 **到账时间**: \n• 支付宝/微信: 即时到账\n• 银行卡: 3-5个工作日\n\n退款会原路返回到您的支付账户。\n如有疑问可联系客服查询~', 90, 1),

-- 账户相关
('account', '会员,积分,优惠', '有会员优惠吗？', '当然有！会员专享福利：\n\n🥉 **普通会员**\n• 消费累积积分\n• 生日专属优惠\n\n🥈 **银卡会员** (消费满500)\n• 全单9.5折\n• 优先预约\n\n🥇 **金卡会员** (消费满2000)\n• 全单9折\n• 免费升级服务\n\n💎 **钻石会员** (消费满5000)\n• 全单8.5折\n• 专属客服\n\n快来注册成为会员吧~', 95, 1),
('account', '注册,账号,怎么注册', '怎么注册账号？', '注册非常简单：\n\n1️⃣ 点击首页的"注册"按钮\n2️⃣ 输入手机号，获取验证码\n3️⃣ 设置密码，完成注册\n\n注册成功后即可享受：\n• 在线预约服务\n• 查看订单记录\n• 累积会员积分\n• 接收优惠通知\n\n赶快注册体验吧~', 90, 1),

-- 通用问题
('general', '营业,时间,几点,开门,关门', '营业时间是什么时候？', '我们的营业时间：\n\n🕐 **工作日**: 8:00 - 22:00\n🕐 **周末/节假日**: 8:00 - 22:00\n\n⏰ **建议预约时间**：\n• 工作日上午 - 人少不排队\n• 避开周末下午高峰期\n\n💡 提前预约可确保到店即可服务！', 100, 1),
('general', '地址,位置,在哪,怎么走', '你们店在哪里？', '我们的门店地址：\n\n📍 **总店**: XX市XX区XX路888号\n📍 **分店**: XX市XX区XX街666号\n\n🚗 **交通指引**：\n• 地铁: X号线XX站B出口\n• 公交: XXX路、XXX路\n• 自驾: 导航"XX洗车"\n\n门店均有充足停车位哦~', 95, 1),
('general', '电话,联系,客服', '客服电话是多少？', '您可以通过以下方式联系我们：\n\n📞 **客服热线**: 400-888-8888\n📱 **微信客服**: carwash_service\n📧 **邮箱**: service@carwash.com\n\n🕐 **服务时间**: 8:00-22:00\n\n有任何问题随时联系我们~', 100, 1);
