package com.example.demo;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/productos")
public class ProductoController {

	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;
	private final ProveedorRepository proveedorRepository;

	public ProductoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, ProveedorRepository proveedorRepository) {
		this.productoRepository = productoRepository;
		this.categoriaRepository = categoriaRepository;
		this.proveedorRepository = proveedorRepository;
	}

	@GetMapping
	public List<ProductoResponse> listar() { return productoRepository.findAll().stream().map(ProductoResponse::from).toList(); }

	@GetMapping("/{id}")
	public ProductoResponse obtener(@PathVariable Long id) { return ProductoResponse.from(buscar(id)); }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
		return ProductoResponse.from(productoRepository.save(aplicar(new Producto(), request)));
	}

	@PutMapping("/{id}")
	@Transactional
	public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
		return ProductoResponse.from(productoRepository.save(aplicar(buscar(id), request)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id) { productoRepository.delete(buscar(id)); }

	private Producto aplicar(Producto producto, ProductoRequest request) {
		producto.setNombre(request.nombre());
		producto.setDescripcion(request.descripcion());
		producto.setPrecio(request.precio());
		producto.setStock(request.stock());
		producto.setCategoria(categoriaRepository.findById(request.categoriaId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada")));
		producto.setProveedor(proveedorRepository.findById(request.proveedorId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado")));
		return producto;
	}

	private Producto buscar(Long id) {
		return productoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
	}

	public record ProductoRequest(@NotBlank @Size(max = 150) String nombre, @Size(max = 500) String descripcion,
			@NotNull @DecimalMin("0.00") BigDecimal precio, @NotNull @PositiveOrZero Integer stock,
			@NotNull Long categoriaId, @NotNull Long proveedorId) { }

	public record ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock,
			Long categoriaId, String categoria, Long proveedorId, String proveedor) {
		static ProductoResponse from(Producto producto) {
			return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(), producto.getPrecio(),
					producto.getStock(), producto.getCategoria().getId(), producto.getCategoria().getNombre(),
					producto.getProveedor().getId(), producto.getProveedor().getNombre());
		}
	}
}