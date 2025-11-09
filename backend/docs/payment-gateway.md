# 支付网关集成规范

本文档描述系统支付网关的统一接口与虚拟网关接入方式，便于扩展与联调。

## 网关接口

- 统一接口：`PaymentGateway`，包含 `createPayment(PaymentRequest)`, `queryPayment(String paymentNo)`, `refund(RefundRequest)`, `handleCallback(String rawPayload)`。
- 工厂注册：所有实现通过 `PaymentGatewayFactory` 的构造器注入后自动注册，`getGateway(method)` 获取具体实现。

## 虚拟网关（virtual）

- 组件：`VirtualPaymentGateway`，使用 `VirtualPaymentSDK` 模拟支付、查询与退款。
- 支持渠道：`qr`、`app`、`h5`。`qr` 返回 `qrCode`；`app/h5` 返回 `payUrl`。
- 支付状态：创建为 `pending`，查询后转为 `paid` 并生成 `transactionId`。
- 退款：返回 `success` 并生成 `refundNo`。

## 回调

- 控制器端点：`POST /api/payment/callback/{method}`（如：`virtual`、`wechat`、`alipay`）。
- 处理逻辑：`PaymentController` 收集请求参数并调用 `paymentService.handlePaymentCallback(method, params)`，成功返回 `ok`，失败返回 `fail`。

## 验签

- 组件：`CallbackSignatureVerifier`（统一验签）。
- 集成位置：`PaymentServiceImpl.verifyPaymentCallback` 优先使用统一验签，失败则拒绝回调。
- WeChat（微信）：对除 `sign` 外的参数进行字典序拼接，末尾追加 `&key={payment.wechat.api-key}`，取 `MD5` 大写与 `sign` 比较。
- Alipay（支付宝）：对除 `sign`、`sign_type` 外的参数进行字典序拼接，按 `sign_type`（`RSA2`/`RSA`）使用支付宝公钥 `payment.alipay.alipay-public-key` 验签（Base64签名）。
- Virtual（虚拟网关）：开发/测试用途，验签始终通过。

示例（微信）：

```
appid=wx_test&mch_id=10001&nonce_str=ABC&out_trade_no=ORDER123&total_fee=100&key=TEST_KEY
MD5(大写) -> 与 sign 比较
```

示例（支付宝）：

```
字典序拼接且对值进行 URL 编码后使用 RSA2 验签（Base64）
```

## 请求校验

- 校验层：`PaymentDataValidator`，对订单号、金额、支付方式、渠道与信用卡安全载荷进行校验。
- 使用位置：各 `Gateway.createPayment` 在调用 SDK 前进行校验。

## 监控

- 组件：`PaymentMonitor`，记录 `create`、`query`、`refund` 与 `failure` 计数。
- 集成位置：`PaymentServiceImpl` 在创建、查询、退款与异常路径打点。

## 测试与模拟数据

- 单元测试：`VirtualPaymentGatewayTest` 覆盖创建/查询/退款/回调。
- 服务测试：`PaymentServiceImplVirtualGatewayTest` 通过模拟持久层验证流程。
- 验签测试：`CallbackSignatureVerifierTest` 覆盖微信 MD5、支付宝 RSA2 与虚拟网关验签。
- 模拟数据：`src/test/resources/mock/virtual-transaction.json`，包含脱敏订单样例。

## 扩展建议

- 新增网关时遵循 `PaymentGateway` 接口，并在 `PaymentGatewayFactory` 构造器中注入实现。
- 回调端点命名建议：`/callback/{method}`，方法名与 `PaymentGateway.getPaymentMethod()` 保持一致。