package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioPersonalRepository repository;
	private final PasswordEncoder passwordEncoder;

	public AuthController(UsuarioPersonalRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioPersonalController.PerfilResponse registrar(@Valid @RequestBody RegistroRequest request) {
		String username = request.username().trim().toLowerCase();
		String email = request.email().trim().toLowerCase();
		if (repository.existsByUsername(username) || repository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario o correo ya está registrado");
		}

		UsuarioPersonal usuario = new UsuarioPersonal();
		usuario.setUsername(username);
		usuario.setPasswordHash(passwordEncoder.encode(request.password()));
		usuario.setNombreCompleto(request.nombreCompleto().trim());
		usuario.setEmail(email);
		usuario.setTelefono(request.telefono());
		usuario.setDomicilio(request.domicilio());
		return UsuarioPersonalController.PerfilResponse.from(repository.save(usuario));
	}

	@PostMapping("/login")
	public LoginResponse login(Authentication authentication) {
		UsuarioPersonal usuario = repository.findByUsername(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		return new LoginResponse("Autenticación correcta", UsuarioPersonalController.PerfilResponse.from(usuario));
	}

	public record RegistroRequest(@NotBlank @Size(max = 40) String username,
			@NotBlank @Size(min = 12, max = 72) String password,
			@NotBlank @Size(max = 150) String nombreCompleto,
			@NotBlank @Email @Size(max = 160) String email,
			@Size(max = 30) String telefono,
			@Size(max = 250) String domicilio) { }

	public record LoginResponse(String mensaje, UsuarioPersonalController.PerfilResponse usuario) { }
}