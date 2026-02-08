package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class CsvQuestionDaoTest {

    private TestFileNameProvider fileNameProvider;
    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
        fileNameProvider = Mockito.mock(TestFileNameProvider.class);
        csvQuestionDao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    void shouldReadQuestionsCorrectly() {
        when(fileNameProvider.getTestFileName()).thenReturn("test-questions.csv");

        List<Question> questions = csvQuestionDao.findAll();

        assertThat(questions).isNotNull();
        assertThat(questions).hasSize(3);

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isEqualTo("What is the capital of Russia?");
        assertThat(firstQuestion.answers()).hasSize(3);

        assertThat(firstQuestion.answers().get(0).text())
                .isEqualTo("Moscow");
        assertThat(firstQuestion.answers().get(0).isCorrect()).isTrue();

        assertThat(firstQuestion.answers().get(1).text())
                .isEqualTo("London");
        assertThat(firstQuestion.answers().get(1).isCorrect()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        when(fileNameProvider.getTestFileName()).thenReturn("non-existent-file.csv");

        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("File not found: non-existent-file.csv");
    }
}