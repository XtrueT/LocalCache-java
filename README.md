## 概述

编写一个Java本地缓存类，以及核心方法的单元测试。

## 时间限制

3天

## Must

1. 实现基本缓存操作 get, set, remove。
2. 读写操作保证线程安全。
3. 支持设置缓存过期时间, TTL或者TTI。
3. maven 打包编译 (https://maven.apache.org/)。
4. junit单元测试 (https://junit.org/)。
5. 核心方法变量添加 javadoc。
6. 项目上传到github, 最终提交github公开仓库的链接。


## Better
- [ ] 读写拷贝
- [ ] 持久化
- [x] 内存回收策略(LRU, FIFO)

## Result

1. 实现基本缓存操作 get, set, remove。
2. 读写操作保证线程安全。
3. 支持设置缓存过期时间, TTL或者TTI。
4. maven 打包编译 (https://maven.apache.org/)。
5. junit单元测试 (https://junit.org/)。
6. 核心方法变量添加 javadoc。
7. 项目上传到github, 最终提交github公开仓库的链接。
8. 内存回收策略 FIFO