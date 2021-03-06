/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package com.wws.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification

/**
 * A simple unit test for the 'com.wws.plugin.greeting' plugin.
 */
public class ComWwsPluginPluginTest extends Specification {
    def "plugin registers task"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply("com.wws.plugin.greeting")

        then:
        project.tasks.findByName("greeting") != null
    }
}
