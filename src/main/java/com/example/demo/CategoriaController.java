package com.example.demo;

import java.util.List;

import jakarta.validation.Valid;
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
@RequestMapping("/api/categorias")
public class CategoriaController {
	private final CategoriaRepository repository;
	public CategoriaController(CategoriaRepository repository) { this.repository = repository; }
	@GetMapping public List<Categoria> listar() { return repository.findAll(); }
	@GetMapping("/{id}") public Categoria obtener(@PathVariable Long id) { return buscar(id); }
	@PostMapping @ResponseStatus(HttpStatus.CREATED)
	public Categoria crear(@Valid @RequestBody CategoriaRequest request) { return repository.save(new Categoria(request.nombre(), request.descripcion())); }
	@PutMapping("/{id}")
	public Categoria actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
		Categoria categoria = buscar(id);
		categoria.setNombre(request.nombre());
		categoria.setDescripcion(request.descripcion());
		return repository.save(categoria);
	}
	@DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id) { repository.delete(buscar(id)); }
	private Categoria buscar(Long id) {
		return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
	}
	public record CategoriaRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 500) String descripcion) { }
}