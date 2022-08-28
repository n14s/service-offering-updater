public class ServiceRepository {
     private String repoDir = "";
     private String repoUrl= "";

    public ServiceRepository (String repoUrl) {
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
}
