plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.14.0"

}

group = "net.lawaxi"
version = "0.1.7-test18"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    implementation ("cn.hutool:hutool-all:5.8.18")
    implementation ("com.belerweb:pinyin4j:2.5.0")
}