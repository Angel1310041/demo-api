package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioPersonalRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtEncoder jwtEncoder;

	public AuthController(UsuarioPersonalRepository repository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtEncoder = jwtEncoder;
	}

	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public synchronized UsuarioPersonalController.PerfilResponse registrar(@Valid @RequestBody RegistroRequest request) {
		if (repository.count() > 0) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El registro inicial ya se realizó");
		}

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
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));
		} catch (AuthenticationException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
		}

		UsuarioPersonal usuario = repository.findByUsername(request.username())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(30, ChronoUnit.MINUTES);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("demo-api")
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(usuario.getUsername())
				.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
		return new LoginResponse("Autenticación correcta", "Bearer", token, expiresAt,
				UsuarioPersonalController.PerfilResponse.from(usuario));
	}

	public record RegistroRequest(@NotBlank @Size(max = 40) String username,
			@NotBlank @Size(min = 12, max = 72) String password,
			@NotBlank @Size(max = 150) String nombreCompleto,
			@NotBlank @Email @Size(max = 160) String email,
			@Size(max = 30) String telefono,
			@Size(max = 250) String domicilio) { }

	public record LoginRequest(@NotBlank @Size(max = 40) String username,
			@NotBlank @Size(max = 72) String password) { }

	public record LoginResponse(String mensaje, String tipoToken, String accessToken, Instant expiraEn,
			UsuarioPersonalController.PerfilResponse usuario) { }
}