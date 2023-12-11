package io.github.mortuusars.exposure.test.framework;

import java.util.List;

public interface ITestClass {
    List<Test> collect();


    default void assertThat(boolean condition, String error) {
        if (!condition)
            throw new IllegalStateException(error);
    }
}
