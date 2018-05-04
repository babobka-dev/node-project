package ru.babobka.nodemasterserver.service;

import ru.babobka.nodebusiness.model.User;
import ru.babobka.nodebusiness.service.NodeUsersService;
import ru.babobka.nodesecurity.auth.AbstractAuth;
import ru.babobka.nodesecurity.auth.AuthData;
import ru.babobka.nodesecurity.auth.AuthResult;
import ru.babobka.nodesecurity.config.SrpConfig;
import ru.babobka.nodesecurity.service.SecurityService;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;
import ru.babobka.nodeutils.math.Fp;
import ru.babobka.nodeutils.network.NodeConnection;

import java.io.IOException;
import java.math.BigInteger;


/**
 * Created by dolgopolov.a on 29.10.15.
 */
public class MasterAuthService extends AbstractAuth {

    private final NodeUsersService usersService = Container.getInstance().get(NodeUsersService.class);
    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);
    private final SrpConfig srpConfig = Container.getInstance().get(SrpConfig.class);
    private final SecurityService securityService = Container.getInstance().get(SecurityService.class);

    public AuthResult auth(NodeConnection connection) throws IOException {
        String login = connection.receive();
        User user = usersService.get(login);
        if (user == null) {
            logger.debug("can not find user " + login);
            return fail(connection);
        } else {
            logger.debug(login + " was found");
            success(connection);
        }
        return srpHostAuth(connection, user);
    }

    AuthResult srpHostAuth(NodeConnection connection, User user) throws IOException {
        connection.send(new AuthData(srpConfig, user.getSalt()));
        Fp A = connection.receive();
        if (!A.isSameMod(srpConfig.getG()) || A.isAddNeutral() || A.isMultNeutral()) {
            logger.debug("invalid A :" + A);
            return fail(connection);
        } else {
            logger.debug("client's A is fine");
            success(connection);
        }
        Fp b = new Fp(securityService.generatePrivateKey(srpConfig), srpConfig.getG().getMod());
        Fp v = new Fp(new BigInteger(user.getSecret()), srpConfig.getG().getMod());
        Fp k = srpConfig.getK();
        Fp B = k.mult(v).add(srpConfig.getG().pow(b.getNumber()));
        connection.send(B);
        boolean validB = connection.receive();
        if (!validB) {
            logger.debug("failed B validation from client: " + B);
            return fail(connection);
        }
        byte[] secretKey = securityService.createSecretKeyHost(A, B, b, v, srpConfig);
        boolean challengeResult = securityService.sendChallenge(connection, secretKey, srpConfig);
        if (!challengeResult) {
            logger.debug("client failed to solve challenge");
            return fail(connection);
        } else {
            logger.debug("client solved challenge");
            success(connection);
        }
        boolean solvedChallengeResult = securityService.solveChallenge(connection, secretKey);
        if (!solvedChallengeResult) {
            return fail(connection);
        } else {
            return AuthResult.success(secretKey);
        }
    }

}
