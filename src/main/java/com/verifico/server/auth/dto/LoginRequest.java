

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

  @Size(min = 3, max = 16, message = "Username must be 3–16 characters")
  private String username;

  @Email(message = "Email must be valid")
  private String email;

  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;
}