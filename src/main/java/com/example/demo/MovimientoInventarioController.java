package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {

	private final MovimientoInventarioRepository movimientoRepository;
	private final ProductoRepository productoRepository;

	public MovimientoInventarioController(MovimientoInventarioRepository movimientoRepository,
			ProductoRepository productoRepository) {
		this.movimientoRepository = movimientoRepository;
		this.productoRepository = productoRepository;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<MovimientoResponse> listar() {
		return movimientoRepository.findAll().stream().map(MovimientoResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public MovimientoResponse obtener(@PathVariable Long id) {
		return MovimientoResponse.from(buscar(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public MovimientoResponse crear(@Valid @RequestBody MovimientoRequest request) {
		Producto producto = buscarProducto(request.productoId());
		aplicarAjuste(producto, request.tipo(), request.cantidad(), false);

		MovimientoInventario movimiento = new MovimientoInventario();
		aplicarDatos(movimiento, producto, request);
		return MovimientoResponse.from(movimientoRepository.save(movimiento));
	}

	@PutMapping("/{id}")
	@Transactional
	public MovimientoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoRequest request) {
		MovimientoInventario movimiento = buscar(id);
		Producto productoAnterior = movimiento.getProducto();
		aplicarAjuste(productoAnterior, movimiento.getTipo(), movimiento.getCantidad(), true);

		Producto productoNuevo = buscarProducto(request.productoId());
		aplicarAjuste(productoNuevo, request.tipo(), request.cantidad(), false);
		aplicarDatos(movimiento, productoNuevo, request);
		return MovimientoResponse.from(movimientoRepository.save(movimiento));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void eliminar(@PathVariable Long id) {
		MovimientoInventario movimiento = buscar(id);
		aplicarAjuste(movimiento.getProducto(), movimiento.getTipo(), movimiento.getCantidad(), true);
		movimientoRepository.delete(movimiento);
	}

	private void aplicarDatos(MovimientoInventario movimiento, Producto producto, MovimientoRequest request) {
		movimiento.setProducto(producto);
		movimiento.setTipo(request.tipo());
		movimiento.setCantidad(request.cantidad());
		movimiento.setFecha(request.fecha() == null ? LocalDateTime.now() : request.fecha());
		movimiento.setObservacion(request.observacion());
	}

	private void aplicarAjuste(Producto producto, TipoMovimiento tipo, int cantidad, boolean invertir) {
		long cambio = tipo == TipoMovimiento.ENTRADA ? cantidad : -((long) cantidad);
		if (invertir) {
			cambio = -cambio;
		}
		long stockNuevo = producto.getStock() + cambio;
		if (stockNuevo < 0 || stockNuevo > Integer.MAX_VALUE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El movimiento dejaría el stock fuera de rango");
		}
		producto.setStock((int) stockNuevo);
	}

	private MovimientoInventario buscar(Long id) {
		return movimientoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimiento no encontrado"));
	}

	private Producto buscarProducto(Long id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
	}

	public record MovimientoRequest(@NotNull Long productoId, @NotNull TipoMovimiento tipo, @NotNull @Positive Integer cantidad,
			LocalDateTime fecha, @Size(max = 500) String observacion) { }

	public record MovimientoResponse(Long id, Long productoId, String producto, TipoMovimiento tipo, Integer cantidad,
			LocalDateTime fecha, String observacion) {
		static MovimientoResponse from(MovimientoInventario movimiento) {
			return new MovimientoResponse(movimiento.getId(), movimiento.getProducto().getId(),
					movimiento.getProducto().getNombre(), movimiento.getTipo(), movimiento.getCantidad(),
					movimiento.getFecha(), movimiento.getObservacion());
		}
	}
}
