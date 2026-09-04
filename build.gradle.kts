// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val resolveIdeRuntimeClasspathCopyLocks = tasks.register("resolveIdeRuntimeClasspathCopyLocks") {
    group = "build setup"
    description = "Resolves Android Studio runtime classpath copies when refreshing lock state."
}

tasks.whenTaskAdded {
    if (name.contains("ArtProfile")) {
        enabled = false
    }
}

tasks.register("clientRewriteContractTest") {
    group = "verification"
    description = "Runs the complete compatibility gate for a from-scratch client rewrite."
    dependsOn(":app:testDebugUnitTest")
}
