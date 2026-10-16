package com.example.demo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "productos")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 150)
	@Column(nullable = false, length = 150)
	private String nombre;

	@Size(max = 500)
	@Column(length = 500)
	private String descripcion;

	@NotNull
	@DecimalMin("0.00")
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal precio;

	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	private Integer stock;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "proveedor_id", nullable = false)
	private Proveedor proveedor;

	public Producto() { }
	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public BigDecimal getPrecio() { return precio; }
	public Integer getStock() { return stock; }
	public Categoria getCategoria() { return categoria; }
	public Proveedor getProveedor() { return proveedor; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public void setPrecio(BigDecimal precio) { this.precio = precio; }
	public void setStock(Integer stock) { this.stock = stock; }
	public void setCategoria(Categoria categoria) { this.categoria = categoria; }
	public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
}