package dev.anusha.userservice.service;

import dev.anusha.userservice.exception.UserAlreadyExistsException;
import dev.anusha.userservice.exception.UserNotFoundException;
import dev.anusha.userservice.model.Token;
import dev.anusha.userservice.model.User;

public interface UserService {
    public User signup(String name, String email, String password) throws UserAlreadyExistsException;

    public Token login(String email, String password) throws UserNotFoundException;

    public void logout(String tokenValue);

    public User validateToken(String tokenValue) throws UserNotFoundException;
}
