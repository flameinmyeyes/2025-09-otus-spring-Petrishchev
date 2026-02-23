package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = "spring.shell.interactive.enabled=false")
class TestServiceImplTest {

    @MockitoBean
    private LocalizedIOService ioService;

    @MockitoBean
    private QuestionDao questionDao;

    @Autowired
    private TestServiceImpl testService;

    @Test
    void executeTestForShouldReturnCorrectResult() {
        var student = new Student("Ivan", "Ivanov");
        var questions = List.of(
                new Question("Q1", List.of(new Answer("A1", true)))
        );
        given(questionDao.findAll()).willReturn(questions);
        given(ioService.readIntForRangeWithPromptLocalized(
                anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(1);
        assertThat(result.getAnsweredQuestions()).hasSize(1);
    }
}