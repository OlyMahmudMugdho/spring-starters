package com.mahmud.redispostgres.service;

import com.mahmud.redispostgres.model.User;
import com.mahmud.redispostgres.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Marks this class as a service component in Spring.
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Fetches all users from the database and caches the result in Redis.
     * @return A list of all users.
     */
    @Cacheable(value = "users", key = "'allUsers'") // Caches the result under the key "users::allUsers".
    public List<User> getAllUsers() {
        System.out.println("Fetching users from the database..."); // Logs when the database is queried.
        return userRepository.findAll(); // Fetches all users from PostgreSQL.
    }

    /**
     * Creates a new user and saves it to the database.
     * Invalidates the "allUsers" cache after creating a user.
     * @param user The user to create.
     * @return The newly created user.
     */
    @CacheEvict(value = "users", key = "'allUsers'") // Invalidates the cache after creating a user.
    public User createUser(User user) {
        return userRepository.save(user); // Saves the user to PostgreSQL.
    }

    /**
     * Fetches a single user by ID and caches the result if the ID is greater than 10.
     * @param id The ID of the user to fetch.
     * @return The user if found, or null if not found.
     */
    @Cacheable(value = "users", key = "#id", condition = "#id > 10") // Caches the result only if the ID is greater than 10.
    public User getUserById(Long id) {
        System.out.println("Fetching user with ID " + id + " from the database...");
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Updates a user's data in the database and updates the corresponding cache entry.
     * @param id The ID of the user to update.
     * @param updatedUser The updated user data.
     * @return The updated user.
     */
    @CachePut(value = "users", key = "#id") // Updates the cache with the result of this method.
    public User updateUser(Long id, User updatedUser) {
        System.out.println("Updating user with ID " + id + " in the database...");
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Update the user's fields
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());

        // Save the updated user to the database
        return userRepository.save(existingUser);
    }

    /**
     * Deletes a user from the database and removes the corresponding cache entry.
     * @param id The ID of the user to delete.
     */
    @CacheEvict(value = "users", key = "#id") // Removes the cache entry for the deleted user.
    public void deleteUser(Long id) {
        System.out.println("Deleting user with ID " + id + " from the database...");
        userRepository.deleteById(id); // Deletes the user from PostgreSQL.
    }
}