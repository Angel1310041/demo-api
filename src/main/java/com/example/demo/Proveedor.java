package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "proveedores")
public class Proveedor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 120)
	@Column(nullable = false, length = 120)
	private String nombre;

	@Email
	@Size(max = 160)
	@Column(unique = true, length = 160)
	private String email;

	@Size(max = 30)
	@Column(length = 30)
	private String telefono;

	public Proveedor() { }
	public Proveedor(String nombre, String email, String telefono) { this.nombre = nombre; this.email = email; this.telefono = telefono; }
	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getEmail() { return email; }
	public String getTelefono() { return telefono; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setEmail(String email) { this.email = email; }
	public void setTelefono(String telefono) { this.telefono = telefono; }
}