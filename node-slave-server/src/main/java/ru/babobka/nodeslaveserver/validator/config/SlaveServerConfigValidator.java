package ru.babobka.nodeslaveserver.validator.config;

import ru.babobka.nodeslaveserver.server.SlaveServerConfig;
import ru.babobka.nodeslaveserver.validator.config.rule.*;
import ru.babobka.nodeutils.validation.Validator;

import java.util.Arrays;

/**
 * Created by 123 on 05.11.2017.
 */
public class SlaveServerConfigValidator extends Validator<SlaveServerConfig> {
    public SlaveServerConfigValidator() {
        super(Arrays.asList(
                new ServerPortValidationRule(),
                new AuthTimeoutMillisValidationRule(),
                new LoggerFolderValidationRule(),
                new RequestTimeoutMillisValidationRule(),
                new TasksFolderValidationRule()));
    }
}