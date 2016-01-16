# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*

-dontwarn sun.misc.Unsafe

# WIFI FINGERPRINTING
-keep class dk.alexandra.** { *; }
-keep class joptsimple.* { *; }

# SUPERRECYCLEVIEW
-dontwarn com.malinskiy.superrecyclerview.SwipeDismissRecyclerViewTouchListener*

# PARSE
-keep class com.parse.* { *; }
-dontwarn com.parse.**

-keep class com.facebook.** { *; }
-dontwarn com.parse.ui.**

# GOOGLE GMS
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Dagger
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class dagger.* { *; }
-keep class javax.lang.** { *; }
-keep class javax.inject.* { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection

# EVENTBUS
-keepclassmembers class ** {
    public void onEvent*(**);
}

# BUTTERKNIFE
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# PICASSO
-dontwarn com.squareup.javawriter.**
 -dontwarn com.squareup.okhttp.**

# FONTAWESOME
-keep class .R
-keep class **.R$* {
    <fields>;
}