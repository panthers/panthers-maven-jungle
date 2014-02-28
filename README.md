panthers-maven-jungle
=====================

### Example
```xml
<build>
  <plugins>
    <plugin>
			<groupId>com.panther.maven.plugins</groupId>
			<artifactId>dependency-maven-plugin</artifactId>
			<version>1.0.1-SNAPSHOT</version>
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

#License
* [GPL-2.0] (http://opensource.org/licenses/GPL-2.0)
