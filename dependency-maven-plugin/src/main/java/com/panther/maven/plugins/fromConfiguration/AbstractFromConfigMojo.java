package com.panther.maven.plugins.fromConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;

import com.panther.maven.plugins.AbstractDependencyMojo;

/**
 * Panther Configuration Abstraction
 * Reproduced from org.apache.maven.plugin.dependency.fromConfiguration.AbstractFromConfigurationMojo 2.8
 * @author panther
 *
 */
public abstract class AbstractFromConfigMojo extends AbstractDependencyMojo {
	
	/**
     * Default output location used for mojo, unless overridden in ArtifactItem.
     */
    @Parameter( property = "outputDirectory", defaultValue = "${project.build.directory}/dependency" )
    private File outputDirectory;
    
    /**
     * Collection of ArtifactItems to work on. (ArtifactItem contains groupId, artifactId, version, versionRange, type, classifier,
     * outputDirectory, destFileName and overWrite.) See <a href="./usage.html">Usage</a> for details.
     */
    @Parameter
    private List<ArtifactItemsWithRange> artifactItemsWithRange;

    /**
     * To look up ArtifactRepository implementation
     */
    @Component
    private ArtifactRepositoryFactory artifactRepositoryManager;
    
	protected List<ArtifactItem> getProcessRangedArtifactItems(ArtifactItemsWithRange artifactItemsWithRange) throws MojoExecutionException {
		getLog().debug(logArtifactWithRange(artifactItemsWithRange));
		List<DefaultArtifactVersion> avilableVersions = getAvailableVersions(artifactItemsWithRange);
		List<ArtifactItem> artifactItems = new ArrayList<ArtifactItem>();
		for (DefaultArtifactVersion av : avilableVersions) {
			getLog().debug("Found version : " + av.toString());
			Artifact artifactWithVersion;
			if (StringUtils.isEmpty(artifactItemsWithRange.getClassifier())) {
				artifactWithVersion = factory.createDependencyArtifact( artifactItemsWithRange.getGroupId(), artifactItemsWithRange.getArtifactId(), VersionRange.createFromVersion(av.toString()),
	            		artifactItemsWithRange.getType(), null, Artifact.SCOPE_COMPILE );
	        } else {
	        	artifactWithVersion = factory.createDependencyArtifact( artifactItemsWithRange.getGroupId(), artifactItemsWithRange.getArtifactId(), VersionRange.createFromVersion(av.toString()),
	            		artifactItemsWithRange.getType(), artifactItemsWithRange.getClassifier(),
	                                                         Artifact.SCOPE_COMPILE );
	        }
			artifactWithVersion = resolveArtifact(artifactWithVersion);
			ArtifactItem artifactItem = new ArtifactItem(artifactWithVersion);
			
			//Destination name is artifactId-version.type
			artifactItem.setDestFileName(artifactItem.getArtifactId()+"-"+artifactItem.getVersion()+"."+artifactItem.getType());
			
			//First preference goes to artifactItem output directory
			if(artifactItemsWithRange.getOutputDirectory() == null) {
				artifactItem.setOutputDirectory(outputDirectory);
			} else {
				artifactItem.setOutputDirectory(artifactItemsWithRange.getOutputDirectory());
			}
			artifactItem.getOutputDirectory().mkdir();
			artifactItem.setOverWrite(artifactItemsWithRange.getOverWrite());
			artifactItem.setNeedsProcessing(true);
			getLog().debug("Adding to artifacts to process : " + artifactItem.toString());
			artifactItems.add(artifactItem);
		}
		return artifactItems;
	}
	
	@SuppressWarnings("unchecked")
	private List<DefaultArtifactVersion> getAvailableVersions(ArtifactItemsWithRange artifactItemsWithRange) throws MojoExecutionException {
		VersionRange artifactVersionRange;
		try {
			artifactVersionRange = VersionRange.createFromVersionSpec(artifactItemsWithRange.getVersionRange());
		} catch (InvalidVersionSpecificationException e) {
			throw new MojoExecutionException("Version range is invalid.");
		}
		Artifact artifact;
		if(StringUtils.isEmpty(artifactItemsWithRange.getClassifier())) {
			artifact = factory.createDependencyArtifact( artifactItemsWithRange.getGroupId(), artifactItemsWithRange.getArtifactId(), artifactVersionRange, artifactItemsWithRange.getType(), artifactItemsWithRange.getClassifier(), Artifact.SCOPE_COMPILE );
		} else {
			artifact = factory.createDependencyArtifact( artifactItemsWithRange.getGroupId(), artifactItemsWithRange.getArtifactId(), artifactVersionRange, artifactItemsWithRange.getType(), null, Artifact.SCOPE_COMPILE );
		}
		List<DefaultArtifactVersion> availableVersions = new ArrayList<DefaultArtifactVersion>();
		try {
			List<DefaultArtifactVersion> allVersions = artifactMetadataSource.retrieveAvailableVersions(artifact, local, remoteRepos);
			if(artifactItemsWithRange.getIncludeSnapshots()) {
				availableVersions.addAll(allVersions);
			} else {
				Collections.sort(allVersions);
				Collections.reverse(allVersions);
				if("SNAPSHOT".equals(allVersions.get(0).getQualifier()) && artifactItemsWithRange.getIncludeLatestSnapshot()) { //If latest is snapshot and include latest exists add this
					availableVersions.add(allVersions.get(0));
				} else if(!"SNAPSHOT".equals(allVersions.get(0).getQualifier())) {
					availableVersions.add(allVersions.get(0));
				}
				for(int i = 1; i < allVersions.size(); i++) { //Get all other artifacts without snapshot
					if(!"SNAPSHOT".equals(allVersions.get(i).getQualifier())) {
						availableVersions.add(allVersions.get(i));
					}
				}
			}
		} catch (ArtifactMetadataRetrievalException e) {
			throw new MojoExecutionException("Could not retrieve available versions");
		}
		
		return availableVersions;
	}

	private StringBuilder logArtifactWithRange(ArtifactItemsWithRange artifactItemsWithRange) {
		StringBuilder sb = new StringBuilder("Finding Versions for : \n" + artifactItemsWithRange.toString());
		return sb;
	}
}
