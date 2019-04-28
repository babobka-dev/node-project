package ru.babobka.nodeift.lags;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import ru.babobka.nodeconfigs.master.MasterServerConfig;
import ru.babobka.nodeift.AuthITCase;
import ru.babobka.nodesecurity.rsa.RSAPublicKey;
import ru.babobka.nodetester.master.MasterServerRunner;
import ru.babobka.nodetester.network.LaggyNodeConnectionFactory;
import ru.babobka.nodetester.slave.SlaveServerRunner;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.enums.Env;
import ru.babobka.nodeutils.log.LoggerInit;
import ru.babobka.nodeutils.util.TextUtil;

/**
 * Created by 123 on 06.11.2017.
 */
public class LagAuthITCase extends AuthITCase {

    @BeforeClass
    public static void setUp() {
        LoggerInit.initPersistentConsoleDebugLogger(TextUtil.getEnv(Env.NODE_LOGS), LagAuthITCase.class.getSimpleName());
        Container.getInstance().put(new LaggyNodeConnectionFactory());
        MasterServerRunner.init();
        MasterServerConfig masterServerConfig = Container.getInstance().get(MasterServerConfig.class);
        RSAPublicKey publicKey = masterServerConfig.getSecurity().getRsaConfig().getPublicKey();
        SlaveServerRunner.init(publicKey);
        masterServer = MasterServerRunner.runMasterServer();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        masterServer.interrupt();
        masterServer.join();
        Container.getInstance().clear();
    }

    @Override
    protected int getTests() {
        return 10;
    }

}
