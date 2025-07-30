package org.project.cloudfilestorage.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRequestDTO {

  @NotBlank(message = "Username не должен быть пустым")
  @Size(min = 6, max = 24, message = "Username должен быть минимум 6 символов")
  public String username;
  @NotBlank(message = "Username не должен быть пустым")
  @Size(min = 6, max = 1000, message = "Пароль должен быть минимум 6 символов")
  public String password;

}
