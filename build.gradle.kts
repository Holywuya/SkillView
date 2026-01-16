import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.BukkitHook
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.CommandHelper
import io.izzel.taboolib.gradle.JavaScript
import io.izzel.taboolib.gradle.Kether


plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

taboolib {
    env {
        install(Basic)
        install(BukkitUI)
        install(BukkitHook)
        install(Bukkit)
        install(BukkitUtil)
        install(DatabasePlayer)
        install(BukkitNMSItemTag)
    }
    description {
        name("SkillView")
        contributors {
            name("Esters")
        }
    }
    version { taboolib = "6.2.4-abd325ee" }
    relocate("ink.ptms.um", "com.skillview.um")
    relocate("top.maplex.arim", "com.skillview.arim")
}

repositories {
    maven {
        url = uri("https://nexus.maplex.top/repository/maven-public/")
        isAllowInsecureProtocol = true
    }
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    taboo("ink.ptms:um:1.2.1")
    taboo("top.maplex.arim:Arim:1.3.12")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}