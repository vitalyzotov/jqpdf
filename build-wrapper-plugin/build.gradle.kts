plugins {
    `java-gradle-plugin`
}

group = "ru.vzotov.gradle"
version = "1.0"

gradlePlugin {
    (plugins) {
        register("cmakeLibrary") {
            id = "ru.vzotov.gradle.cmake-library"
            implementationClass = "ru.vzotov.gradle.plugins.cmake.CMakeLibraryPlugin"
        }
        register("wrappedBase") {
            id = "ru.vzotov.gradle.wrapped-native-base"
            implementationClass = "ru.vzotov.gradle.plugins.WrappedNativeBasePlugin"
        }
        register("wrappedLibrary") {
            id = "ru.vzotov.gradle.wrapped-native-library"
            implementationClass = "ru.vzotov.gradle.plugins.WrappedNativeLibraryPlugin"
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Werror", "-Xlint:deprecation"))
}
