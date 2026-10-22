package com.example.demo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioPersonalRepository extends JpaRepository<UsuarioPersonal, Long> {
	Optional<UsuarioPersonal> findByUsername(String username);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
}