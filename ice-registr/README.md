# ICERegistry

这个是一个java操作注册表的jni类库，由于太过久远，找不到dll和源码，我对他进行了重新编译
目前这将会是一个支持jdk11的类库。

## 如何使用

请clone此仓库并install到本地，然后通过maven使用它。

或者使用maven：
在pom添加此节点。
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
然后添加此依赖项：
```xml
<dependency>
    <groupId>com.github.SW-Fantastic</groupId>
	<artifactId>standalone</artifactId>
	<version>v0.1</version>
</dependency>
```