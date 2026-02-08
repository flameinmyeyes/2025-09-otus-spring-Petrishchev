package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestServiceImplTest {

    private IOService ioService;
    private QuestionDao questionDao;
    private TestServiceImpl testService;
    private Student student;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        questionDao = mock(QuestionDao.class);
        testService = new TestServiceImpl(ioService, questionDao);
        student = new Student("Ivan", "Ivanov");
    }

    @Test
    @DisplayName("Должен корректно обрабатывать правильные и неправильные ответы")
    void shouldProcessCorrectAndIncorrectAnswers() {
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Correct answer 1", true),
                        new Answer("Incorrect answer 1", false),
                        new Answer("Incorrect answer 2", false)
                )),
                new Question("Question 2", List.of(
                        new Answer("Incorrect answer 3", false),
                        new Answer("Incorrect answer 4", false),
                        new Answer("Correct answer 2", true)
                )),
                new Question("Question 3", List.of(
                        new Answer("Incorrect answer 5", false),
                        new Answer("Correct answer 3", true),
                        new Answer("Incorrect answer 6", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1)
                .thenReturn(3)
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(3);
        assertThat(result.getAnsweredQuestions()).hasSize(3);
        assertThat(result.getStudent()).isEqualTo(student);
    }

    @Test
    @DisplayName("Должен корректно отображать вопросы и ответы")
    void shouldDisplayQuestionsCorrectly() {
        List<Question> questions = List.of(
                new Question("Test question?", List.of(
                        new Answer("Answer 1", true),
                        new Answer("Answer 2", false),
                        new Answer("Answer 3", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        testService.executeTestFor(student);

        verify(ioService, atLeastOnce()).printLine("Test question?");
        verify(ioService, atLeastOnce()).printFormattedLine(eq("%d. %s"), eq(1), eq("Answer 1"));
        verify(ioService, atLeastOnce()).printFormattedLine(eq("%d. %s"), eq(2), eq("Answer 2"));
        verify(ioService, atLeastOnce()).printFormattedLine(eq("%d. %s"), eq(3), eq("Answer 3"));
    }

    @Test
    @DisplayName("Должен обрабатывать неправильные ответы")
    void shouldProcessIncorrectAnswers() {
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Correct answer", true),
                        new Answer("Incorrect answer", false)
                )),
                new Question("Question 2", List.of(
                        new Answer("Incorrect answer", false),
                        new Answer("Correct answer", true)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2)
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(0);
        assertThat(result.getAnsweredQuestions()).hasSize(2);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать смешанные ответы")
    void shouldProcessMixedAnswers() {
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Correct answer", true),
                        new Answer("Incorrect answer", false)
                )),
                new Question("Question 2", List.of(
                        new Answer("Incorrect answer", false),
                        new Answer("Correct answer", true)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1)
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(1);
        assertThat(result.getAnsweredQuestions()).hasSize(2);
    }
}