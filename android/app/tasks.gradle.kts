@file:Suppress("DSL_SCOPE_VIOLATION")

tasks.register("assembleAndInstall") {
    group = "build"
    description = "Build and install the debug APK"
    dependsOn("installDebug")
}

tasks.register("quickRun") {
    group = "run"
    description = "Quick build and run on connected device"
    dependsOn("installDebug")
    doLast {
        println("App installed. Launching...")
        exec {
            commandLine("adb", "shell", "am", "start", "-n", "com.helix.wifimic/.MainActivity")
        }
    }
}
