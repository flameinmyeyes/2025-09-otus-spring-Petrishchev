package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    void executeTest_ShouldPrintQuestionsAndAnswers() {
        List<Question> questions = List.of(
                new Question("Вопрос 1", List.of(
                        new Answer("Ответ 1", true),
                        new Answer("Ответ 2", false),
                        new Answer("Ответ 3", false)
                )),
                new Question("Вопрос 2", List.of(
                        new Answer("Ответ 1", true),
                        new Answer("Ответ 2", false),
                        new Answer("Ответ 3", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);

        testService.executeTest();

        verify(ioService).printFormattedLine("Please answer the questions below%n");

        verify(ioService, times(2)).printFormattedLine(
                eq("Question %d: %s"), anyInt(), anyString()
        );

        verify(ioService, times(6)).printFormattedLine(
                eq("  %d) %s"), anyInt(), anyString()
        );

        verify(ioService, atLeast(1)).printLine("");
    }
}