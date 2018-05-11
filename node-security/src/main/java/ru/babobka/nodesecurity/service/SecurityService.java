package ru.babobka.nodesecurity.service;

import ru.babobka.nodesecurity.config.SrpConfig;
import ru.babobka.nodesecurity.data.SecureNodeRequest;
import ru.babobka.nodesecurity.data.SecureNodeResponse;
import ru.babobka.nodeserials.NodeData;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.enumerations.RequestStatus;
import ru.babobka.nodeserials.enumerations.ResponseStatus;
import ru.babobka.nodeutils.math.Fp;
import ru.babobka.nodeutils.network.NodeConnection;
import ru.babobka.nodeutils.util.ArrayUtil;
import ru.babobka.nodeutils.util.HashUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by 123 on 24.04.2018.
 */
public class SecurityService {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private final SecureRandom secureRandom = new SecureRandom();

    public boolean sendChallenge(NodeConnection connection, byte[] secretKey, SrpConfig srpConfig) throws IOException {
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        } else if (connection.isClosed()) {
            throw new IllegalArgumentException("connection is closed");
        } else if (ArrayUtil.isEmpty(secretKey)) {
            throw new IllegalArgumentException("secretKey is empty");
        } else if (srpConfig == null) {
            throw new IllegalArgumentException("srpConfig is empty");
        }
        byte[] challenge = createChallenge(srpConfig);
        connection.send(challenge);
        byte[] challengerSolution = connection.receive();
        byte[] solution = HashUtil.sha2(secretKey, challenge);
        return Arrays.equals(challengerSolution, solution);
    }

    public boolean solveChallenge(NodeConnection connection, byte[] secretKey) throws IOException {
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        } else if (connection.isClosed()) {
            throw new IllegalArgumentException("connection is closed");
        } else if (ArrayUtil.isEmpty(secretKey)) {
            throw new IllegalArgumentException("secretKey is empty");
        }
        byte[] clientChallenge = connection.receive();
        byte[] clientSolution = HashUtil.sha2(secretKey, clientChallenge);
        connection.send(clientSolution);
        return connection.receive();
    }

    byte[] createChallenge(SrpConfig srpConfig) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] challenge = new byte[srpConfig.getChallengeBytes()];
        secureRandom.nextBytes(challenge);
        return challenge;
    }

    public byte[] createSecretKeyHost(Fp A, Fp B, Fp b, Fp v, SrpConfig srpConfig) {
        Fp u = new Fp(new BigInteger(
                HashUtil.sha2(A.getNumber().toByteArray(),
                        B.getNumber().toByteArray())), srpConfig.getG().getMod());
        Fp S = A.mult(v.pow(u.getNumber())).pow(b.getNumber());
        return HashUtil.sha2(S.getNumber().toByteArray());
    }

    public byte[] createSecretKeyUser(Fp B, Fp a, Fp u, Fp x, SrpConfig srpConfig) {
        Fp v = srpConfig.getG().pow(x.getNumber());
        Fp S = B.subtract(srpConfig.getK().mult(v)).pow(
                a.getNumber().add(u.getNumber().multiply(x.getNumber()))
        );
        return HashUtil.sha2(S.getNumber().toByteArray());
    }

    public BigInteger generatePrivateKey(SrpConfig srpConfig) {
        return new BigInteger(srpConfig.getG().getMod().subtract(BigInteger.ONE).bitLength(), secureRandom);
    }

    public byte[] secretBuilder(byte[] password, byte[] salt, SrpConfig srpConfig) {
        if (ArrayUtil.isEmpty(password)) {
            throw new IllegalArgumentException("password is empty");
        } else if (ArrayUtil.isEmpty(salt)) {
            throw new IllegalArgumentException("salt is empty");
        } else if (srpConfig == null) {
            throw new IllegalArgumentException("srpConfig is null");
        }
        byte[] saltedPassword = HashUtil.sha2(password, salt);
        Fp gen = srpConfig.getG();
        Fp secret = new Fp(new BigInteger(saltedPassword), gen.getMod());
        return gen.pow(secret.getNumber()).getNumber().toByteArray();
    }

    public byte[] buildMac(NodeData data, byte[] secretKey) {
        if (data == null) {
            throw new IllegalArgumentException("cannot build mac of null data");
        } else if (ArrayUtil.isEmpty(secretKey)) {
            throw new IllegalArgumentException("secretKey was not set");
        }
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey, HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            return mac.doFinal(data.getHash());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isSecure(Object object, byte[] secretKey) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        } else if (ArrayUtil.isEmpty(secretKey)) {
            throw new IllegalArgumentException("secretKey is empty");
        } else if (object instanceof NodeResponse) {
            return isSecure((NodeResponse) object, secretKey);
        } else if (object instanceof NodeRequest) {
            return isSecure((NodeRequest) object, secretKey);
        }
        return false;
    }

    private boolean isSecure(NodeResponse response, byte[] secretKey) {
        if (response.getStatus() == ResponseStatus.HEART_BEAT) {
            return true;
        } else if (!(response instanceof SecureNodeResponse)) {
            return false;
        }
        byte[] mac = ((SecureNodeResponse) response).getMac();
        return Arrays.equals(mac, buildMac(response, secretKey));
    }

    private boolean isSecure(NodeRequest request, byte[] secretKey) {
        if (request.getRequestStatus() == RequestStatus.HEART_BEAT) {
            return true;
        } else if (!(request instanceof SecureNodeRequest)) {
            return false;
        }
        byte[] mac = ((SecureNodeRequest) request).getMac();
        return Arrays.equals(mac, buildMac(request, secretKey));
    }
}