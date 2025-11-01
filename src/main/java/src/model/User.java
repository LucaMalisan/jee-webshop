package src.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_data")
public class User implements Serializable {

  @Id
  @Column(name = "email")
  private String email;

  @Column(name = "confirm_key")
  private String confirmKey;

  @Column(name = "confirmed")
  private boolean confirmed;

  public User() {}

  public User(String email, String confirmKey, boolean confirmed) {
    this.email = email;
    this.confirmKey = confirmKey;
    this.confirmed = confirmed;
  }
}
