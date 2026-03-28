package ru.otus.hw.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Команды для работы с книгами")
@SpringBootTest
@TestPropertySource(properties = "spring.shell.interactive.enabled=false")
class BookCommandsTest {

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookConverter bookConverter;

    @Autowired
    private BookCommands bookCommands;

    private Author author;
    private Genre genre;
    private Book book1;
    private Book book2;
    private String book1String;
    private String book2String;

    @BeforeEach
    void setUp() {
        author = new Author(1L, "Author_1");
        genre = new Genre(1L, "Genre_1");
        book1 = new Book(1L, "BookTitle_1", author, genre);
        book2 = new Book(2L, "BookTitle_2", author, genre);

        book1String = "Id: 1, title: BookTitle_1, author: {Id: 1, FullName: Author_1}, genres: [Id: 1, Name: Genre_1]";
        book2String = "Id: 2, title: BookTitle_2, author: {Id: 1, FullName: Author_1}, genres: [Id: 1, Name: Genre_1]";
    }

    @Test
    @DisplayName("должен возвращать все книги в виде строки")
    void findAllBooks_ShouldReturnAllBooks() {
        List<Book> books = List.of(book1, book2);
        when(bookService.findAll()).thenReturn(books);
        when(bookConverter.bookToString(book1)).thenReturn(book1String);
        when(bookConverter.bookToString(book2)).thenReturn(book2String);

        String result = bookCommands.findAllBooks();

        assertThat(result).isEqualTo(book1String + "," + System.lineSeparator() + book2String);
        verify(bookService).findAll();
    }

    @Test
    @DisplayName("должен возвращать пустую строку, если книг нет")
    void findAllBooks_ShouldReturnEmptyString_WhenNoBooks() {
        when(bookService.findAll()).thenReturn(List.of());

        String result = bookCommands.findAllBooks();

        assertThat(result).isEmpty();
        verify(bookService).findAll();
    }

    @Test
    @DisplayName("должен находить книгу по id")
    void findBookById_ShouldReturnBook_WhenExists() {
        when(bookService.findById(1L)).thenReturn(Optional.of(book1));
        when(bookConverter.bookToString(book1)).thenReturn(book1String);

        String result = bookCommands.findBookById(1L);

        assertThat(result).isEqualTo(book1String);
        verify(bookService).findById(1L);
    }

    @Test
    @DisplayName("должен возвращать сообщение об ошибке, если книга не найдена")
    void findBookById_ShouldReturnNotFoundMessage_WhenNotExists() {
        when(bookService.findById(999L)).thenReturn(Optional.empty());

        String result = bookCommands.findBookById(999L);

        assertThat(result).isEqualTo("Book with id 999 not found");
        verify(bookService).findById(999L);
    }

    @Test
    @DisplayName("должен вставлять новую книгу")
    void insertBook_ShouldInsertAndReturnBook() {
        when(bookService.insert("New Book", 1L, 1L)).thenReturn(book1);
        when(bookConverter.bookToString(book1)).thenReturn(book1String);

        String result = bookCommands.insertBook("New Book", 1L, 1L);

        assertThat(result).isEqualTo(book1String);
        verify(bookService).insert("New Book", 1L, 1L);
    }

    @Test
    @DisplayName("должен обновлять книгу")
    void updateBook_ShouldUpdateAndReturnBook() {
        when(bookService.update(1L, "Updated Book", 1L, 1L)).thenReturn(book1);
        when(bookConverter.bookToString(book1)).thenReturn(book1String);

        String result = bookCommands.updateBook(1L, "Updated Book", 1L, 1L);

        assertThat(result).isEqualTo(book1String);
        verify(bookService).update(1L, "Updated Book", 1L, 1L);
    }

    @Test
    @DisplayName("должен удалять книгу по id")
    void deleteBook_ShouldDeleteBook() {
        doNothing().when(bookService).deleteById(1L);

        bookCommands.deleteBook(1L);

        verify(bookService).deleteById(1L);
    }
}