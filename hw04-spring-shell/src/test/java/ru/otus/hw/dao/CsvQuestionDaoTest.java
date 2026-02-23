package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest(properties = "spring.shell.interactive.enabled=false")
class CsvQuestionDaoTest {

    @Autowired
    private CsvQuestionDao questionDao;

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public TestFileNameProvider testFileNameProvider() {
            TestFileNameProvider mock = mock(TestFileNameProvider.class);
            given(mock.getTestFileName()).willReturn("test-questions.csv");
            return mock;
        }
    }

    @Test
    void findAllShouldReturnCorrectQuestionsList() {
        List<Question> questions = questionDao.findAll();

        assertThat(questions).isNotEmpty()
                .hasSize(3)
                .allSatisfy(question -> {
                    assertThat(question.text()).isNotBlank();
                    assertThat(question.answers()).isNotEmpty();
                });
    }

    @Test
    void findAllShouldParseAnswersCorrectly() {
        List<Question> questions = questionDao.findAll();

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isEqualTo("Is there life on Mars?");
        assertThat(firstQuestion.answers()).hasSize(3);

        List<Answer> answers = firstQuestion.answers();
        assertThat(answers.get(0).isCorrect()).isTrue();
        assertThat(answers.get(0).text()).isEqualTo("Science doesn't know this yet");
        assertThat(answers.get(1).isCorrect()).isFalse();
        assertThat(answers.get(2).isCorrect()).isFalse();
    }
}