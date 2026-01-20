package dev.anusha.userservice.controller;

import dev.anusha.userservice.dto.*;
import dev.nithin.userservice.dto.*;
import dev.anusha.userservice.exception.UserAlreadyExistsException;
import dev.anusha.userservice.exception.UserNotFoundException;
import dev.anusha.userservice.model.Token;
import dev.anusha.userservice.model.User;
import dev.anusha.userservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(@RequestBody SignUpRequestDto signUpRequestDto) throws UserAlreadyExistsException {
        User user = userService.signup(signUpRequestDto.getName(),
                signUpRequestDto.getEmail(), signUpRequestDto.getPassword());
        return new ResponseEntity<>(SignUpResponseDto.from(user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) throws UserNotFoundException {
        Token token = userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return new ResponseEntity<>(LoginResponseDto.from(token), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto logoutRequestDto) throws UserNotFoundException {
        userService.logout(logoutRequestDto.getToken());
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws UserNotFoundException {
        if(token.startsWith("Bearer ")) {
            // token = token.substring(7); // Remove "Bearer " prefix
            token = token.replace("Bearer ", "");
        }
        User user = userService.validateToken(token);
        ResponseEntity<Boolean> responseEntity;
        if (user != null) {
            responseEntity = ResponseEntity.ok(true);
        } else {
            responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        return responseEntity;
    }
}
