# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\joosung\AppData\Local\Android\android-sdk/tools/proguard/proguard-android.txt
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


# Android stuff


#

-keep public class ** { public protected *; }

-keep public class com.samilcts.printer.android.** { public protected *; }

-keep public class com.woosim.** {  *; }
-keep public class com.bixolon.** { *; }

-keepclassmembers class com.woosim.**  {
    <fields>;
    *;
}
-keepclassmembers class com.bixolon.**  {
    <fields>;
    *;
}

-dontwarn net.sf.andpdf.**
-dontwarn com.sun.pdfview.**
-dontwarn org.bouncycastle.crypto.engines.**


-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    *;
}

-keepattributes InnerClasses
-keep public enum com.samilcts.printer.android.Printer$** { *; }


