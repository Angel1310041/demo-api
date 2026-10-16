package com.example.demo;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
	private final ProveedorRepository repository;
	public ProveedorController(ProveedorRepository repository) { this.repository = repository; }
	@GetMapping public List<Proveedor> listar() { return repository.findAll(); }
	@GetMapping("/{id}") public Proveedor obtener(@PathVariable Long id) { return buscar(id); }
	@PostMapping @ResponseStatus(HttpStatus.CREATED)
	public Proveedor crear(@Valid @RequestBody ProveedorRequest request) { return repository.save(new Proveedor(request.nombre(), request.email(), request.telefono())); }
	@PutMapping("/{id}")
	public Proveedor actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequest request) {
		Proveedor proveedor = buscar(id);
		proveedor.setNombre(request.nombre());
		proveedor.setEmail(request.email());
		proveedor.setTelefono(request.telefono());
		return repository.save(proveedor);
	}
	@DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id) { repository.delete(buscar(id)); }
	private Proveedor buscar(Long id) {
		return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
	}
	public record ProveedorRequest(@NotBlank @Size(max = 120) String nombre, @Email @Size(max = 160) String email, @Size(max = 30) String telefono) { }
}