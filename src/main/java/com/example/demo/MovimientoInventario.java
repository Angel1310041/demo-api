package com.example.demo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TipoMovimiento tipo;

	@NotNull
	@Positive
	@Column(nullable = false)
	private Integer cantidad;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime fecha;

	@Size(max = 500)
	@Column(length = 500)
	private String observacion;

	public MovimientoInventario() { }
	public Long getId() { return id; }
	public Producto getProducto() { return producto; }
	public TipoMovimiento getTipo() { return tipo; }
	public Integer getCantidad() { return cantidad; }
	public LocalDateTime getFecha() { return fecha; }
	public String getObservacion() { return observacion; }
	public void setProducto(Producto producto) { this.producto = producto; }
	public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
	public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
	public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
	public void setObservacion(String observacion) { this.observacion = observacion; }
}
