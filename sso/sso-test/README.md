### 测试流程
1. 前提条件: sso-core和sso-test公用同一个redis，同时两者中的user类路径相同
2. 启动sso登录中心`com.fhb.sso.core.SSOApplication`
3. 修改sso-test中的`pplication.yml`, 通过修改端口`server.port`启动多个程序模拟多个子系统
4. 在任一子系统登录，查看能否访问其他子系统
5. 在任一子系统登出，查看是否同步登出
