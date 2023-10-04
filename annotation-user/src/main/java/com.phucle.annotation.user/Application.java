package com.phucle.annotation.user;

import com.phucle.annotation.user.build.PersonBuilder;

import java.time.LocalDate;

public class Application {
    public static void main(String[] args) {
        Person person = new PersonBuilder()
                .name("Phuc")
                .age(18)
                .dateOfBirth(
                        LocalDate.of(1995, 6, 11))
                .build();

        System.out.println(person);
    }
}
