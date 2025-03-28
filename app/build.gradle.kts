import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"
    id("com.google.gms.google-services")
}

sonar {
    properties {
        property("sonar.projectKey", "Software-Engineering-II-Gruppe2_WebSocketBroker-App")
        property("sonar.organization", "software-engineering-ii-gruppe2")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val mainSrc = "$projectDir/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))

    classDirectories.setFrom(
        fileTree("build/tmp/kotlin-classes/debug") {
            include("at/aau/serg/websocketbrokerdemo/**/*.class")
            exclude(
                "**/databinding/**",
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/ui/theme/**"
            )
        }
    )

    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }
    )
}

dependencies {
    // Firebase
    implementation(libs.firebase.auth.ktx)
    implementation(platform(libs.firebase.bom))

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services)

    // WebSockets
    implementation(libs.krossbow.websocket.okhttp)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.builtin)

    // AndroidX Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.navigation.testing)
}
