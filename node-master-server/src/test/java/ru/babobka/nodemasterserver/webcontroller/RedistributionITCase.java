package ru.babobka.nodemasterserver.webcontroller;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.http.HttpMessage;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.nodemasterserver.builder.TestUserBuilder;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodeslaveserver.server.SlaveServer;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.util.TextUtil;

public class RedistributionITCase {

    private static SlaveServer[] slaveServers;

    private static final int SLAVES = 3;

    private static MasterServerConfig config;

    private static MasterServer masterServer;

    private static String restLogin;

    private static String restPassword;

    private static int restPort;

    private static String restURL;

    private static final String LOGIN = TestUserBuilder.LOGIN;

    private static final String PASSWORD = TestUserBuilder.PASSWORD;

    private static final String LOGIN_HEADER = "X-Login";

    private static final String PASSWORD_HEADER = "X-Password";

    private static final HttpClient httpClient = HttpClientBuilder.create().build();

    private static final String DUMMY_PRIME_COUNTER_TASK_NAME = TextUtil.toURL("Dummy prime counter");

    @BeforeClass
    public static void runServers() throws IOException, InterruptedException {
	MasterServer.initTestContainer();
	SlaveServer.initTestContainer();
	config = Container.getInstance().get(MasterServerConfig.class);
	restLogin = config.getRestServiceLogin();
	restPassword = config.getRestServicePassword();
	restPort = config.getWebPort();
	restURL = "http://localhost:" + restPort + "/task";

	masterServer = new MasterServer();
	masterServer.start();

    }

    @AfterClass
    public static void closeServers() throws InterruptedException {

	if (masterServer != null)
	    masterServer.interrupt();
	try {
	    if (masterServer != null)
		masterServer.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

    }

    @Test
    public void testMillionPrimesOneSlaveDie() throws IOException, InterruptedException {
	Random random = new Random();
	for (int i = 0; i < 5; i++) {
	    createSlaves();
	    startSlaves();
	    new Thread(new Runnable() {

		@Override
		public void run() {
		    try {
			Thread.sleep(2000);
			slaveServers[random.nextInt(SLAVES)].interrupt();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }

		}
	    }).start();
	    JSONObject jsonObject = getPrimesInRangeJson(0, 15_485_863);
	    assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 1_000_000);
	    closeSlaves();
	}

    }

    public static void createSlaves() throws IOException {
	slaveServers = new SlaveServer[SLAVES];
	for (int i = 0; i < SLAVES; i++) {
	    slaveServers[i] = new SlaveServer("localhost", config.getMainServerPort(), LOGIN, PASSWORD);
	}
    }

    public static void startSlaves() throws InterruptedException {
	for (int i = 0; i < SLAVES; i++) {
	    slaveServers[i].start();
	}
    }

    public static void closeSlaves() throws InterruptedException {
	for (int i = 0; i < SLAVES; i++) {
	    slaveServers[i].interrupt();
	}

    }

    private JSONObject getPrimesInRangeJson(int begin, int end) {

	HttpGet get = null;
	try {
	    String url = restURL + "/" + URLEncoder.encode(DUMMY_PRIME_COUNTER_TASK_NAME, "UTF-8") + "?begin=" + begin
		    + "&end=" + end + "&noCache=true";
	    get = new HttpGet(url);
	    setCredentialHeaders(get);
	    return new JSONObject(new BasicResponseHandler().handleResponse(httpClient.execute(get)));
	} catch (IOException e) {
	    throw new RuntimeException(e);

	} finally {
	    if (get != null)
		get.releaseConnection();
	}

    }

    private void setCredentialHeaders(HttpMessage httpMessage) {
	httpMessage.setHeader(LOGIN_HEADER, restLogin);
	httpMessage.setHeader(PASSWORD_HEADER, restPassword);
    }

}
