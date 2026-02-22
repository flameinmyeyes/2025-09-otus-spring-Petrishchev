package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("autorun")
@Component
@RequiredArgsConstructor
public class TestAutoRunner implements CommandLineRunner {

    private final TestRunnerService testRunnerService;

    @Override
    public void run(String... args) {
        testRunnerService.run();
    }
}
