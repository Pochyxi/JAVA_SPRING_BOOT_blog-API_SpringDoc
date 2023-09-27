package com.developez.Spring.boot.blog.API.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        description = "Modello di dati per l'invio di una richiesta di login"
)
public class LoginDto {

    @Schema(
            description = "Username o Email dell'utente"
    )
    private String usernameOrEmail;

    @Schema(
            description = "Password dell'utente"
    )
    private String password;
}
