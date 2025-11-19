# Конфигурация ProGuard для нашего приложения

# Сохраняем классы, которые используются в манифесте
-keep class com.example.funny_cats.** { *; }

# Сохраняем классы с аннотациями
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Hilt
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @javax.inject.Inject class * { *; }

# Retrofit
-keep class com.example.funny_cats.data.api.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptekch.glide.module.AppGlideModule {
    *;
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Kotlin
-keep class kotlin.Metadata { *; }

# WorkManager
-keep class androidx.work.impl.model.WorkSpec { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Сохраняем ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater);
}

# Не обфусцируем имена методов, используемых в XML
-keepclassmembers class * {
    public void on*(**);
}

# Сохраняем классы, которые могут использоваться через рефлексию
-keep class com.google.gson.** { *; }