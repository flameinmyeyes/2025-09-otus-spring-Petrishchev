package ru.otus.hw.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AppPropertiesTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void shouldLoadPropertiesCorrectly() {
        assertThat(appProperties).isNotNull();
        assertThat(appProperties.getRightAnswersCountToPass()).isEqualTo(5);
        assertThat(appProperties.getLocale()).isEqualTo(Locale.forLanguageTag("ru-RU"));

        Map<String, String> fileNameByLocale = appProperties.getFileNameByLocaleTag();
        assertThat(fileNameByLocale).containsKeys("ru-RU", "en-US");
        assertThat(fileNameByLocale.get("en-US")).isEqualTo("test-questions.csv");
        assertThat(fileNameByLocale.get("ru-RU")).isEqualTo("test-questions_ru.csv");
    }

    @Test
    void shouldReturnCorrectFileNameForLocale() {
        assertThat(appProperties.getTestFileName()).isEqualTo("test-questions_ru.csv");
    }
}