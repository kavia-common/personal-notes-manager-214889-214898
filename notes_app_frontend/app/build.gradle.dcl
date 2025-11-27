androidApplication {
    namespace = "org.example.app"

    // Compose enablement and versions now centrally defined in defaults in settings.gradle.dcl

    dependencies {
        // Compose BOM ensures aligned versions across Compose artifacts
        implementation(platform("androidx.compose:compose-bom:2024.10.01"))

        // Core Compose UI and Material3
        implementation("androidx.activity:activity-compose:1.9.3")
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.ui:ui-tooling-preview")
        debugImplementation("androidx.compose.ui:ui-tooling")

        // Foundation & runtime utilities used (e.g., LazyColumn, state)
        implementation("androidx.compose.foundation:foundation")
        implementation("androidx.compose.runtime:runtime")

        // Icons for the app's Icon usage
        implementation("androidx.compose.material:material-icons-extended")

        // Lifecycle ViewModel Compose (not strictly required right now but commonly used in Compose apps)
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

        // AndroidX core for KTX extensions
        implementation("androidx.core:core-ktx:1.13.1")
        // Appcompat is optional for Compose but keep if themes/resources reference it; safe to include
        implementation("androidx.appcompat:appcompat:1.7.0")
    }
}
