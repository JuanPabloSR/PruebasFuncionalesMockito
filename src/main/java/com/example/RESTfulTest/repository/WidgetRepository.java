package com.example.RESTfulTest.repository;

import com.example.RESTfulTest.model.Widget;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WidgetRepository extends MongoRepository<Widget, Long> {
}
