plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.pulsmesser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pulsmesser"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // include libraries for Javadoc
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.0.2")

    // extends MQTT-Client functionalities
    implementation("androidx.legacy:legacy-support-v4:1.0.0");
    implementation("com.github.hannesa2:paho.mqtt.android:3.3.5")


}