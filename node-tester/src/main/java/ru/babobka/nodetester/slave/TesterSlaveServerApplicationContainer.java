package ru.babobka.nodetester.slave;

import ru.babobka.nodeconfigs.slave.SlaveServerConfig;
import ru.babobka.nodeconfigs.slave.validation.SlaveServerConfigValidator;
import ru.babobka.nodesecurity.SecurityApplicationContainer;
import ru.babobka.nodesecurity.rsa.RSAPublicKey;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeslaveserver.key.SlaveServerKey;
import ru.babobka.nodeslaveserver.server.pipeline.SlavePipelineFactory;
import ru.babobka.nodeslaveserver.service.SlaveAuthService;
import ru.babobka.nodeslaveserver.task.TaskRunnerService;
import ru.babobka.nodetask.NodeTaskApplicationContainer;
import ru.babobka.nodetask.TaskPool;
import ru.babobka.nodeutils.NodeUtilsApplicationContainer;
import ru.babobka.nodeutils.container.AbstractApplicationContainer;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.container.ContainerException;
import ru.babobka.nodeutils.container.Properties;
import ru.babobka.nodeutils.enums.Env;
import ru.babobka.nodeutils.key.UtilKey;
import ru.babobka.nodeutils.network.NodeConnectionFactory;
import ru.babobka.nodeutils.react.PubSub;
import ru.babobka.nodeutils.thread.ThreadPoolService;

import static ru.babobka.nodeslaveserver.key.SlaveServerKey.SLAVE_SERVER_REQUEST_STREAM;


/**
 * Created by 123 on 05.11.2017.
 */
public class TesterSlaveServerApplicationContainer extends AbstractApplicationContainer {

    private final RSAPublicKey rsaPublicKey;

    public TesterSlaveServerApplicationContainer(RSAPublicKey rsaPublicKey) {
        if (rsaPublicKey == null) {
            throw new IllegalArgumentException("rsaPublicKey is null");
        }
        this.rsaPublicKey = rsaPublicKey;
    }

    @Override
    protected void containImpl(Container container) {
        try {
            Properties.put(UtilKey.SERVICE_THREADS_NUM, Runtime.getRuntime().availableProcessors());
            container.put(new NodeUtilsApplicationContainer());
            container.put(new SecurityApplicationContainer());
            container.putIfAbsent(new NodeConnectionFactory());
            container.put(SLAVE_SERVER_REQUEST_STREAM, new PubSub<NodeRequest>());
            SlaveServerConfig config = createTestConfig();
            new SlaveServerConfigValidator().validate(config);
            container.put(config);
            container.put(UtilKey.SERVICE_THREAD_POOL, ThreadPoolService.createDaemonPool("service"));
            container.put(new NodeTaskApplicationContainer());
            container.put(new SlavePipelineFactory());
            container.put(new TaskRunnerService());
            container.put(SlaveServerKey.SLAVE_SERVER_TASK_POOL, new TaskPool(config.getTasksFolder()));
            container.put(new SlaveAuthService());
        } catch (Exception e) {
            throw new ContainerException(e);
        }
    }

    private SlaveServerConfig createTestConfig() {
        SlaveServerConfig config = new SlaveServerConfig();
        config.setSlaveLogin("test_user");
        config.setSlavePassword("test_password");
        config.setTasksFolder("$" + Env.NODE_TASKS.name());
        config.setLoggerFolder("$" + Env.NODE_LOGS.name());
        config.setAuthTimeOutMillis(15_000);
        config.setRequestTimeoutMillis(30_000);
        config.setServerHost("localhost");
        config.setServerPort(9090);
        config.setServerPublicKey(rsaPublicKey);
        return config;
    }
}