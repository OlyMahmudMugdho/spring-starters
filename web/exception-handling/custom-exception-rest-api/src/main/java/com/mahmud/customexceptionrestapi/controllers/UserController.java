package com.mahmud.customexceptionrestapi.controllers;

import com.mahmud.customexceptionrestapi.exceptions.UserNotFoundException;
import com.mahmud.customexceptionrestapi.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping(value = {"/api/users","/api/users/"})
public class UserController {
    private static final ArrayList<User> users = new ArrayList<>();

    public UserController() {
        users.add(new User(1, "John", true));
        users.add(new User(2, "Mary", false));
        users.add(new User(3, "Mike", true));
        users.add(new User(4, "Jane", false));
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        users.add(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<ArrayList<User>> getUsers() {
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        Optional<User> user = users.stream().filter(usr -> usr.getId()==id)
                .findFirst();
        if (user.isEmpty()) throw new UserNotFoundException("User does not exist");
        return ResponseEntity.ok(user.get());
    }
}
