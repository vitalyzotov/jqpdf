package ru.vzotov.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.stream.Collectors;

/**
 * Task types to execute CMake
 */
public class CMake extends DefaultTask {
    private String buildType;
    private final DirectoryProperty variantDirectory = getProject().getObjects().directoryProperty();
    private final DirectoryProperty projectDirectory = getProject().getObjects().directoryProperty();
    private final ConfigurableFileCollection includeDirs = getProject().files();
    private final ConfigurableFileCollection linkFiles = getProject().files();
    private final ListProperty<String> arguments = getProject().getObjects().listProperty(String.class);

    @TaskAction
    public void generateCmakeFiles() {
        String cmakeExecutable = System.getenv().getOrDefault("CMAKE_EXECUTABLE", "cmake");

        variantDirectory.get().getAsFile().mkdirs();
        getProject().exec(execSpec -> {
            execSpec.setWorkingDir(getVariantDirectory());
            execSpec.executable(cmakeExecutable);
            execSpec.args("-DCMAKE_BUILD_TYPE=" + capitalize(getBuildType()));
            if(!getIncludeDirs().isEmpty()) {
                execSpec.args("-DINCLUDE_DIRS=" + getIncludeDirs().getFiles().stream().map(File::getAbsolutePath).collect(Collectors.joining(";  ")));
            }
            if(!getLinkFiles().isEmpty()) {
                execSpec.args("-DLINK_DIRS=" + getLinkFiles().getFiles().stream().map(File::getParent).collect(Collectors.joining(";")));
            }
            execSpec.args("--no-warn-unused-cli");
            execSpec.args(getArguments().get());
            execSpec.args(getProjectDirectory().get().getAsFile().getAbsolutePath());
        });
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @InputFiles
    public FileCollection getCMakeLists() {
        return getProject().fileTree(projectDirectory, it -> it.include("**/CMakeLists.txt"));
    }

    @OutputFiles
    public FileCollection getCmakeFiles() {
        return getProject().fileTree(variantDirectory, it -> it.include("**/CMakeFiles/**/*").include("**/Makefile").include("**/*.cmake"));
    }

    @Input
    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    @Internal
    public DirectoryProperty getVariantDirectory() {
        return variantDirectory;
    }

    @Internal
    public DirectoryProperty getProjectDirectory() {
        return projectDirectory;
    }

    @InputFiles
    public ConfigurableFileCollection getIncludeDirs() {
        return includeDirs;
    }

    @InputFiles
    public ConfigurableFileCollection getLinkFiles() {
        return linkFiles;
    }

    @Input
    public ListProperty<String> getArguments() {
        return arguments;
    }

}
