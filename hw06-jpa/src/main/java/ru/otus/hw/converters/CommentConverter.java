package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Comment;

@Component
public class CommentConverter {
    public String commentToString(Comment comment) {
        // book загружен через JOIN FETCH в репозитории, обращаться безопасно
        return "Id: %d, Text: %s, Book: %s".formatted(
                comment.getId(),
                comment.getText(),
                comment.getBook().getTitle());
    }
}