package io.accelerate.challenge.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Created by julianghionoiu on 02/08/2015.
 */
public class ReferenceClient {

    public ResponseToServer respondToRequest(RequestFromServer requestFromServer, ReferenceSolution referenceSolution) {
        String requestId = requestFromServer.requestId();
        String methodName = requestFromServer.methodName();
        ParamAccessor[] serializedParams = serializeAndDeserializeArgs(requestFromServer);

        ImplementationMap implementations = referenceSolution.participantUpdatesImplementationMap();
        UserImplementation userImplementation = implementations.getImplementationFor(methodName);
        Object actualReturnedValue = userImplementation.process(serializedParams);

        return new ResponseToServer(requestId, actualReturnedValue);
    }

    //~~ Process
    private ParamAccessor[] serializeAndDeserializeArgs(RequestFromServer requestFromServer) {
        try {
            // Create an ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert the original parameters to JSON
            Object[] originalArgs = requestFromServer.args().toArray();
            String serializedArgs = objectMapper.writeValueAsString(originalArgs);

            // Deserialize the JSON string back into a list of JsonNode
            List<JsonNode> elements = objectMapper.readValue(serializedArgs, new TypeReference<>() {});

            // Convert JsonNode elements to ParamAccessor instances
            ParamAccessor[] paramAccessors = new ParamAccessor[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                paramAccessors[i] = new ParamAccessor(elements.get(i));
            }

            return paramAccessors;
        } catch (Exception e) {
            throw new RuntimeException("Error during serialization and deserialization of params", e);
        }
    }
}
