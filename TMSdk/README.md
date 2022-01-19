# 发布
cat ~/.gradle/gradle.properties
```
NEXUS_EMAIL=amwoqmgo201314@gmail.com
NEXUS_PASSWORD=password
NEXUS_USERNAME=username
signing.keyId=7BDDF87A
signing.password=password
signing.secretKeyRingFile=/Users/lake/.gnupg/secring.gpg
```
打包并发布到中央仓库
```
gradle clean build publish 
```