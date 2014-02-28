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

package com.panther.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Panther Dependency Abstraction 
 * Reproduced from org.apache.maven.plugin.dependency.AbstractDependencyMojo 2.8
 * @author panther
 *
 */
public abstract class AbstractDependencyMojo extends AbstractMojo {
	
	/**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    protected ArtifactFactory factory;
    
    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    protected ArtifactResolver resolver;

    /**
     * Artifact collector, needed to resolve dependencies.
     */
    @Component( role = ArtifactCollector.class )
    protected ArtifactCollector artifactCollector;

    /**
     *
     */
    @Component( role = ArtifactMetadataSource.class, hint = "maven" )
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * Location of the local repository.
     */
    @Parameter( defaultValue = "${localRepository}", readonly = true, required = true )
    protected ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver
     */
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    protected List<ArtifactRepository> remoteRepos;
    
    /**
     * will use the jvm chmod, this is available for user and all level group level will be ignored
     */
    @Parameter( property = "dependency.useJvmChmod", defaultValue = "true" )
    protected boolean useJvmChmod = true;
    
    /**
     * POM
     */
    @Component
    protected MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter( defaultValue = "${reactorProjects}", readonly = true )
    protected List<MavenProject> reactorProjects;

    /**
     * If the plugin should be silent.
     */
    @Parameter( property = "silent", defaultValue = "false" )
    protected boolean silent;
	
	/**
     * Skip plugin execution completely.
     */
    @Parameter( property = "mdep.skip", defaultValue = "false" )
    private boolean skip;

	public final void execute() throws MojoExecutionException, MojoFailureException {
		
		if ( isSkip() )
        {
            getLog().info( "Skipping plugin execution" );
            return;
        }

        doExecute();
	}
	
	protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;
	
	/**
	 * Searches artifact given locally if not found searches on remote repositories.
	 * 
	 * @param artifact
	 * @return resolved artifact
	 * @throws MojoExecutionException
	 */
	protected Artifact resolveArtifact(Artifact artifact) throws MojoExecutionException {
		Artifact result = getArtifactFomReactor( artifact );
        if ( result != null ) {
            return result;
        }
		try {
            resolver.resolve( artifact, remoteRepos, getLocal() );
        } catch ( ArtifactResolutionException e ) {
            throw new MojoExecutionException( "Unable to resolve artifact.", e );
        } catch ( ArtifactNotFoundException e ) {
            throw new MojoExecutionException( "Unable to find artifact.", e );
        }
		return artifact;
	}
	
	/**
     * Checks to see if the specified artifact is available from the reactor.
     *
     * @param artifact The artifact we are looking for.
     * @return The resolved artifact that is the same as the one we were looking for or <code>null</code> if one could
     *         not be found.
     */
    @SuppressWarnings("unchecked")
	private Artifact getArtifactFomReactor( Artifact artifact ) {
        // check project dependencies first off
        for ( Artifact a : (Set<Artifact>) project.getArtifacts() ) {
            if ( equals( artifact, a ) && hasFile( a ) ) {
                return a;
            }
        }

        // check reactor projects
        for ( MavenProject p : reactorProjects == null ? Collections.<MavenProject>emptyList() : reactorProjects ) {
            // check the main artifact
            if ( equals( artifact, p.getArtifact() ) && hasFile( p.getArtifact() ) ) {
                return p.getArtifact();
            }

            // check any side artifacts
            for ( Artifact a : (List<Artifact>) p.getAttachedArtifacts() ) {
                if ( equals( artifact, a ) && hasFile( a ) ) {
                    return a;
                }
            }
        }

        // not available
        return null;
    }
    
    /**
     * Returns <code>true</code> if the artifact has a file.
     *
     * @param artifact the artifact (may be null)
     * @return <code>true</code> if and only if the artifact is non-null and has a file.
     */
    private static boolean hasFile( Artifact artifact )
    {
        return artifact != null && artifact.getFile() != null && artifact.getFile().isFile();
    }

    /**
     * Null-safe compare of two artifacts based on groupId, artifactId, version, type and classifier.
     *
     * @param a the first artifact.
     * @param b the second artifact.
     * @return <code>true</code> if and only if the two artifacts have the same groupId, artifactId, version,
     *         type and classifier.
     */
    private static boolean equals( Artifact a, Artifact b )
    {
        return a == b || !( a == null || b == null )
            && StringUtils.equals( a.getGroupId(), b.getGroupId() )
            && StringUtils.equals( a.getArtifactId(), b.getArtifactId() )
            && StringUtils.equals( a.getVersion(), b.getVersion() )
            && StringUtils.equals( a.getType(), b.getType() )
            && StringUtils.equals( a.getClassifier(), b.getClassifier() );
    }
	
	/**
     * Does the actual copy of the file and logging.
     *
     * @param artifact represents the file to copy.
     * @param destFile file name of destination file.
     * @throws MojoExecutionException with a message if an
     *                                error occurs.
     */
    protected void copyFile( File artifact, File destFile ) throws MojoExecutionException {
        try {
            getLog().info( "Copying " + ( artifact.getName() ) + " to " + destFile.getAbsolutePath() );

            if ( artifact.isDirectory() ) {
                // directory is wrong packaging ... throw error
                throw new MojoExecutionException( "Artifact has not been packaged yet. When used on reactor artifact, "
                    + "copy should be executed after packaging: see MDEP-187." );
            }

            FileUtils.copyFile( artifact, destFile );
        } catch ( IOException e ) {
            throw new MojoExecutionException( "Error copying artifact from " + artifact + " to " + destFile, e );
        }
    }

	public ArtifactFactory getFactory() {
		return factory;
	}

	public void setFactory(ArtifactFactory factory) {
		this.factory = factory;
	}

	public ArtifactResolver getResolver() {
		return resolver;
	}

	public void setResolver(ArtifactResolver resolver) {
		this.resolver = resolver;
	}

	public ArtifactCollector getArtifactCollector() {
		return artifactCollector;
	}

	public void setArtifactCollector(ArtifactCollector artifactCollector) {
		this.artifactCollector = artifactCollector;
	}

	public ArtifactMetadataSource getArtifactMetadataSource() {
		return artifactMetadataSource;
	}

	public void setArtifactMetadataSource(
			ArtifactMetadataSource artifactMetadataSource) {
		this.artifactMetadataSource = artifactMetadataSource;
	}

	public ArtifactRepository getLocal() {
		return local;
	}

	public void setLocal(ArtifactRepository local) {
		this.local = local;
	}

	public List<ArtifactRepository> getRemoteRepos() {
		return remoteRepos;
	}

	public void setRemoteRepos(List<ArtifactRepository> remoteRepos) {
		this.remoteRepos = remoteRepos;
	}

	public boolean isUseJvmChmod() {
		return useJvmChmod;
	}

	public void setUseJvmChmod(boolean useJvmChmod) {
		this.useJvmChmod = useJvmChmod;
	}

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public List<MavenProject> getReactorProjects() {
		return reactorProjects;
	}

	public void setReactorProjects(List<MavenProject> reactorProjects) {
		this.reactorProjects = reactorProjects;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

}
