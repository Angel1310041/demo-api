package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_personales")
public class UsuarioPersonal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Column(name = "nombre_completo", nullable = false, length = 150)
	private String nombreCompleto;

	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@Column(length = 30)
	private String telefono;

	@Column(length = 250)
	private String domicilio;

	@Column(nullable = false)
	private boolean oposicionAlTratamiento;

	public UsuarioPersonal() { }

	public Long getId() { return id; }
	public String getUsername() { return username; }
	public String getPasswordHash() { return passwordHash; }
	public String getNombreCompleto() { return nombreCompleto; }
	public String getEmail() { return email; }
	public String getTelefono() { return telefono; }
	public String getDomicilio() { return domicilio; }
	public boolean isOposicionAlTratamiento() { return oposicionAlTratamiento; }
	public void setUsername(String username) { this.username = username; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
	public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
	public void setEmail(String email) { this.email = email; }
	public void setTelefono(String telefono) { this.telefono = telefono; }
	public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
	public void setOposicionAlTratamiento(boolean oposicionAlTratamiento) { this.oposicionAlTratamiento = oposicionAlTratamiento; }
}