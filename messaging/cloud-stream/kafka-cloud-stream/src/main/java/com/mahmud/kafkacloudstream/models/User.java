package com.mahmud.kafkacloudstream.models;

public class User {
    private String name;
    private int age;

    // Default constructor (required for deserialization)
    public User() {}

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getters, setters, and toString
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    @Override
    public String toString() { return "User{name='" + name + "', age=" + age + "}"; }
}