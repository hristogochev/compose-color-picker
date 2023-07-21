@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
}

android {
    namespace = "io.mhssn.colorpicker"
    this.compileSdkVersion = "android-33"

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    defaultConfig {
        aarMetadata {
            minCompileSdk = 33
        }
    }
}


//val sourcesJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("sources")
//    from(sourceSets["main"].allSource)
//}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.hristogochev"
            artifactId = "compose-color-picker"
            version = "1.0.0"

            artifact(javadocJar)
//            artifact(sourcesJar)

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}


dependencies {
    implementation("androidx.compose.runtime:runtime:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.material:material:1.4.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.core:core-ktx:1.10.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}