import de.fhg.ipa.ced.service_registry.model.offerings.docker.DockerComposeServiceOfferingDTOFileImport;
import de.fhg.ipa.ced.service_registry.service.initializer.FileUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.jgit.lib.Constants.R_TAGS;


public class ServiceOfferingUpdater {
    List<DockerComposeServiceOfferingDTOFileImport> initialize(String repoUrl) throws IOException, GitAPIException {
        List<DockerComposeServiceOfferingDTOFileImport> dockerComposeServiceOfferingDTOFileImports
                = new ArrayList<DockerComposeServiceOfferingDTOFileImport>();

        ServiceRepository serviceRepository = new ServiceRepository(repoUrl);
        serviceRepository.cloneRepository();
        extractTags(serviceRepository);

        for (Ref tag : serviceRepository.getTags()){
            serviceRepository.checkoutTag(tag);
            DockerComposeServiceOfferingDTOFileImport dockerComposeServiceOfferingDTOFileImport
                    = parseServiceOffering(serviceRepository.getRepoDir());
            dockerComposeServiceOfferingDTOFileImports.add(dockerComposeServiceOfferingDTOFileImport);
        }
        serviceRepository.getGit().close();
        return dockerComposeServiceOfferingDTOFileImports;
    }

    private DockerComposeServiceOfferingDTOFileImport parseServiceOffering(String repoDir) {
        String serviceOfferingPath = repoDir + "/service-offering.json";
        return FileUtil.loadFromFile(new File(serviceOfferingPath), DockerComposeServiceOfferingDTOFileImport.class);
    }

    void extractTags(ServiceRepository serviceRepository) throws IOException {
        List<Ref> extractedTags = new FileRepository(new File(serviceRepository.getRepoDir(), ".git")).getRefDatabase().getRefsByPrefix(R_TAGS);
        serviceRepository.setTags(extractedTags);
    }

    void update(){
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater();
        List<DockerComposeServiceOfferingDTOFileImport> dockerComposeServiceOfferingDTOFileImports = serviceOfferingUpdater.initialize("https://github.com/n14s/grafana-service-offering.git");
    }
}
