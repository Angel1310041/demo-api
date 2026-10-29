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
@RequestMapping("/api/marcas")
public class MarcaController {

	private final MarcaRepository repository;

	public MarcaController(MarcaRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<Marca> listar() {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	public Marca obtener(@PathVariable Long id) {
		return buscar(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Marca crear(@Valid @RequestBody MarcaRequest request) {
		Marca marca = new Marca();
		marca.setNombre(request.nombre());
		marca.setDescripcion(request.descripcion());
		return repository.save(marca);
	}

	@PutMapping("/{id}")
	public Marca actualizar(@PathVariable Long id, @Valid @RequestBody MarcaRequest request) {
		Marca marca = buscar(id);
		marca.setNombre(request.nombre());
		marca.setDescripcion(request.descripcion());
		return repository.save(marca);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id) {
		repository.delete(buscar(id));
	}

	private Marca buscar(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
	}

	public record MarcaRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 500) String descripcion) { }
}
