import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

public class UpdateTagsTask implements Runnable{
    private final ServiceRepository serviceRepository;

    private final BlockingQueue<List<Ref>> queue = new LinkedBlockingQueue<>();

    public BlockingQueue<List<Ref>> getQueue(){
        return this.queue;
    }

    UpdateTagsTask(ServiceRepository serviceRepository){
        this.serviceRepository = serviceRepository;
    }
    @Override
    public void run() {
        try {
            this.serviceRepository.getGit().pull().setRemote("origin").setRemoteBranchName("main").call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        try {
            this.queue.offer(extractTags());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<Ref> extractTags() throws IOException {
        return new FileRepository(new File(this.serviceRepository.getRepoDir(), ".git")).getRefDatabase().getRefsByPrefix(R_TAGS);
    }

}
