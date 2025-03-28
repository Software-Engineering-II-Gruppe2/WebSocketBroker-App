import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.sonarqube") version "5.1.0.4882"
    id("com.google.gms.google-services")
    id("jacoco")
}

sonar {
    properties {
        property("sonar.projectKey", "Software-Engineering-II-Gruppe2_WebSocketBroker-App")
        property("sonar.organization", "software-engineering-ii-gruppe2")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*", "android/**/*.*"
    )

    val debugTree = fileTree(layout.buildDirectory.get().dir("intermediates/javac/debug")) {
        exclude(fileFilter)
    }
    val kotlinDebugTree = fileTree(layout.buildDirectory.get().dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }
    val executionDataFile = layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec").get().asFile

    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    executionData.setFrom(executionDataFile)
}


    dependencies {
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.krossbow.websocket.okhttp)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.builtin)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.espresso.core)

    // Debugging & UI Testing
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)

    // Unit Testing
    testImplementation(libs.mockk)
    testImplementation(libs.junit)

    // Android Instrumentation Tests
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.ui.test.junit4.v140)
    androidTestImplementation(libs.androidx.ui.test.manifest.v140)
    androidTestImplementation(libs.material3)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android.v451)
    androidTestImplementation(libs.androidx.navigation.testing)
}
