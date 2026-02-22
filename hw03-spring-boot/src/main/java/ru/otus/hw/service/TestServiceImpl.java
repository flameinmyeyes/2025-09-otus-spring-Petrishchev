package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");

        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question : questions) {
            boolean isAnswerValid = processQuestion(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private boolean processQuestion(Question question) {
        printQuestion(question);
        int userAnswerIndex = ioService.readIntForRangeWithPromptLocalized(
                1,
                question.answers().size(),
                "TestService.answer.prompt",
                "TestService.error.range"
        );
        return checkAnswer(question, userAnswerIndex);
    }

    private void printQuestion(Question question) {
        ioService.printLine(question.text());
        List<Answer> answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d. %s", i + 1, answers.get(i).text());
        }
        ioService.printLine("");
    }

    private boolean checkAnswer(Question question, int answerIndex) {
        List<Answer> answers = question.answers();
        if (answerIndex >= 1 && answerIndex <= answers.size()) {
            return answers.get(answerIndex - 1).isCorrect();
        }
        return false;
    }
}