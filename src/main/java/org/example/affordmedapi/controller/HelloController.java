package org.example.affordmedapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.example.affordmedapi.model.Student;
@RestController
public class HelloController {

    @GetMapping("/student")
    public Student getStudent() {
        return new Student(101,"NAITIK");
    }
    @GetMapping("/user/{id}")
    public String getUser(@PathVariable int id) {
        return "User Id = " + id;
    }

    @GetMapping("/name")
    public String name() {
        return "Naitik Tayal";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello Naitik";
    }

}