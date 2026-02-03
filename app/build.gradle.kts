plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ideacarry.opengl30jni"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ideacarry.opengl30jni"
        minSdk = 27
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                abiFilters("arm64-v8a", "x86_64")
                arguments("-DANDROID_STL=c++_shared", "-DANDROID_ARM_NEON=TRUE")
                cppFlags("")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.31.6"
        }
    }
    buildFeatures {
        viewBinding = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    ndkVersion = "27.3.13750724"
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
    // 排除没使用的so
    packaging {
        resources {
            excludes += "lib/x86/**"
            excludes += "lib/armeabi-v7a/**"
        }
    }
}

val nativesConfig = "natives"
configurations.create(nativesConfig)
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    implementation(libs.coroutines)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.livedata)
    implementation(libs.lifecycle.extensions)
    implementation(libs.viewmodel)
    implementation(libs.lifecycle.common)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // gdx
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.gdx.backend.android)
    val gdxVersion = libs.versions.gdxVersion.get()
    // Box2D natives
    add(nativesConfig, "com.badlogicgames.gdx:gdx-box2d-platform:${gdxVersion}:natives-arm64-v8a")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-box2d-platform:${gdxVersion}:natives-armeabi-v7a")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-box2d-platform:${gdxVersion}:natives-x86")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-box2d-platform:${gdxVersion}:natives-x86_64")
    // FreeType natives
    add(nativesConfig, "com.badlogicgames.gdx:gdx-freetype-platform:${gdxVersion}:natives-arm64-v8a")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-freetype-platform:${gdxVersion}:natives-armeabi-v7a")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-freetype-platform:${gdxVersion}:natives-x86")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-freetype-platform:${gdxVersion}:natives-x86_64")
    // Core gdx natives
    add(nativesConfig, "com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-arm64-v8a")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-armeabi-v7a")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-x86")
    add(nativesConfig, "com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-x86_64")

    api(libs.kotlin.stdlib)
    api(libs.kotlinx.coroutines.core)

    // --- LibGDX ---
    api(libs.gdx.core)
    api(libs.gdx.box2d)
    api(libs.gdx.freetype)
    api(libs.gdx.ai)

    // --- ECS ---
    api(libs.ashley)

    // --- Utils ---
    api(libs.libgdx.utils)
    api(libs.vis.ui)

    // --- KTX ---
    api(libs.ktx.actors)
    api(libs.ktx.ai)
    api(libs.ktx.app)

    api(libs.ktx.artemis)
    api(libs.ktx.ashley)

    api(libs.ktx.assets)
    api(libs.ktx.assets.async)
    api(libs.ktx.async)

    api(libs.ktx.box2d)
    api(libs.ktx.collections)

    api(libs.ktx.freetype)
    api(libs.ktx.freetype.async)

    api(libs.ktx.graphics)
    api(libs.ktx.i18n)

    api(libs.ktx.inject)
    api(libs.ktx.json)
    api(libs.ktx.log)

    api(libs.ktx.math)
    api(libs.ktx.preferences)
    api(libs.ktx.reflect)

    api(libs.ktx.scene2d)
    api(libs.ktx.style)
    api(libs.ktx.tiled)

    api(libs.ktx.vis)
    api(libs.ktx.vis.style)

    // --- Artemis ---
    api(libs.artemis.odb)
}

// Called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
tasks.register("copyAndroidNatives") {
    doFirst {
        val abiDirs = listOf(
            "armeabi-v7a",
            "arm64-v8a",
            "x86_64",
            "x86"
        )

        // 创建目录
        abiDirs.forEach { abi ->
            file("libs/$abi").mkdirs()
        }

        configurations[nativesConfig].copy().files.forEach { jar ->

            val outputDir = when {
                jar.name.endsWith("natives-armeabi-v7a.jar") -> file("libs/armeabi-v7a")
                jar.name.endsWith("natives-arm64-v8a.jar") -> file("libs/arm64-v8a")
                jar.name.endsWith("natives-x86_64.jar") -> file("libs/x86_64")
                jar.name.endsWith("natives-x86.jar") -> file("libs/x86")
                else -> null
            }

            outputDir?.let {
                project.copy {
                    from(zipTree(jar))
                    into(it)
                    include("*.so")
                }
            }
        }
    }
}

tasks.matching { it.name.contains("merge") && it.name.contains("JniLibFolders") }.configureEach {
    dependsOn(tasks.named("copyAndroidNatives"))
}