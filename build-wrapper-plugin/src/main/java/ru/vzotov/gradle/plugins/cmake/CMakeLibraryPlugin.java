package ru.vzotov.gradle.plugins.cmake;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import ru.vzotov.gradle.tasks.CMake;
import ru.vzotov.gradle.tasks.CMakeBuild;
import ru.vzotov.gradle.tasks.Make;

/**
 * A sample plugin that wraps a CMake build with Gradle to take care of dependency management.
 */
public class CMakeLibraryPlugin implements Plugin<Project> {
    public void apply(final Project project) {
        project.getPluginManager().apply("ru.vzotov.gradle.wrapped-native-library");

        // Add a CMake extension to the Gradle model
        final CMakeExtension extension = project.getExtensions().create("cmake", CMakeExtension.class, project.getLayout(), project.getObjects());

        /*
         * Create some tasks to drive the CMake build
         */
        TaskContainer tasks = project.getTasks();

        final TaskProvider<CMake> cmakeDebug = tasks.register("cmakeDebug", CMake.class, task -> {
            task.setBuildType("Debug");
            task.getIncludeDirs().from(project.getConfigurations().getByName("cppCompile"));
            task.getLinkFiles().from(project.getConfigurations().getByName("cppLinkDebug"));
            task.getVariantDirectory().set(project.getLayout().getBuildDirectory().dir("debug"));
            task.getProjectDirectory().set(extension.getProjectDirectory());
            task.getArguments().set(extension.getArguments());
        });

        final TaskProvider<CMake> cmakeRelease = tasks.register("cmakeRelease", CMake.class, task -> {
            task.setBuildType("RelWithDebInfo");
            task.getIncludeDirs().from(project.getConfigurations().getByName("cppCompile"));
            task.getLinkFiles().from(project.getConfigurations().getByName("cppLinkRelease"));
            task.getVariantDirectory().set(project.getLayout().getBuildDirectory().dir("release"));
            task.getProjectDirectory().set(extension.getProjectDirectory());
            task.getArguments().set(extension.getArguments());
        });

        final TaskProvider<CMakeBuild> assembleDebug = tasks.register("assembleDebug", CMakeBuild.class, task -> {
            task.setGroup("Build");
            task.setDescription("Builds the debug binaries");
            task.generatedBy(cmakeDebug);
            task.binary(extension.getBinary());
            task.runtimeBinary(extension.getRuntimeBinary());
            task.getArguments().set(extension.getBuildArguments());
        });

        TaskProvider<CMakeBuild> assembleRelease = tasks.register("assembleRelease", CMakeBuild.class, task -> {
            task.setGroup("Build");
            task.setDescription("Builds the release binaries");
            task.generatedBy(cmakeRelease);
            task.binary(extension.getBinary());
            task.runtimeBinary(extension.getRuntimeBinary());
            task.getArguments().set(extension.getBuildArguments());
        });

        tasks.named("assemble", task -> task.dependsOn(assembleDebug));

        /*
         * Configure the artifacts which should be exposed by this build
         * to other Gradle projects. (Note that this build does not currently
         * expose any runtime (shared library) artifacts)
         */
        ConfigurationContainer configurations = project.getConfigurations();
        configurations.getByName("headers").getOutgoing().artifact(extension.getIncludeDirectory());
        configurations.getByName("linkDebug").getOutgoing().artifact(assembleDebug.flatMap(CMakeBuild::getBinary));
        configurations.getByName("linkRelease").getOutgoing().artifact(assembleRelease.flatMap(CMakeBuild::getBinary));
        configurations.getByName("runtimeDebug").getOutgoing().artifact(assembleDebug.flatMap(CMakeBuild::getRuntimeBinary));
        configurations.getByName("runtimeRelease").getOutgoing().artifact(assembleDebug.flatMap(CMakeBuild::getRuntimeBinary));
    }
}
