package com.panther.maven.plugins.fromConfiguration;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

/**
 * Information for artifacts to download. Range is mandatory. 
 * @author panther
 *
 */
public class ArtifactItemsWithRange extends ArtifactItem {
	
	/**
     * Version Range of Artifact
     */
	@Parameter(required = true, alias = "versionRange")
	private String versionRange;

	/**
	 * Get all snapshots of a particular version of this artifact.
	 */
	@Parameter(defaultValue = "false")
	private Boolean includeSnapshots = false;
	
	/**
	 * Include only the snapshots where release is not available.
	 */
	@Parameter(defaultValue = "false")
	private Boolean includeLatestSnapshot = false;

	public String getVersionRange() {
		return versionRange;
	}

	public void setVersionRange(String versionRange) {
		this.versionRange = versionRange;
	}

	public Boolean getIncludeSnapshots() {
		return includeSnapshots;
	}

	public void setIncludeSnapshots(Boolean includeSnapshots) {
		this.includeSnapshots = includeSnapshots;
	}

	public Boolean getIncludeLatestSnapshot() {
		return includeLatestSnapshot;
	}

	public void setIncludeLatestSnapshot(Boolean includeLatestSnapshot) {
		this.includeLatestSnapshot = includeLatestSnapshot;
	}

	public ArtifactItemsWithRange() {
		super();
	}
	
	public ArtifactItemsWithRange(Artifact artifact) throws MojoFailureException {
		super(artifact);
		try {
			artifact.setVersionRange(VersionRange.createFromVersionSpec(versionRange));
		} catch (InvalidVersionSpecificationException e) {
			throw new MojoFailureException("Please provide a valid version range." + " check VersionRange JDoc createFromVersionSpec");
		}
	}
	
	@Override
	public String toString() {
		if ( this.getClassifier() == null ) {
            return this.getGroupId() + ":" + this.getArtifactId() + ":" + StringUtils.defaultString( versionRange, "?" ) + ":" + this.getType();
        } else {
            return this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getClassifier() + ":" + StringUtils.defaultString( versionRange, "?" ) + ":" + this.getType();
        }
	}
}
