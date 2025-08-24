package src.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "hello_world")
@Getter
@Setter
public class HelloWorld implements Serializable {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "hello")
    private String hello;
}
