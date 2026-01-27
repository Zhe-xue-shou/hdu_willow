## todolist
1. 完善api文档 明白每个模块的功能
2. vboard部分的数据库
3. token逻辑拆分
4. 测试模块
5. session和token？

## fixlist
1. 登录接口是/auth/login 前端写的是/user/account/login
2. 登录时要先调用/generate-verification-code生成验证码
3. login需要的参数为LoginRO 逻辑不合理 需要拆分相关逻辑？

## note
- hdu-account 负责单独信息的增删改查 主要包括Department(班级) Role(权限) user(用户)
- hdu-account-api 未知
- hdu-auth 鉴权 登录
- hdu-common 公共模块
- hdu-data-transform 新老平台迁移
- hdu-fpga 实体板卡实验模块
- hdu-interrupt 熔断平台？功能未知
- hdu-record 记录平台 负责实验记录？
- hdu-vboard 虚拟板卡实验模块 目前没有接入数据库
- 登录逻辑：username+"-"+departmentId作为整个门户平台登录的loginId 子系统也通过*作为loginId
  也就是说 主系统和子系统的登录逻辑是一样的 只是显式地区分了登录的语句