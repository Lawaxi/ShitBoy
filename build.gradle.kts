plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.15.0-short-video-message-15"

}

group = "net.lawaxi"
version = "0.1.11-dev4"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://repo.mirai.mamoe.net/snapshots")
    mavenCentral()
}

dependencies {
    api ("cn.hutool:hutool-all:5.8.18")
    api ("com.belerweb:pinyin4j:2.5.0")
}