plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "cn.org.expect"
version = "1.0.0"

// 顶层结构
repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/nexus/content/repositories/central/")
    mavenCentral()
    maven("https://plugins.jetbrains.com/maven")
}

dependencies {
    implementation("cn.org.expect:modest-script-engine:1.0.0")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20210307")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 顶层结构
tasks.jar.configure {
    archiveBaseName.set("modest-maven-idea-plugin") // 设置 artifactId
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//    version.set("2024.2.0.2")
    version.set("2023.2.6") // 开发测试插件的 IntelliJ IDEA 版本
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("maven")) // 依赖的 IntelliJ IDEA 内置插件
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }

    patchPluginXml {
        sinceBuild.set("232") // 2023.3.8 开始
        untilBuild.set("") // 向上兼容所有版本
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
