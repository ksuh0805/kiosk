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


-keep class android.**
-keep public class * { public protected *; }

-dontwarn android.support.**
-dontwarn com.google.ads.**

-keep public class android.support.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.appwidget.AppWidgetProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.support.v7.**


# -keep public class com.android.vending.licensing.ILicensingService
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


-keep class android.support.v7.widget.** { *; }



#
-keep class android.support.**
-keep class android.**
-dontwarn android.**
-keep class com.samsung.** { *;}

-keep public class com.samilcts.mpos.demo.** { public protected *; }



##############################################

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# rxjava
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#############
-keep public class com.woosim.** {  *; }
-keep public class com.bixolon.** { *; }
-dontwarn net.sf.andpdf.**
-dontwarn com.sun.pdfview.**
-dontwarn org.bouncycastle.crypto.engines.**

-keepattributes Exceptions, Signature, InnerClasses


# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
# Guava 19.0
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn com.google.auto.common.**
# Guava 23.5
-dontwarn afu.**
-dontwarn org.checkerframework.**
-dontwarn javax.**
-dontwarn com.google.**


# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-keepattributes InnerClasses,EnclosingMethod