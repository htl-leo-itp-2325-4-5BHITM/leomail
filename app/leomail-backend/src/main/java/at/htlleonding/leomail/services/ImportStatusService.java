package at.htlleonding.leomail.services;

import at.htlleonding.leomail.websockets.ImportStatusWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ImportStatusService {

    private static final Logger LOGGER = Logger.getLogger(ImportStatusService.class);

    private static ImportStatusService instance;

    private final AtomicBoolean importing = new AtomicBoolean(false);

    public ImportStatusService() {
        instance = this;
    }

    public static ImportStatusService getInstance() {
        return instance;
    }

    public void setImporting(boolean status) {
        importing.set(status);
        LOGGER.info("Importing-Status gesetzt auf: " + status);
        ImportStatusWebSocket.broadcastStatus(status);
    }

    public boolean isImporting() {
        return importing.get();
    }
}
