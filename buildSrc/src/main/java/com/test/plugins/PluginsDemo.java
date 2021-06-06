package com.test.plugins;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryPlugin;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PluginsDemo implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        boolean isAndroid = project.getPlugins().hasPlugin(AppPlugin.class) || project.getPlugins().hasPlugin(LibraryPlugin.class);
        if (!isAndroid) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.");
        }
        System.out.println("isAndroid===" + isAndroid);
        BaseExtension baseExtension = (BaseExtension) project.getExtensions().findByName("android");
        baseExtension.registerTransform(new TransformDemo(baseExtension));
    }
}
