package ru.babobka.nodeift.master;

import ru.babobka.nodebusiness.NodeBusinessApplicationContainer;
import ru.babobka.nodebusiness.service.MasterAuthService;
import ru.babobka.nodemasterserver.client.ClientStorage;
import ru.babobka.nodemasterserver.listener.OnRaceStyleTaskIsReady;
import ru.babobka.nodemasterserver.listener.OnTaskIsReady;
import ru.babobka.nodemasterserver.model.ResponseStorage;
import ru.babobka.nodemasterserver.server.IncomingClientsThread;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodemasterserver.service.DistributionService;
import ru.babobka.nodemasterserver.service.TaskServiceImpl;
import ru.babobka.nodemasterserver.slave.IncomingSlaveListenerThread;
import ru.babobka.nodemasterserver.slave.SlaveFactory;
import ru.babobka.nodemasterserver.slave.SlavesStorage;
import ru.babobka.nodemasterserver.thread.HeartBeatingThread;
import ru.babobka.nodemasterserver.validation.config.MasterServerConfigValidator;
import ru.babobka.nodemasterserver.validation.config.rule.*;
import ru.babobka.nodemasterserver.webcontroller.AvailableTasksWebController;
import ru.babobka.nodemasterserver.webcontroller.ClusterInfoWebController;
import ru.babobka.nodemasterserver.webcontroller.TasksInfoWebController;
import ru.babobka.nodetask.NodeTaskApplicationContainer;
import ru.babobka.nodetask.TaskPool;
import ru.babobka.nodetask.model.StoppedTasks;
import ru.babobka.nodeutils.NodeUtilsApplicationContainer;
import ru.babobka.nodeutils.container.ApplicationContainer;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.container.ContainerException;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.network.NodeConnectionFactory;
import ru.babobka.nodeutils.util.HashUtil;
import ru.babobka.nodeweb.NodeWebApplicationContainer;
import ru.babobka.nodeweb.webcontroller.NodeUsersCRUDWebController;
import ru.babobka.nodeweb.webfilter.AuthWebFilter;
import ru.babobka.vsjws.webserver.WebServer;

import java.net.ServerSocket;
import java.util.concurrent.Executors;

/**
 * Created by 123 on 05.11.2017.
 */
public class MasterServerApplicationContainer implements ApplicationContainer {
    private static final String TASKS_FOLDER = "C:\\Users\\123\\Documents\\node-project\\tasks";
    private static final String LOGGER_FOLDER = "C:\\Users\\123\\Documents\\node-project\\logs";
    private static final String REST_LOGIN = "abc";
    private static final String REST_PASSWORD = "123";

    @Override
    public void contain(Container container) {
        try {
            container.put(new NodeUtilsApplicationContainer());
            MasterServerConfig config = new MasterServerConfig();
            config.setTasksFolder(TASKS_FOLDER);
            config.setLoggerFolder(LOGGER_FOLDER);
            config.setAuthTimeOutMillis(2000);
            config.setClientListenerPort(9999);
            config.setDebugMode(true);
            config.setHeartBeatTimeOutMillis(2000);
            config.setRequestTimeOutMillis(5000);
            config.setSlaveListenerPort(9090);
            config.setWebListenerPort(8080);
            config.setRestServiceLogin(REST_LOGIN);
            config.setRestServicePassword(REST_PASSWORD);
            MasterServerConfigValidator configValidator = new MasterServerConfigValidator(new AuthTimeoutMillisValidationRule(), new HeartBeatTimeoutMillisValidationRule(), new LoggerFolderValidationRule(), new MainServerPortValidationRule(), new RequestTimeoutMillisValidationRule(), new RestServiceLoginValidationRule(), new RestServicePasswordValidationRule(), new TaskFolderValidationRule(), new WebPortValidationRule());
            configValidator.validate(config);
            container.put(config);
            SimpleLogger logger = new SimpleLogger("common logger", config.getLoggerFolder(), "common");
            container.put(logger);
            container.put(new NodeTaskApplicationContainer());
            container.put(new NodeBusinessApplicationContainer());
            container.put(new NodeWebApplicationContainer());
            container.put(new SlavesStorage());
            container.put(new DistributionService());
            container.put(new ResponseStorage());
            container.put(new ClientStorage());
            container.put("masterServerTaskPool", new TaskPool(config.getTasksFolder()));
            container.put(new TaskServiceImpl());
            container.put(new StoppedTasks());
            container.put(new SlaveFactory());
            container.put("clientsThreadPool", Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            container.put(new IncomingClientsThread());
            container.put(new HeartBeatingThread());
            container.put(new MasterAuthService());
            container.put(new NodeConnectionFactory());
            container.put(new IncomingSlaveListenerThread(new ServerSocket(config.getSlaveListenerPort())));
            container.put(new OnTaskIsReady());
            container.put(new OnRaceStyleTaskIsReady());
            WebServer webServer = new WebServer("node web server", config.getWebListenerPort(), config.getLoggerFolder());
            AuthWebFilter authWebFilter = new AuthWebFilter(config.getRestServiceLogin(), HashUtil.sha2(config.getRestServicePassword()));
            webServer.addController("users", new NodeUsersCRUDWebController()).addWebFilter(authWebFilter);
            webServer.addController("availableTasks", new AvailableTasksWebController()).addWebFilter(authWebFilter);
            webServer.addController("clusterInfo", new ClusterInfoWebController()).addWebFilter(authWebFilter);
            webServer.addController("taskInfo", new TasksInfoWebController()).addWebFilter(authWebFilter);
            container.put(webServer);
        } catch (Exception e) {
            throw new ContainerException(e);
        }
    }
}
