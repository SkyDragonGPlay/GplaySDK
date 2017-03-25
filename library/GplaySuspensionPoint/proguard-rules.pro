# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zhangjunfei/work/android-sdk/tools/proguard/proguard-android.txt
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


-dontshrink
-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-ignorewarnings
-repackageclasses com.skydragon.gplay.suspension
-keepattributes InnerClasses,Signature,*Annotation*
-dontpreverify
-verbose
-dontwarn

##########################################################################
## Android System libraries that don't need to be obfuscated

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

-keep public class * extends android.app.Activity

-keep public class * extends android.app.Application

-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class android.support.v4.app.FragmentManagerMaker

-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class android.content.**{ *;}
-keep class android.os.**{ *;}

-keep public class com.skydragon.gplay.sprite.GplaySpritePoint {
    public <fields>;
    public <methods>;
}

-keep public class com.skydragon.gplay.sprite.GplaySpriteButton {
    public <fields>;
    public <methods>;
}

-keep public class com.skydragon.gplay.sprite.LayoutDecelerateAnimation {
    public <fields>;
    public <methods>;
}

-keep public interface com.skydragon.gplay.sprite.LayoutAnimation{*;}
-keep public interface com.skydragon.gplay.sprite.OnGplayButtonAction{*;}
-keep public interface com.skydragon.gplay.sprite.OnGplaySpriteDragonListener{*;}