package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
	@Override
	@EntityGraph(attributePaths = {"categoria", "proveedor"})
	List<Producto> findAll();

	@Override
	@EntityGraph(attributePaths = {"categoria", "proveedor"})
	Optional<Producto> findById(Long id);
}