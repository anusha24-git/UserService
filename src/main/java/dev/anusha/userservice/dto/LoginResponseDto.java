package dev.anusha.userservice.dto;

import dev.anusha.userservice.model.Token;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponseDto {
    private String token;

    public static LoginResponseDto from(Token token) {
        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token.getTokenValue());
        return response;
    }
}
