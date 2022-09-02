import de.fhg.ipa.ced.service_registry.model.offerings.docker.DockerComposeServiceOfferingDTOFileImport;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UpdateTask implements Runnable{


    private final ServiceRepository serviceRepository;
    List<DockerComposeServiceOfferingDTOFileImport> previousServiceOfferingDTOs = new ArrayList<DockerComposeServiceOfferingDTOFileImport>();
    List<DockerComposeServiceOfferingDTOFileImport> currentServiceOfferingDTOs = new ArrayList<DockerComposeServiceOfferingDTOFileImport>();


    BlockingQueue queue = new LinkedBlockingQueue();

    UpdateTask(ServiceRepository serviceRepository){
        this.serviceRepository = serviceRepository;
    }
    @Override
    public void run() {


        this.previousServiceOfferingDTOs = this.currentServiceOfferingDTOs;

        for (Ref tag : this.serviceRepository.getTags()){
            try {
                this.serviceRepository.checkoutTag(tag);
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            DockerComposeServiceOfferingDTOFileImport dockerComposeServiceOfferingDTOFileImport
                    = this.serviceRepository.parseServiceOffering(serviceRepository.getRepoDir()); // put parseServiceOffering in util class
            this.currentServiceOfferingDTOs.add(dockerComposeServiceOfferingDTOFileImport);
        }
    }
}
