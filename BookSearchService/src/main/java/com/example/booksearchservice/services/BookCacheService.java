package com.example.booksearchservice.services;

import com.example.booksearchservice.repositories.BookRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookCacheService {

    @Autowired
    BookRepository bookRepo;

    public void cache(String worksId, String jsonStr) {
        bookRepo.save(worksId, jsonStr);
    }

    public String get(String worksId) {
        Optional<String> opt = bookRepo.get(worksId);
        return opt.get();
    }

    public boolean hasKey(String key) {
        Optional<String> opt = bookRepo.get(key);
        return opt.isPresent();
    }
}
