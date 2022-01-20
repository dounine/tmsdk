# 发布
### 自动打包
[使用github action打包并发布到nexus仓库](https://gitbook.dounine.com/bi-ji/gradle-github-action-fa-bu-dao-maven-cang-ku)

### 手动打包
cat ~/.gradle/gradle.properties
```
NEXUS_EMAIL=amwoqmgo@gmail.com
NEXUS_PASSWORD=password
NEXUS_USERNAME=username
signing.keyId=7BDDF87A
signing.password=password
signing.secretKeyRingFile=/Users/lake/.gnupg/secring.gpg
```
打包并发布到中央仓库
```
gradle publish 
```
登录[nexus](https://oss.sonatype.org/#nexus-search;quick~dounine)操作发布即可