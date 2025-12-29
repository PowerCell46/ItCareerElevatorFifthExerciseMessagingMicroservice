package com.ItCareerElevatorFifthExercise.util;

public class RetryPolicy {

    public static boolean isRetriable(Throwable throwable) {
        return isNetworkIssue(throwable) || isTransientHttpResponse(throwable);
    }

    private static boolean isNetworkIssue(Throwable throwable) {
        return
                // @formatter:off
                    throwable instanceof java.net.ConnectException ||
                    throwable instanceof java.util.concurrent.TimeoutException ||
                    throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException ||
                    throwable.getCause() instanceof java.net.SocketException;
                // @formatter:on
    }

    private static boolean isTransientHttpResponse(Throwable t) {
        if (t instanceof org.springframework.web.reactive.function.client.WebClientResponseException ex) {
            int status = ex.getStatusCode().value();

            return status == 502 || status == 503 || status == 504;
        }

        return false;
    }
}
