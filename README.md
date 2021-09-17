# 独立工程组

这个主要是整理的一些老的项目的源码，对他们重新编译和release，提供新版本的jdk使用，也有一些我的
比较零碎的功能，不好归类在其他Project里面的那种。

## 现有项目

com.ice.jni.registry：用于操作windows注册表的API，现在是org.swdc.extern.ice。

org.kxml2 ：xml的api，包含xmlpull和kxml2两部分。

org.eclipse: Eclipse的Wrapper，把所有常用的Eclipse SWT组件整合在一起，这里分别是32位和64位。

org.swdc.libloader: 本地类库加载器，根据xml依次加载dll，这里面有NativeXMLDemo展示了如何编写这种xml。

## 使用方法

clone后对需要的模块maven install进入本地maven即可。