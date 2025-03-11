package io.accelerate.challenge.client;

import java.util.List;

/**
 * Created by julianghionoiu on 07/03/2015.
 */
public record RequestFromServer(String requestId, String methodName, List<?> args) {
}
