# tmsdk
## 天幕安卓SDK
## 说明
1. 用户ID，默认使用系统生成，用户卸载再安装，既视为一个新用户，可以以使用自己的ID。
## 使用方法
gradle添加依赖
```
implementation 'com.dounine:tmsdk:1.0.0'
```
onCreate方法添加启动
```
//TMSdk.Companion.userId("123"); //是否使用游戏自己后端生成的ID，可选
TMSdk.Companion.init(this, "游戏ID", "渠道ID");
TMSdk.Companion.appStart();
```
onShow方法调用
```
TMSdk.Companion.appShow();
```
onHide方法调用
```
TMSdk.Companion.appHide();
```
混淆设置，如果您的应用使用了代码混淆，请添加如下配置，以避免【天幕SDK】被错误混淆导致SDK不可用。
```
-keep class com.dounine.tmsdk.** {*;}
```
