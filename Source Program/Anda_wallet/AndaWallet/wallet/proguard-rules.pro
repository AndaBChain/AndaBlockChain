# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in <Android-SDK>/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}

-dontwarn com.googlecode.jsonrpc4j.**
-keep class com.googlecode.jsonrpc4j.** { *; }

-dontwarn com.squareup.javapoet.**
-keep class com.squareup.javapoet.** { *; }

-dontwarn !rehanced.com.**,**
-keep class !rehanced.com.** { *; }

-dontoptimize
-dontobfuscate
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn sun.misc.Unsafe
-dontwarn sun.misc.Cleaner
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn sun.nio.ch.DirectBuffer
-dontwarn net.jcip.annotations.GuardedBy
-dontwarn com.subgraph.orchid.**


# Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# 不做预校验
-dontpreverify

### 忽略警告
-ignorewarning

-keepattributes EnclosingMethod

 #如果有其它包有warning，在报出warning的包加入下面类似的-dontwarn 包名
-dontwarn com.fengmap.*.**

## 注解支持
-keepclassmembers class *{
   void *(android.view.View);
}

############====================androidannotations====================##############
-dontwarn org.androidannotations.**
-keep class org.androidannotations.** {*;}
##处理注释属性
#-keepattributes *Annotation*
#
-dontwarn org.springframework.**