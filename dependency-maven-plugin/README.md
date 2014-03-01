dependency-maven-plugin [![Build Status](https://travis-ci.org/jaysaikia/panthers-maven-jungle.png?branch=master)](https://travis-ci.org/jaysaikia/panthers-maven-jungle)
=====================

This project was specifically created for the purpose of adding artifacts to the project based on range and qualifier.
It is based on the maven dependency plugin.
You can copy multiple versions of the same artifact by providing a range of versions to download from in the configuration.
Please feel free to use the plugin as you see fit.

### Configuration
1. Currently only copy is possible.
2. Version Range works as standard maven version range. [Details](http://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#CJHDEHAB)
3. Output file name is artifactId-version.type

### Examples

#### command line (in progress)
Add command line plugin call

#### pom.xml
```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.panther.maven.plugins</groupId>
			<artifactId>dependency-maven-plugin</artifactId>
			<version>0.1-SNAPSHOT</version>
			<executions>
				<execution>
					<id>multi-artifact-copy</id>
					<phase>process-resources</phase>
					<goals>
						<goal>copy-artifacts-with-range</goal>
					</goals>
					<configuration>
						<outputDirectory>${project.build.directory}/multi-artifacts</outputDirectory>
						<artifactItems>
							<artifactItem>
								<groupId>org.apache.maven</groupId>
								<artifactId>maven-core</artifactId>
								<versionRange>[3.0,)</versionRange>
								<type>jar</type>
								<includeSnapshots>false</includeSnapshots>
								<includeLatestSnapshot>true</includeLatestSnapshot>
							</artifactItem>
						</artifactItems>
					</configuration>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

#### License
* [Apache-2.0] (http://opensource.org/licenses/Apache-2.0)
