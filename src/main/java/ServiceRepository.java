import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import de.fhg.ipa.ced.service_registry.service.initializer.FileUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class ServiceRepository {
    private String repoDir = "";
    private String repoUrl = "";
    private List<Ref> tags;
    private Git git;
    private String repoName;
    private String bareUrl;
    private String gitUsername;
    private String gitPassword;

    public ServiceRepository(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoDir() {
        return repoDir;
    }

    public void setRepoDir(String repoDir) {
        this.repoDir = repoDir;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public List<Ref> getTags() {
        return tags;
    }

    public void setTags(List<Ref> tags) {
        this.tags = tags;
    }


    void cloneRepository() {
        this.parseRepoUrl(this.getRepoUrl());

        this.setRepoDir(FileUtil.getExecutionPath(this) + this.repoName);

        if (Files.isDirectory(Paths.get(this.repoDir))) {
            try {
                Git git = Git.open(new File(this.repoDir));
                this.setGit(git);
            } catch(Exception e){
                    System.out.println(e);
            }
        } else {
            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(this.gitUsername, this.gitPassword);
            try {
                Git git = Git.cloneRepository()
                        .setURI(this.bareUrl)
                        .setDirectory(new File(this.repoDir))
                        .setCredentialsProvider(credentialsProvider)
                        .call();
                this.setGit(git);
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }

    void parseRepoUrl(String repoUrl) {
        String repoName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1, repoUrl.lastIndexOf('.'));
        String bareUrl = repoUrl.substring(repoUrl.lastIndexOf('@') + 1);

        if (repoUrl.contains("@")) {
            String gitUsernamePassword = repoUrl.substring(0, repoUrl.lastIndexOf('@'));
            String gitUsername = gitUsernamePassword.substring(0, gitUsernamePassword.indexOf(":"));
            String gitPassword = gitUsernamePassword.substring(gitUsernamePassword.indexOf(":") + 1);

            this.setGitUsername(gitUsername);
            this.setGitPassword(gitPassword);
        }

        this.setRepoName(repoName);
        this.setBareUrl(bareUrl);
    }

    public void setGit(Git git) {
        this.git = git;
    }

    public Git getGit() {
        return git;
    }

    public void checkoutTag(Ref tag) throws GitAPIException {
        this.git.checkout().setName(tag.getName()).call();
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setBareUrl(String bareUrl) {
        this.bareUrl = bareUrl;
    }

    public String getBareUrl() {
        return bareUrl;
    }

    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    public String getGitUsername() {
        return gitUsername;
    }

    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getGitPassword() {
        return gitPassword;
    }
}
