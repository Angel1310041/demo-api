package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/arco")
public class UsuarioPersonalController {

	private final UsuarioPersonalRepository repository;

	public UsuarioPersonalController(UsuarioPersonalRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/perfil")
	public PerfilResponse acceder(Authentication authentication) {
		return PerfilResponse.from(buscarUsuario(authentication));
	}

	@PutMapping("/perfil")
	public PerfilResponse rectificar(Authentication authentication, @Valid @RequestBody RectificacionRequest request) {
		UsuarioPersonal usuario = buscarUsuario(authentication);
		String email = request.email().trim().toLowerCase();
		if (!usuario.getEmail().equals(email) && repository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
		}
		usuario.setNombreCompleto(request.nombreCompleto().trim());
		usuario.setEmail(email);
		usuario.setTelefono(request.telefono());
		usuario.setDomicilio(request.domicilio());
		return PerfilResponse.from(repository.save(usuario));
	}

	@DeleteMapping("/perfil")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelar(Authentication authentication) {
		repository.delete(buscarUsuario(authentication));
	}

	@PatchMapping("/oposicion")
	public OposicionResponse oponerse(Authentication authentication, @Valid @RequestBody OposicionRequest request) {
		UsuarioPersonal usuario = buscarUsuario(authentication);
		usuario.setOposicionAlTratamiento(request.oposicionAlTratamiento());
		repository.save(usuario);
		return new OposicionResponse(usuario.isOposicionAlTratamiento());
	}

	private UsuarioPersonal buscarUsuario(Authentication authentication) {
		return repository.findByUsername(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
	}

	public record RectificacionRequest(@NotBlank @Size(max = 150) String nombreCompleto,
			@NotBlank @Email @Size(max = 160) String email,
			@Size(max = 30) String telefono,
			@Size(max = 250) String domicilio) { }

	public record OposicionRequest(boolean oposicionAlTratamiento) { }
	public record OposicionResponse(boolean oposicionAlTratamiento) { }

	public record PerfilResponse(Long id, String username, String nombreCompleto, String email,
			String telefono, String domicilio, boolean oposicionAlTratamiento) {
		static PerfilResponse from(UsuarioPersonal usuario) {
			return new PerfilResponse(usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(),
					usuario.getEmail(), usuario.getTelefono(), usuario.getDomicilio(),
					usuario.isOposicionAlTratamiento());
		}
	}
}