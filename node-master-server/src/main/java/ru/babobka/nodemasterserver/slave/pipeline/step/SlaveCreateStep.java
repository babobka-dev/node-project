package ru.babobka.nodemasterserver.slave.pipeline.step;

import org.apache.log4j.Logger;
import ru.babobka.nodeconfigs.master.MasterServerConfig;
import ru.babobka.nodemasterserver.slave.Sessions;
import ru.babobka.nodemasterserver.slave.pipeline.PipeContext;
import ru.babobka.nodesecurity.auth.AuthResult;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.func.pipeline.Step;
import ru.babobka.nodeutils.network.NodeConnection;

import java.io.IOException;

/**
 * Created by 123 on 07.06.2018.
 */
public class SlaveCreateStep implements Step<PipeContext> {
    private static final Logger logger = Logger.getLogger(SlaveCreateStep.class);
    private final Sessions sessions = Container.getInstance().get(Sessions.class);
    private final MasterServerConfig config = Container.getInstance().get(MasterServerConfig.class);

    @Override
    public boolean execute(PipeContext pipeContext) {
        AuthResult authResult = pipeContext.getAuthResult();
        NodeConnection connection = pipeContext.getConnection();
        try {
            if (!isAbleToCreateNewSlave(authResult)) {
                logger.error("not able to create session for user " + authResult.getUserName());
                connection.send(false);
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("cannot create slave due to network error", e);
            return false;
        }
    }

    boolean isAbleToCreateNewSlave(AuthResult authResult) {
        return !config.getModes().isSingleSessionMode() || sessions.put(authResult.getUserName());
    }
}
