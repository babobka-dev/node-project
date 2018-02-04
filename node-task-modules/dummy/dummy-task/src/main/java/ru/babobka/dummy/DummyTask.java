package ru.babobka.dummy;

import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodetask.exception.ReducingException;
import ru.babobka.nodetask.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by dolgopolov.a on 15.12.15.
 */
public class DummyTask extends SubTask {
    private static final String DESCRIPTION = "Dummy task";

    DummyTask() {
    }

    @Override
    public RequestDistributor getDistributor() {
        return new RequestDistributor() {
            @Override
            protected List<NodeRequest> distributeImpl(NodeRequest request, int nodes) {
                List<NodeRequest> requests = new ArrayList<>();
                for (int i = 0; i < nodes; i++) {
                    requests.add(NodeRequest.regular(request.getTaskId(), request.getTaskName(), new HashMap<>()));
                }
                return requests;
            }
        };
    }

    @Override
    public Reducer getReducer() {
        return new Reducer() {
            @Override
            protected ReducingResult reduceImpl(List<NodeResponse> responses) throws ReducingException {
                return new ReducingResult();
            }
        };
    }

    @Override
    public TaskExecutor getTaskExecutor() {
        return new TaskExecutor() {
            @Override
            protected ExecutionResult executeImpl(NodeRequest request) {
                return ExecutionResult.ok(new HashMap<String, Serializable>());
            }

            @Override
            public void stopCurrentTask() {

            }
        };
    }

    @Override
    public DataValidators getDataValidators() {
        return new DataValidators() {
            @Override
            protected boolean isValidResponseImpl(NodeResponse response) {
                return true;
            }

            @Override
            protected boolean isValidRequestImpl(NodeRequest request) {
                return true;
            }
        };
    }

    @Override
    public boolean isRequestDataTooSmall(NodeRequest request) {
        return false;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean isRaceStyle() {
        return false;
    }

}
