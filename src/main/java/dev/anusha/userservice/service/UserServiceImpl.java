package dev.anusha.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.anusha.userservice.dto.SendEmailDto;
import dev.anusha.userservice.exception.UserAlreadyExistsException;
import dev.anusha.userservice.exception.UserNotFoundException;
import dev.anusha.userservice.model.Token;
import dev.anusha.userservice.model.User;
import dev.anusha.userservice.repository.TokenRepository;
import dev.anusha.userservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    // private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // The above line generates a new secret key for each application run.
    // So it becomes a problem if the application restarts, as the tokens generated before the restart will not be valid anymore.
    // To avoid this we use a fixed secret key.
    // private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("Secret_key_string".getBytes(StandardCharsets.UTF_8));
    // Instead of keeping the secret key here we can keep it in a config class of its own and reuse it across the application.
    private static final long EXPIRATION_TIME_IN_MS = 10L * 60 * 60 * 1000; // 10 hours in milliseconds
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    UserRepository userRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    TokenRepository tokenRepository;
    SecretKey secretKey;

    public UserServiceImpl(UserRepository userRepository, TokenRepository tokenRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SecretKey secretKey, ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) { //
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepository = tokenRepository;
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public User signup(String name, String email, String password) throws UserAlreadyExistsException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()) {
            throw new UserAlreadyExistsException("User with this email ("+email+") already exists, Try logging in instead.");
            // return null;
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        SendEmailDto sendEmailDto = new SendEmailDto();
        sendEmailDto.setFrom("nithinbommerla99@gmail.com");
        sendEmailDto.setTo(email);
        sendEmailDto.setSubject("Welcome to User Service");
        sendEmailDto.setBody("Hello " + name + ",\n\nThank you for signing up for our service. We are excited to have you on board!\n\nBest regards,\nUser Service Team");

        String sendEmailDtoString;
        try {
            sendEmailDtoString = objectMapper.writeValueAsString(sendEmailDto);
        } catch (Exception e) {
            throw new RuntimeException("Error while converting SendEmailDto to JSON", e);
        }

        kafkaTemplate.send("send-email-topic", sendEmailDtoString);
        return userRepository.save(user);
    }

    @Override
    public Token login(String email, String password) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()) {
            throw new UserNotFoundException("User with this email ("+email+") does not exist, please sign up first.");
        }
        User user = userOptional.get();
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Incorrect password for user with email ("+email+").");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_IN_MS);

        Map<String, Object> claims = new HashMap<>(); // JWT contains key-value pairs and values can be of any type so using object as value
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        String jsonString = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256) // SECRET_KEY
                .compact();

        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(jsonString);
        token.setExpiresAt(expiryDate);
        return token;

        /*
        // OLD WAY: Custom Token Generation
        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);
        Date date = calendar.getTime();

        token.setExpiresAt(date);
        return tokenRepository.save(token);
         */
    }

    @Override
    public User validateToken(String tokenValue) throws UserNotFoundException {
        // We will validate JWT Token here.
        if(tokenValue == null || tokenValue.isEmpty()) {
            throw new UserNotFoundException("Token is null or empty.");
        }
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // SECRET_KEY
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new UserNotFoundException("Token is expired.");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new UserNotFoundException("Invalid token.");
        }
        String email = claims.getSubject();
        if(email == null || email.isEmpty()) {
            throw new UserNotFoundException("Invalid token: No subject found.");
        }
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()) {
            throw new UserNotFoundException("User with this email ("+email+") does not exist.");
        }
        return userOptional.get();
    }


    @Override
    public void logout(String tokenValue){
        //TODO: Implement logout functionality
    }

    private void logoutUsingTokenInDB(String tokenValue){
        /*
        Logout is simply deleting the token from the database.
        This will make the token invalid for future requests.
         */
        Optional<Token> tokenOptional = tokenRepository.findByTokenValueAndIsDeleted(tokenValue, false);
        if(tokenOptional.isPresent()) {
            Token token = tokenOptional.get();
            token.setDeleted(true); // soft delete
            tokenRepository.save(token);
        }
        // else, do nothing as the token is already deleted or does not exist
    }

    private User validateTokenInDB(String tokenValue) throws UserNotFoundException {
        /*
        Valid Token: 1. should exist in the database
                     2. should not be expired
                     3. should not be deleted
         */
    Optional<Token> tokenOptional = tokenRepository.findByTokenValueAndIsDeletedAndExpiresAtGreaterThan
            (tokenValue, false, new Date());
    if(tokenOptional.isEmpty()) {
        // return null;
        throw new UserNotFoundException("Invalid or expired token.");
    }
    Token token = tokenOptional.get();
    return token.getUser();
    }
}