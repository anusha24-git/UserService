package dev.anusha.userservice.dto;

import dev.anusha.userservice.model.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignUpResponseDto {
    private String name;
    private String email;
    private String password;

    public static SignUpResponseDto from(User user) {
        if(user == null) return null;
        SignUpResponseDto responseDto = new SignUpResponseDto();
        responseDto.setName(user.getName());
        responseDto.setEmail(user.getEmail());
        responseDto.setPassword(user.getPassword());
        return responseDto;
    }
}
