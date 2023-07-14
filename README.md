# Shitboy：丝芭应援群机器人插件

Mirai-Console插件，构建mirai（[mamoe/mirai](https://github.com/mamoe/mirai), [docs.mirai.mamoe.net](https://docs.mirai.mamoe.net/)）后拖入plugins文件夹运行即可，首次运行生成配置，全部内容可在QQ中使用指令自助完成。

## Add-on

本插件尽可能简化功能，如您需要更多功能，推荐您在**安装本插件的同时**安装下列Add-ons

- [Lawaxi/ShitBoyJuJuAddon](https://github.com/Lawaxi/ShitBoyJuJuAddon)
  - 在所有房间监测某些聚聚的发言
  - 注：本插件需要将Shitboy配置中pocket48/save_login设置为false
- [Lawaxi/ShitBoyWeidianAddon](https://github.com/Lawaxi/ShitBoyWeidianAddon)
  - 微店订单抽卡
  - 微店PK

## 使用说明

在群内或私聊机器人输入`“/帮助”“/help”“/?”`均可获得指令表

### （0）通用功能

1. 欢迎新成员：群管理私聊机器人输入`“/欢迎 <群id> xxx”`可开启欢迎，可多行

### （1）口袋48

1. 关注：
    - 群管理在群内直接输入`“/口袋 关注 room_id”`
    - 同上`“/口袋 取消关注 room_id”`
    - 任意群成员输入`“/口袋 关注列表”`
    - room_id可以通过查询指令获取或从机器人外其他渠道获取
    - 关注加密房间需要额外输入指令`“/口袋 连接 room_id server_id”`（server_id通过查询指令获取）（无需密码）
2. 查询：
    - 口袋48官方设定为
        - 每个账号（成员&聚聚）有一个id（或star_id）
        - 在团成员/袋王账号id下属一个server_id，队伍房单独有server_id
        - server_id就是口袋48软件里小偶像菜单的本质，可以下属多个room_id（或channel_id）
    - 任意群成员输入`“/口袋 搜索 小偶像名”`可以自动查询id、server_id、room_id
    - 同上`“/口袋 查询 id”`可根据id自动查询server_id、room_id
    - 同上`“/口袋 查询2 server_id”`可根据server_id查下属room_id

#### （2）B站

1. 博主关注
   - 群管理在群内直接输入`“/bili 关注 uid”`
   - 同上`“/bili 取消关注 uid”`
   - 任意群成员输入`“/bili 关注列表”`
2. 直播关注
    - 群管理在群内直接输入`“/bililive 关注 room_id”`
    - 同上`“/bililive 取消关注 room_id”`
    - 任意群成员输入`“/bililive 关注列表”`
    - room_id为直播间链接最后的数字，是直播间单独，区别于主站uid

### （3）微博

1. 博主关注
    - 群管理在群内直接输入`“/微博 关注 uid”`
    - 同上`“/微博 取消关注 uid”`
    - 任意群成员输入`“/微博 关注列表”`
1. 超话关注
    - 群管理在群内直接输入`“/超话 关注 超话id”`
    - 同上`“/超话 取消关注 超话id”`
    - 任意群成员输入`“/超话 关注列表”`

### （3）微店

1. 提交Cookie
    - 一切的开始
    - 群管理私聊机器人输入`“/微店 <群id> cookie xxx”`，xxx为以`__spider__`开头的一串
    - 同上`“/微店 <群id> 关闭”`可删除提交的cookie并关闭微店功能
    - 不知道如何获取cookie的饭头可以参考http://www.lgyzero.top/weidianCookie
    - cookie基本上是微店全权，勿轻易外泄
2. 普链/特殊链
    - 为区别【商品播报】（进度&排行），商品分为普链、特殊链两种
        - 普链【商品播报】仅在有人购买时附在【订单播报】底部播报
        - 特殊链【商品播报】实时播报，并且不会附在【订单播报】底部
    - 商品默认为普链，群管理私聊机器人输入`“/微店 <群id> # 商品id”`可切换
    - 商品id可通过“全部商品”查询功能获得
3. 功能
    - 自动发货：群管理私聊机器人输入`“/微店 <群id> 自动发货”`可切换
    - 全部商品：`“/微店 <群id> 全部”`
    - 查单个商品进度、贡献排行：`“/微店 <群id> 查 商品id”`

### （4）配置文件 ./config/net.lawaxi.shitboy/config.setting

~~~python
[]
enable = true #开启插件
admins = #安全的人，QQ号以逗号隔开，在任意群内无论是否为管理员都可使用管理员指令
secureGroup = #安全的群，以逗号隔开，在这些群内任何成员可使用管理员指令
[pocket48]
account = #口袋手机号
password = #口袋密码
#需要输入口袋账密后重启才可使用口袋机器人功能
schedule = * * * * * #监测时间间隔，具体搜索Cron定时任务表达式
~~~

## 更新日志

### 0.1.0

1. 口袋48房间播报：除文字消息外均提示“不支持的消息”

### 0.1.1 (内测更新)

1. 口袋48房间播报更新：提示全部消息
2. 易拉罐群特殊代码目前仅出现在util/Listener中，删掉即可正常使用

### 0.1.2

1. Bilibili直播提醒功能

### 0.1.3

1. 增加了群成员查询&关注&取关的功能<br>鉴于胡乱设置可能被群友群起而攻之，暂未添加权限功能
2. 常规消息合并发送
3. 易拉罐群特殊代码都在util/ListenerYLG中，删掉或用其他插件禁用即可正常使用
4. test6-新增查直播&查录播功能
5. test14-关注未解锁的加密房间

### 0.1.5

1. 群聊从没有关注到有关注后无需重启
2. 微博超话播报
3. test3-微博博主播报
4. test6-权限设计
5. test8-微博解析更新，在有管理员权限的群使用@全体成员功能
6. test9-对房间语音的初步支持
7. test10-对房间语音的全面支持
8. test10-搜索 & 查询2

### 0.1.6

1. 微店播报
2. 监测时间间隔修复

### 0.1.7

1. BUG修复
2. test2-欢迎功能
3. test4-Schedule Pattern可自定义
4. test8-清理功能 & 全面优化
5. test10-微店功能更新
6. test15-save_login
7. test16-getUserNickName
9. test19-微店BUG修复 & 调增
10. test20-WeidianBuyer使用int储存金额

### 0.1.8

1. B站主站关注

### 0.1.9

1. 微店订单播报显示买家排名（主插件重置微店播报功能，如使用WeidianAddon也需更新0.1.1以上版本）
2. test4-B站主站关注BUG修复
3. test5-未登录机器人时微店正常自动发货
4. test6-微店排名标明是否为并列

### 0.1.10

1. api更新（Addon请使用Maven: <scope>provided</scope>, Gradle: compile/api形式引用）