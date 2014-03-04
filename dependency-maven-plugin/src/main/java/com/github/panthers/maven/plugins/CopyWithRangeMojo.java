 /*
   Copyright 2014 Jayant Pratim Saikia

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.github.panthers.maven.plugins;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.github.panthers.maven.plugins.fromConfig.AbstractFromConfigMojo;
import com.github.panthers.maven.plugins.fromConfig.ArtifactItemsWithRange;

/**
 * Goal which downloads multiple artifacts based on the version given
 */
@Mojo( name = "copy-artifacts-with-range", requiresDependencyResolution = ResolutionScope.TEST, 
defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true )
public class CopyWithRangeMojo extends AbstractFromConfigMojo {
	
	@Parameter
    private List<ArtifactItemsWithRange> artifactItems;
	
	@Override
	protected void doExecute() throws MojoExecutionException, MojoFailureException {
		if( artifactItems == null || artifactItems.isEmpty()) {
			throw new MojoFailureException( "Either artifact or artifactItems is required " );
		}
		processArtifactItemsWithRange();
	}
	
	/**
	 * Main work of copy range plugin starts here
	 * @throws MojoExecutionException
	 */
	private void processArtifactItemsWithRange() throws MojoExecutionException {
		for (ArtifactItemsWithRange artifactItemsWithRange : artifactItems) {
			List<ArtifactItem> listOfArtifacts = getProcessRangedArtifactItems(artifactItemsWithRange);
			for (ArtifactItem artifactItem : listOfArtifacts ) {
	            if (artifactItem.isNeedsProcessing()) {
	                copyArtifact( artifactItem );
	            } else {
	                this.getLog().info( artifactItem + " already exists in " + artifactItem.getOutputDirectory() );
	            }
	        }
		}
	}
	
	/**
     * Resolves the artifact from the repository and copies it to the specified location.
     *
     * @param artifactItem containing the information about the Artifact to copy.
     * @throws MojoExecutionException with a message if an error occurs.
     * @see DependencyUtil#copyFile(File, File, Log)
     * @see DependencyUtil#getFormattedFileName(Artifact, boolean)
     */
    protected void copyArtifact( ArtifactItem artifactItem ) throws MojoExecutionException
    {
        File destFile = new File( artifactItem.getOutputDirectory(), artifactItem.getDestFileName() );

        copyFile( artifactItem.getArtifact().getFile(), destFile );
    }

}
