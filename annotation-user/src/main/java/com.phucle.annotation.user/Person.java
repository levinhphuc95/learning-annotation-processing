package com.phucle.annotation.user;

import com.phucle.annotation.processing.Builder;
import com.phucle.annotation.processing.NotNull;

import java.time.LocalDate;

@Builder
public class Person {
    private int age;
    private String name;

    private LocalDate dateOfBirth;

    public Person(int age, @NotNull String name, LocalDate dateOfBirth) {
        this.age = age;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString() {
        return String.format("Person %s, age %s, dateOfBirth %s", name, age, dateOfBirth);
    }
}
