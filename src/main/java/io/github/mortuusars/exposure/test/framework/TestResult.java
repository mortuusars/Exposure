package io.github.mortuusars.exposure.test.framework;

import javax.annotation.Nullable;
import java.util.Objects;

public final class TestResult {
    public enum Status {
        PASSED,
        FAILED,
        SKIPPED
    }

    private final String name;
    private final Status status;
    @Nullable
    private final String error;

    private TestResult(String name, Status status, @Nullable String error) {
        this.name = name;
        this.status = status;
        this.error = error;
    }

    public static TestResult pass(String name) {
        return new TestResult(name, Status.PASSED, null);
    }

    public static TestResult error(String name, String error) {
        return new TestResult(name, Status.FAILED, error);
    }

    public static TestResult skip(String name) {
        return new TestResult(name, Status.SKIPPED, null);
    }

    public String name() {
        return name;
    }

    public Status status() {
        return status;
    }

    @Nullable
    public String error() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestResult that = (TestResult) o;
        return Objects.equals(name, that.name) && status == that.status && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status, error);
    }

    @Override
    public String toString() {
        return "[" + status + "] " + name + (error != null ? "\n" + error : "");
    }
}
