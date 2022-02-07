# files
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <parent>
        <groupId>com.cvent.attendee-login</groupId>
        <artifactId>attendee-login-parent</artifactId>
        <version>${revision}</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>
    <artifactId>attendee-login-integration-test</artifactId>
    <name>attendee-login-integration-test</name>

    <dependencies>
        <!-- all integrations tests should use the java client -->
        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>attendee-login-java-client</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>com.cvent.auth-service</groupId>
            <artifactId>auth-service-java-client</artifactId>
            <version>${auth-service.version}</version>
        </dependency>
        <dependency>
            <groupId>com.cvent.auth-service</groupId>
            <artifactId>auth-service-core</artifactId>
            <version>${auth-service.version}</version>
        </dependency>

        <!-- unit test libs -->
        <dependency>
            <groupId>com.cvent</groupId>
            <artifactId>dropwizard-unit-tests</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.cvent.datatags</groupId>
            <artifactId>datatags-api</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>javax.validation</groupId>
                    <artifactId>validation-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- java 11 dependency -->
        <dependency>
            <groupId>com.cvent.cvent-ee</groupId>
            <artifactId>cvent-ee</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- needed to run karate -->
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
        </dependency>

        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>attendee-login-java-client</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- karate -->
        <dependency>
            <groupId>com.intuit.karate</groupId>
            <artifactId>karate-apache</artifactId>
            <version>0.9.6</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.intuit.karate</groupId>
            <artifactId>karate-jersey</artifactId>
        </dependency>
        <dependency>
            <groupId>com.intuit.karate</groupId>
            <artifactId>karate-junit4</artifactId>
        </dependency>
    </dependencies>

    <build>
        <testResources>
            <testResource>
                <directory>src/test/resources</directory>
            </testResource>
            <testResource>
                <!-- Add feature, JSON payload/fixtures and JS files for Karate -->
                <directory>src/test/java</directory>
                <excludes>
                    <!-- No need to add Java files again, they are already on the classpath -->
                    <exclude>**/*.java</exclude>
                </excludes>
            </testResource>
            <testResource>
                <directory>test_configs</directory>
                <includes>
                    <!-- Add the various Karate config files to the classpath -->
                    <include>karate*.js</include>
                </includes>
            </testResource>
        </testResources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <!-- Temporarily downgrade the version of this plugin until run-it issues are figured out -->
                <version>3.0.0-M5</version>
                <configuration>
                    <useModulePath>false</useModulePath>
                    <skipTests>${skipIntegrationTests}</skipTests>
                    <systemPropertyVariables>
                        <karate.env>${env.IT_ENVIRONMENT}</karate.env>
                        <it_environment>${env.IT_ENVIRONMENT}</it_environment>
                        <karate.tags>${customTags}</karate.tags>
                    </systemPropertyVariables>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>


--------------------------------------------------------------------------------------------------------------
package com.cvent.attendeelogin;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.RunnerOptions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Runs all features in this directory and below
 */
public class AttendeeLoginKarateTestIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttendeeLoginKarateTestIT.class);

    @Test
    public void testParallel() {
        // IMPORTANT - reports must be written to "target/failsafe-reports/cucumber" directory for dropwizardPipeline
        // to find and publish them in Jenkins.
        Runner.Builder runner = new Runner.Builder()
                .path(RunnerOptions.fromAnnotationAndSystemProperties(null, null, getClass()).getFeatures())
                .tags(getKarateTagList())
                .reportDir("target/failsafe-reports/cucumber");
        Results results = runner.parallel(1);
        Assert.assertEquals(results.getErrorMessages(), 0, results.getFailCount());
    }

    private List<String> getKarateTagList() {
        List<String> karateTagList = new ArrayList<>();
        // Adding a default "~ignore" tag to exclude utilities files from being pulled by the runner
        karateTagList.add("~@ignore");
        String suppliedTags = System.getProperty("karate.tags");
        if (StringUtils.isNotBlank(suppliedTags)) {
            karateTagList.add(suppliedTags);
        }
        LOGGER.info(String.format("Karate tags: \"%s\"", String.join(", ", karateTagList)));
        return karateTagList;
    }

}
