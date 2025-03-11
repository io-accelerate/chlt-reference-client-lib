package io.accelerate.challenge.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReferenceClientTest {

    private ReferenceClient referenceClient;
    
    @BeforeEach
    void setUp() {
        referenceClient = new ReferenceClient();
    }
    
    @Test
    void client_passes_parameters_to_solution() {
        List<Object> args = List.of(11, 22);
        RequestFromServer requestFromServer = new RequestFromServer(
                "ID1", "methodName", args);
        CaptureParametersSolution captureParametersSolution = new CaptureParametersSolution(
                "methodName");
        
        referenceClient.respondToRequest(requestFromServer, captureParametersSolution);

        ParamAccessor[] capturedParams = captureParametersSolution.getCapturedParams();

        assertThat(capturedParams.length, is(args.size()));
        assertThat(capturedParams[0].getAsInteger(), is(11));
        assertThat(capturedParams[1].getAsInteger(), is(22));
    }

    @Test
    void client_returns_value_from_solution() {
        int expectedValue = 123;
        
        RequestFromServer requestFromServer = new RequestFromServer(
                "ID1", "methodName", List.<Object>of(11, 22));
        ResponseToServer responseToServer = referenceClient.respondToRequest(requestFromServer,
                new CannedResponseSolution("methodName", expectedValue));

        assertThat(responseToServer.requestId(), is("ID1"));
        assertThat(responseToServer.value(), is(123));
    }


    @Test
    void client_throws_exception_when_args_fail_to_serialize() {
        List<Object> args = List.of(new PoisonousPayload());
        RequestFromServer requestFromServer = new RequestFromServer(
                "ID1", "methodName", args);
        CannedResponseSolution referenceSolution = new CannedResponseSolution("methodName", "unused");
        
        assertThrows(RuntimeException.class, () ->
            referenceClient.respondToRequest(requestFromServer, referenceSolution));
    }

    @Test
    void client_throws_exception_when_method_not_found() {
        List<Object> args = List.of(11, 22);
        RequestFromServer requestFromServer = new RequestFromServer(
                "ID1", "methodName", args);
        CannedResponseSolution referenceSolution = new CannedResponseSolution("otherMethod", "unused");

        assertThrows(NoSuchMethodError.class, () ->
                referenceClient.respondToRequest(requestFromServer, referenceSolution));
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private static class CaptureParametersSolution implements ReferenceSolution {
        private final String methodName;
        private ParamAccessor[] capturedParams;

        public CaptureParametersSolution(String methodName) {
            this.methodName = methodName;
            this.capturedParams = new ParamAccessor[0];
        }

        @Override
        public void participantReceivesRoundDescription(String description) {
            // DO nothing
        }

        @Override
        public ImplementationMap participantUpdatesImplementationMap() {
            ImplementationMap implementationMap = new ImplementationMap();
            implementationMap.register(methodName, params -> {
                capturedParams = params;
                return true;
            });
            return implementationMap;
        }

        public ParamAccessor[] getCapturedParams() {
            return capturedParams;
        }
    }

    private static class CannedResponseSolution implements ReferenceSolution {
        private final String methodName;
        private final Object cannedResponse;

        public CannedResponseSolution(String methodName, Object cannedResponse) {
            this.methodName = methodName;
            this.cannedResponse = cannedResponse;
        }

        @Override
        public void participantReceivesRoundDescription(String description) {
            // DO nothing
        }

        @Override
        public ImplementationMap participantUpdatesImplementationMap() {
            ImplementationMap implementationMap = new ImplementationMap();
            implementationMap.register(methodName, params -> cannedResponse);
            return implementationMap;
        }
    }
    
    private static class PoisonousPayload {
        
    }
}
