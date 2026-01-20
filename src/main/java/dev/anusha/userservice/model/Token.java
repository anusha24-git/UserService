package dev.anusha.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class Token extends Base {
    private String tokenValue;
    private Date expiresAt;
    @ManyToOne
    private User user;
}
