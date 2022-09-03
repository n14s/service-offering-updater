import de.fhg.ipa.ced.service_registry.model.offerings.docker.DockerComposeServiceOfferingDTOFileImport;
import de.fhg.ipa.ced.service_registry.service.initializer.FileUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ServiceOfferingUpdater {
    int updateIntervalHours;
    ServiceRepository serviceRepository;
    UpdateTagsTask updateTagsTask;

    ServiceOfferingUpdater(ServiceRepository serviceRepository, int updateIntervalHours) {
        this.serviceRepository = serviceRepository;
        this.updateIntervalHours = updateIntervalHours;
        this.updateTagsTask = new UpdateTagsTask(this.serviceRepository);

    }
    void start() throws InterruptedException {
        serviceRepository.cloneAndAssignRepository();
        initializeDTOs();
        updateDTOs();
    }




    private void initializeDTOs() throws InterruptedException {
        this.updateTagsTask.run();
        List<Ref> queuedTags = this.updateTagsTask.getQueue().take();
        this.serviceRepository.setTags(queuedTags);
        List<DockerComposeServiceOfferingDTOFileImport> initialServiceOfferingDTOs = checkoutTagsAndCreateDTOs(queuedTags);

        // Handle initialServiceOfferingDTOs. Replace next line.
        System.out.println("initial DTOs: " + initialServiceOfferingDTOs);
    }

    void updateDTOs() throws InterruptedException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(updateTagsTask, this.updateIntervalHours, this.updateIntervalHours, TimeUnit.SECONDS);

        while(true) {
            List<Ref> queuedTags = this.updateTagsTask.getQueue().take();

            if (queuedTags.size() != this.serviceRepository.getTags().size()){
                List<Ref> newTags = queuedTags.subList(this.serviceRepository.getTags().size(), queuedTags.size());
                this.serviceRepository.setTags(queuedTags);

                List<DockerComposeServiceOfferingDTOFileImport> newServiceOfferingDTOs = checkoutTagsAndCreateDTOs(newTags);

                // handle newServiceOfferingDTOs. Replace next line.
                System.out.println("new DTOs: " + newServiceOfferingDTOs);
            }
        }
    }
    List<DockerComposeServiceOfferingDTOFileImport> checkoutTagsAndCreateDTOs(List<Ref> newTags) {
        String serviceOfferingPath = this.serviceRepository.getRepoDir() + "/service-offering.json";
        List<DockerComposeServiceOfferingDTOFileImport> newServiceOfferingDTOs = new ArrayList<>();

        for (Ref tag : newTags) {
            try {
                this.serviceRepository.checkoutTag(tag);
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            DockerComposeServiceOfferingDTOFileImport dockerComposeServiceOfferingDTOFileImport
                    = FileUtil.loadFromFile(new File(serviceOfferingPath), DockerComposeServiceOfferingDTOFileImport.class);
            newServiceOfferingDTOs.add(dockerComposeServiceOfferingDTOFileImport);
        }
        return newServiceOfferingDTOs;
    }


    public static void main(String[] args) throws InterruptedException {
        String repoUrl = "https://github.com/n14s/grafana-service-offering.git";
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater(new ServiceRepository(repoUrl), 10);
        serviceOfferingUpdater.start();

    }
}
