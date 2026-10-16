package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:inventario-test;DB_CLOSE_DELAY=-1")
class DemoApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void crudRespetaRelacionesYRestricciones() throws Exception {
		mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Periféricos\",\"descripcion\":\"Accesorios\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
		mockMvc.perform(put("/api/categorias/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Periféricos y audio\",\"descripcion\":\"Accesorios\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Periféricos y audio"));
		mockMvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Tech SA\",\"email\":\"ventas@tech.test\",\"telefono\":\"5551234\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
		mockMvc.perform(put("/api/proveedores/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Tech Global\",\"email\":\"ventas@tech.test\",\"telefono\":\"5551234\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Tech Global"));
		mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(productoJson("Teclado", 1, 1)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.categoria").value("Periféricos y audio"));
		mockMvc.perform(get("/api/productos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.proveedor").value("Tech Global"));
		mockMvc.perform(get("/api/categorias/1")).andExpect(status().isOk());
		mockMvc.perform(get("/api/proveedores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Tech Global"));
		mockMvc.perform(delete("/api/categorias/1")).andExpect(status().isConflict());
		mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(productoJson("Inválido", 999, 1)))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\" \"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON).content(productoJson("Teclado mecánico", 1, 1)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Teclado mecánico"));
		mockMvc.perform(delete("/api/productos/1")).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/categorias/1")).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/proveedores/1")).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/productos/999")).andExpect(status().isNotFound());
	}

	private String productoJson(String nombre, long categoriaId, long proveedorId) {
		return "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Uso general\",\"precio\":25.50,\"stock\":10,\"categoriaId\":"
				+ categoriaId + ",\"proveedorId\":" + proveedorId + "}";
	}
}
