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
@Table(name = "user_email_confirmed")
public class UserEmailConfirmed implements Serializable {

  @Id
  @Column(name = "email")
  private String email;

  @Column(name = "confirm_key")
  private String confirmKey;

  @Column(name = "confirmed")
  private boolean confirmed;
}
