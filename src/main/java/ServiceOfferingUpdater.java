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

    ServiceRepository serviceRepository;

    ServiceOfferingUpdater(ServiceRepository serviceRepository, int updateIntervalHours) {
        this.serviceRepository = serviceRepository;
        this.updateIntervalHours = updateIntervalHours;

    }
    void run() throws IOException, GitAPIException, ExecutionException, InterruptedException {
        serviceRepository.cloneRepository();
        extractTags();
        // createDTOs (initialize)
        updateDTOs();
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
        UpdateTask updateTask = new UpdateTask(this.serviceRepository);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(updateTask, this.updateIntervalHours, this.updateIntervalHours, TimeUnit.SECONDS);

//        currentServiceOfferingDTOs.removeAll(previousServiceOfferingDTOs);
    }


    public static void main(String[] args) throws IOException, GitAPIException, ExecutionException, InterruptedException {
        String repoUrl = "https://github.com/n14s/grafana-service-offering.git";
        ServiceOfferingUpdater serviceOfferingUpdater = new ServiceOfferingUpdater(new ServiceRepository(repoUrl), 10);
        serviceOfferingUpdater.run();

    }
}
