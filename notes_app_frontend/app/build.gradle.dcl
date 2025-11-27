androidApplication {
    namespace = "org.example.app"

    compose {
        enable = true
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    dependencies {
        // Compose BOM and core
        implementation(platform("androidx.compose:compose-bom:2024.10.01"))
        implementation("androidx.activity:activity-compose:1.9.3")
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.ui:ui-tooling-preview")

        // Optional icons
        implementation("androidx.compose.material:material-icons-extended")

        // AndroidX core and appcompat for theme compatibility
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
    }
}
