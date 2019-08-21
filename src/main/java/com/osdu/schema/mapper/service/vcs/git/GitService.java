package com.osdu.schema.mapper.service.vcs.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
public class GitService implements GitClient {

    @Value("${git.folder.path}")
    private String folderPath;

    /**
     * Clones a given repository to a specified folder ( 'git.folder.path' ) property in the
     * application.properties file.
     * <p>
     * This is a version for repositories which accept pulling without SSH key via credentials.
     * For SSH based version @see
     *
     * @param repositoryUrl the url of the repository to clone
     * @param user          username which can be used to pull
     * @param password      password of the username that will be used
     */
    private void cloneRepository(String repositoryUrl, String user, String password) throws GitAPIException {
        Git.cloneRepository()
                .setURI(repositoryUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password))
                .setDirectory(Paths.get("resource/git").toFile()).call();
    }


    /**
     * Clones a given repository to a specified folder ( 'git.folder.path' ) property in the
     * application.properties file.
     * <p>
     * This is a version for repositories which do not allow Credentials based auth and force the user to use SSH keys.
     *
     * @param repositoryUrl the url of the repository to clone
     * @param keyName       keyName that has to be pulled from KeyStory
     */
    private void cloneRepository(String repositoryUrl, String keyName) throws GitAPIException {
        Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(Paths.get("resource/git").toFile()).call();
    }


}
