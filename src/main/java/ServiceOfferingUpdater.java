import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;


public class ServiceOfferingUpdater {
    void initialize(String repoUrl) throws IOException {
        ServiceRepository serviceRepository = new ServiceRepository(repoUrl);
        cloneRepo(serviceRepository);
    }

        void cloneRepo(ServiceRepository serviceRepository) {
            try {
                //parse url, username and pw
                String[] repoUrlElements = parseRepoUrl(serviceRepository.getRepoUrl());
                String repoName = repoUrlElements[0];
                String bareUrl = repoUrlElements[1];
                String gitUsername = repoUrlElements[2];
                String gitPassword= repoUrlElements[3];

                UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( gitUsername, gitPassword);

                serviceRepository.setRepoDir(FileUtil.getExecutionPath(this)+ repoName);

                Git git = Git.cloneRepository()
                        .setURI(bareUrl)
                        .setDirectory(new File(serviceRepository.getRepoDir()))
                        .call();

            } catch (Exception e) {
                System.out.println(e);
            }
        }

    String[] parseRepoUrl(String repoUrl){
        String repoName = "";
        String bareUrl = "";
        String gitUsername = "";
        String gitPassword = "";

        repoName = repoUrl.substring(repoUrl.lastIndexOf('/') + 1, repoUrl.lastIndexOf('.'));
        bareUrl = repoUrl.substring(repoUrl.lastIndexOf('@') + 1);

        if (repoUrl.contains("@")) {
            String gitUsernamePassword = repoUrl.substring(0,repoUrl.lastIndexOf('@'));
            gitUsername= gitUsernamePassword.substring(0,gitUsernamePassword.indexOf(":"));
            gitPassword= gitUsernamePassword.substring(gitUsernamePassword.indexOf(":") + 1);
        }

        String[] repoUrlElements = {repoName, bareUrl, gitUsername, gitPassword};
        return repoUrlElements;
    }





    void update(){

    }

    public static void main(String[] args) throws IOException {
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater();
        serviceOfferingUpdater.initialize("https://github.com/n14s/grafana-service-offering.git");
    }
}
