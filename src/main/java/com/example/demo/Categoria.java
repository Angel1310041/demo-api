package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categorias")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, unique = true, length = 100)
	private String nombre;

	@Size(max = 500)
	@Column(length = 500)
	private String descripcion;

	public Categoria() { }
	public Categoria(String nombre, String descripcion) { this.nombre = nombre; this.descripcion = descripcion; }
	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getDescripcion() { return descripcion; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}