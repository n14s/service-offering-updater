import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.eclipse.jgit.lib.Constants.R_TAGS;


public class ServiceOfferingUpdater {
    void initialize(String repoUrl) throws IOException, GitAPIException {
        ServiceRepository serviceRepository = new ServiceRepository(repoUrl);
        serviceRepository.cloneRepository();
        extractTags(serviceRepository);

        for (Ref tag : serviceRepository.getTags()){
            serviceRepository.checkoutTag(tag);
//           parseServiceOffering();
        }

        serviceRepository.getGit().close();
    }

    void extractTags(ServiceRepository serviceRepository) throws IOException {
        List<Ref> extractedTags = new FileRepository(new File(serviceRepository.getRepoDir(), ".git")).getRefDatabase().getRefsByPrefix(R_TAGS);
        serviceRepository.setTags(extractedTags);
    }

    void update(){
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater();
        serviceOfferingUpdater.initialize("https://github.com/n14s/grafana-service-offering.git");
    }
}
