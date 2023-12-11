package io.github.mortuusars.exposure.test.framework;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class Test {
    public final String name;
    public final Consumer<ServerPlayer> test;

    public Test(String name, Consumer<ServerPlayer> test) {
        this.name = name;
        this.test = test;
    }
}
