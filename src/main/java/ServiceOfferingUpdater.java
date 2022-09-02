import de.fhg.ipa.ced.service_registry.model.offerings.docker.DockerComposeServiceOfferingDTOFileImport;
import de.fhg.ipa.ced.service_registry.service.initializer.FileUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class ServiceOfferingUpdater {
    int updateIntervalHours;
    ServiceRepository serviceRepository;
    UpdateTagsTask updateTagsTask;
    private List<Ref> currentTags;
    private List<Ref> previousTags;
    private List<DockerComposeServiceOfferingDTOFileImport> currentServiceOfferingDTOs;

    ServiceOfferingUpdater(ServiceRepository serviceRepository, int updateIntervalHours) {
        this.serviceRepository = serviceRepository;
        this.updateIntervalHours = updateIntervalHours;
        this.updateTagsTask = new UpdateTagsTask(this.serviceRepository);

    }
    void start() throws ExecutionException, InterruptedException {
        serviceRepository.cloneRepository();
        initializeDTOs();
        updateDTOs();
    }




    private void initializeDTOs() throws InterruptedException {
        this.updateTagsTask.run();
        List<Ref> extractedTags = this.updateTagsTask.getQueue().take();
        this.serviceRepository.setTags(extractedTags);
        checkoutTagsAndCreateDTOs();
    }

    void updateDTOs() throws ExecutionException, InterruptedException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(updateTagsTask, this.updateIntervalHours, this.updateIntervalHours, TimeUnit.SECONDS);

        while(true) {
            this.previousTags = this.currentTags;
            this.currentTags = this.updateTagsTask.getQueue().take();


        }

//        currentServiceOfferingDTOs.removeAll(previousServiceOfferingDTOs);
    }
    void checkoutTagsAndCreateDTOs() {
        String serviceOfferingPath = this.serviceRepository.getRepoDir() + "/service-offering.json";

        for (Ref tag : this.serviceRepository.getTags()) {
            try {
                this.serviceRepository.checkoutTag(tag);
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            DockerComposeServiceOfferingDTOFileImport dockerComposeServiceOfferingDTOFileImport
                    = FileUtil.loadFromFile(new File(serviceOfferingPath), DockerComposeServiceOfferingDTOFileImport.class);
            this.currentServiceOfferingDTOs.add(dockerComposeServiceOfferingDTOFileImport);
        }
    }

    private DockerComposeServiceOfferingDTOFileImport parseServiceOffering(String repoDir) {
    }

    public static void main(String[] args) throws IOException, GitAPIException, ExecutionException, InterruptedException {
        String repoUrl = "https://github.com/n14s/grafana-service-offering.git";
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater(new ServiceRepository(repoUrl), 10);
        serviceOfferingUpdater.start();

    }

}
