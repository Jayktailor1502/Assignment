# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep annotations (Room uses reflection on them)
-keepattributes *Annotation*

# Keep Room schema models
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep your Room entities, DAO, and Database
-keep class com.example.smartexpensetracker.data.local.** { *; }

# Kotlin coroutines / Flow
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ViewModel + Lifecycle
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep your ViewModels (instantiated via reflection)
-keep class com.example.smartexpensetracker.ui.vm.** { *; }
