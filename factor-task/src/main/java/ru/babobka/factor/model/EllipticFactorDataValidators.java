package ru.babobka.factor.model;

import ru.babobka.factor.task.Params;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.enumerations.ResponseStatus;
import ru.babobka.nodetask.model.DataValidators;
import ru.babobka.nodeutils.container.Container;
import ru.babobka.nodeutils.logger.SimpleLogger;

import java.math.BigInteger;

/**
 * Created by 123 on 25.10.2017.
 */
public class EllipticFactorDataValidators extends DataValidators {

    private final SimpleLogger logger = Container.getInstance().get(SimpleLogger.class);

    @Override
    protected boolean isValidResponseImpl(NodeResponse response) {
        try {
            if (response != null && response.getStatus() == ResponseStatus.NORMAL) {
                BigInteger factor = response.getDataValue(Params.FACTOR.getValue());
                BigInteger n = response.getDataValue(Params.NUMBER.getValue());
                if (factor != null && n != null && !factor.equals(BigInteger.ONE.negate()) && n.mod(factor).equals(BigInteger.ZERO)) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    protected boolean isValidRequestImpl(NodeRequest request) {
        try {
            BigInteger n = request.getDataValue(Params.NUMBER.getValue());
            if (n.compareTo(BigInteger.ZERO) <= 0) {
                return false;
            }
        } catch (RuntimeException e) {
            logger.error(e);
            return false;
        }
        return true;
    }
}
