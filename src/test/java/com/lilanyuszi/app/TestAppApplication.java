package com.lilanyuszi.app;

import org.springframework.boot.SpringApplication;

public class TestAppApplication {

    public static void main(String[] args) {
        SpringApplication.from(App::main).with(TestcontainersConfiguration.class).run(args);
    }

}
