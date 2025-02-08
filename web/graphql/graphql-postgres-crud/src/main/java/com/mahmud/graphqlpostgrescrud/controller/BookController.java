package com.mahmud.graphqlpostgrescrud.controller;

import com.mahmud.graphqlpostgrescrud.model.Book;
import com.mahmud.graphqlpostgrescrud.service.BookService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @QueryMapping
    public List<Book> findAllBooks() {
        return bookService.findAll();
    }

    @QueryMapping
    public Book findBookById(@Argument Long id) {
        return bookService.findById(id);
    }

    @MutationMapping
    public Book createBook(@Argument BookInput bookInput) {
        Book book = new Book();
        book.setTitle(bookInput.title());
        book.setAuthor(bookInput.author());
        book.setPrice(bookInput.price());
        return bookService.save(book);
    }

    @MutationMapping
    public Book updateBook(@Argument Long id, @Argument BookInput bookInput) {
        Book book = new Book();
        book.setTitle(bookInput.title());
        book.setAuthor(bookInput.author());
        book.setPrice(bookInput.price());
        return bookService.update(id, book);
    }

    @MutationMapping
    public Boolean deleteBook(@Argument Long id) {
        bookService.deleteById(id);
        return true;
    }

    public record BookInput(String title, String author, Double price) {}
}
