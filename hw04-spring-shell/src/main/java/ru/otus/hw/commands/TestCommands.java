package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent
@RequiredArgsConstructor
public class TestCommands {

    private final TestRunnerService testRunnerService;

    @ShellMethod(value = "Run student testing", key = {"test", "t"})
    public void runTest() {
        testRunnerService.run();
    }
}