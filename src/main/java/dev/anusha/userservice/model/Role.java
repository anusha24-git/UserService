package dev.anusha.userservice.model;


import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Role extends Base{
    private String value;
}
