# Keep kotlinx.serialization DTOs for GitHub OTA & JSON Backup Import/Export
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.punhanaghayev.bimmerpro.**$$serializer { *; }
-keepclassmembers class com.punhanaghayev.bimmerpro.** {
    *** Companion;
}
-keepclasseswithmembers class com.punhanaghayev.bimmerpro.** {
    kotlinx.serialization.KSerializer serializer(...);
}
