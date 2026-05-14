package com.hotelbooking.util;

import com.hotelbooking.exception.ErrorResponse;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reusable AssertJ assertions for API error bodies.
 */
public final class ErrorResponseAssert extends AbstractAssert<ErrorResponseAssert, ErrorResponse> {

    private ErrorResponseAssert(ErrorResponse actual) {
        super(actual, ErrorResponseAssert.class);
    }

    public static ErrorResponseAssert assertThatError(ErrorResponse actual) {
        return new ErrorResponseAssert(actual);
    }

    public ErrorResponseAssert hasStatus(int status) {
        isNotNull();
        assertThat(actual.status()).isEqualTo(status);
        return this;
    }

    public ErrorResponseAssert hasMessageContaining(String fragment) {
        isNotNull();
        assertThat(actual.message()).containsIgnoringCase(fragment);
        return this;
    }

    public ErrorResponseAssert hasPath(String path) {
        isNotNull();
        assertThat(actual.path()).isEqualTo(path);
        return this;
    }
}
