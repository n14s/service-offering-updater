import de.fhg.ipa.ced.service_registry.model.offerings.docker.DockerComposeServiceOfferingDTOFileImport;
import de.fhg.ipa.ced.service_registry.service.initializer.FileUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.eclipse.jgit.lib.Constants.R_TAGS;


public class ServiceOfferingUpdater {
    int updateIntervalHours;
    List<DockerComposeServiceOfferingDTOFileImport> previousServiceOfferingDTOs = new ArrayList<DockerComposeServiceOfferingDTOFileImport>();
    List<DockerComposeServiceOfferingDTOFileImport> currentServiceOfferingDTOs = new ArrayList<DockerComposeServiceOfferingDTOFileImport>();

    ServiceRepository serviceRepository;

    ServiceOfferingUpdater(ServiceRepository serviceRepository, int updateIntervalHours) {
        this.serviceRepository = serviceRepository;
        this.updateIntervalHours = updateIntervalHours;

    }
    void run() throws IOException, GitAPIException, ExecutionException, InterruptedException {
        serviceRepository.cloneRepository();
        extractTags();
        createDTOs();
        updateDTOs();
    }

    void createDTOs() throws GitAPIException {
        this.previousServiceOfferingDTOs = this.currentServiceOfferingDTOs;

        for (Ref tag : this.serviceRepository.getTags()){
            this.serviceRepository.checkoutTag(tag);
            DockerComposeServiceOfferingDTOFileImport dockerComposeServiceOfferingDTOFileImport
                    = parseServiceOffering(serviceRepository.getRepoDir());
            this.currentServiceOfferingDTOs.add(dockerComposeServiceOfferingDTOFileImport);
        }
    }

    private DockerComposeServiceOfferingDTOFileImport parseServiceOffering(String repoDir) {
        String serviceOfferingPath = repoDir + "/service-offering.json";
        return FileUtil.loadFromFile(new File(serviceOfferingPath), DockerComposeServiceOfferingDTOFileImport.class);
    }

    void extractTags() throws IOException {
        List<Ref> extractedTags = new FileRepository(new File(this.serviceRepository.getRepoDir(), ".git")).getRefDatabase().getRefsByPrefix(R_TAGS);
        this.serviceRepository.setTags(extractedTags);
    }

    void updateDTOs() throws ExecutionException, InterruptedException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(ServiceOfferingUpdater::update, this.updateIntervalHours, this.updateIntervalHours, TimeUnit.SECONDS);
        System.out.println(future.get());
//        currentServiceOfferingDTOs.removeAll(previousServiceOfferingDTOs);
    }

    private static String update() {
        System.out.println("update");
        return "hi";
    }



    public static void main(String[] args) throws IOException, GitAPIException, ExecutionException, InterruptedException {
        String repoUrl = "https://github.com/n14s/grafana-service-offering.git";
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater(new ServiceRepository(repoUrl), 10);
        serviceOfferingUpdater.run();

    }
}
