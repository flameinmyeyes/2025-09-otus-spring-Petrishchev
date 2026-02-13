package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            displayQuestion(question);
            int userAnswer = readUserAnswer(question.answers().size());
            boolean isAnswerValid = checkAnswer(question, userAnswer);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private void displayQuestion(Question question) {
        ioService.printLine(question.text());
        List<Answer> answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d. %s", i + 1, answers.get(i).text());
        }
        ioService.printLine("");
    }

    private int readUserAnswer(int maxAnswerNumber) {
        return ioService.readIntForRangeWithPrompt(
                1,
                maxAnswerNumber,
                "Enter the number of your answer: ",
                String.format("Please enter a number between 1 and %d", maxAnswerNumber)
        );
    }

    private boolean checkAnswer(Question question, int userAnswerIndex) {
        List<Answer> answers = question.answers();
        if (userAnswerIndex >= 1 && userAnswerIndex <= answers.size()) {
            return answers.get(userAnswerIndex - 1).isCorrect();
        }
        return false;
    }
}