## todolist
1. redis vb token过期时间被更改了(3*60 -> 1*30) 记得改回去
2. satoken和业务逻辑的redis分离实现
3. listPage遇到的check问题 sa-token-alone
4. 目前只有account有role查询 实际上每个包都要有 可能需要rpc调用 目前遇到循环引用问题
5. 后端权限管理的返回值需要更友好
6. 也许需要更改一下DepartmentId的逻辑 有Id不能获取到DepartmentName

## fixlist
1. 前端部分接口需要改
2. login需要的参数为LoginRO 逻辑不合理 需要拆分相关逻辑？
3. 前端需要有Department的查询

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
- operation-step 外存保存 记录每一步骤 reload即依次执行每一步

## buglist
1. redis过期时没有request，无法获取ip，需要单独抽象connection保存到redis中