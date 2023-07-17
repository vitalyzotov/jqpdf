package ru.vzotov.gradle.plugins.cmake;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public class CMakeExtension {
    private final Property<String> binary;
    private final Property<String> runtimeBinary;
    private final DirectoryProperty includeDirectory;
    private final DirectoryProperty projectDirectory;
    private final ListProperty<String> arguments;
    private final ListProperty<String> buildArguments;

    @Inject
    public CMakeExtension(ProjectLayout projectLayout, ObjectFactory objectFactory) {
        binary = objectFactory.property(String.class);
        runtimeBinary = objectFactory.property(String.class);
        includeDirectory = objectFactory.directoryProperty();
        projectDirectory = objectFactory.directoryProperty();
        projectDirectory.set(projectLayout.getProjectDirectory());
        includeDirectory.set(projectDirectory.dir("include"));
        arguments = objectFactory.listProperty(String.class);
        buildArguments = objectFactory.listProperty(String.class);
    }

    public Property<String> getRuntimeBinary() {
        return runtimeBinary;
    }

    public final Property<String> getBinary() {
        return binary;
    }

    public final DirectoryProperty getIncludeDirectory() {
        return includeDirectory;
    }

    public final DirectoryProperty getProjectDirectory() {
        return projectDirectory;
    }

    public ListProperty<String> getArguments() {
        return arguments;
    }

    public ListProperty<String> getBuildArguments() {
        return buildArguments;
    }
}
