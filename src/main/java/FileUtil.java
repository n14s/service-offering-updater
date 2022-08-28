import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class FileUtil {

    private static final String FILE_PATH_PREFIX = "file:/";

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static String getExecutionPath(Object obj){
        String s = obj.getClass().getResource("/").getPath();
        if(!s.startsWith(FILE_PATH_PREFIX)) {
            //s= FILE_PATH_PREFIX+"/"+s;
            try {
                URI uri = new URI(s);
                return uri.toString();
            } catch (URISyntaxException e) {
                LOG.error("URISyntaxException during get execution path of '{}'", obj, e);
            }
        }
        return s;
    }

}
