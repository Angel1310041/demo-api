package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:inventario-test;DB_CLOSE_DELAY=-1")
class DemoApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioPersonalRepository usuarioPersonalRepository;

	@BeforeEach
	void limpiarUsuariosDePrueba() {
		usuarioPersonalRepository.deleteAll();
	}

	@Test
	void crudRespetaRelacionesYRestricciones() throws Exception {
		registrarUsuarioInventario();
		performAsInventoryUser(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Periféricos\",\"descripcion\":\"Accesorios\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
		performAsInventoryUser(put("/api/categorias/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Periféricos y audio\",\"descripcion\":\"Accesorios\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Periféricos y audio"));
		performAsInventoryUser(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Tech SA\",\"email\":\"ventas@tech.test\",\"telefono\":\"5551234\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
		performAsInventoryUser(put("/api/proveedores/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Tech Global\",\"email\":\"ventas@tech.test\",\"telefono\":\"5551234\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Tech Global"));
		performAsInventoryUser(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(productoJson("Teclado", 1, 1)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.categoria").value("Periféricos y audio"));
		performAsInventoryUser(get("/api/productos/1")).andExpect(status().isOk()).andExpect(jsonPath("$.proveedor").value("Tech Global"));
		performAsInventoryUser(get("/api/categorias/1")).andExpect(status().isOk());
		performAsInventoryUser(get("/api/proveedores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Tech Global"));
		performAsInventoryUser(delete("/api/categorias/1")).andExpect(status().isConflict());
		performAsInventoryUser(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(productoJson("Inválido", 999, 1)))
				.andExpect(status().isNotFound());
		performAsInventoryUser(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\" \"}"))
				.andExpect(status().isBadRequest());
		performAsInventoryUser(put("/api/productos/1").contentType(MediaType.APPLICATION_JSON).content(productoJson("Teclado mecánico", 1, 1)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Teclado mecánico"));
		performAsInventoryUser(delete("/api/productos/1")).andExpect(status().isNoContent());
		performAsInventoryUser(delete("/api/categorias/1")).andExpect(status().isNoContent());
		performAsInventoryUser(delete("/api/proveedores/1")).andExpect(status().isNoContent());
		performAsInventoryUser(get("/api/productos/999")).andExpect(status().isNotFound());
	}

	@Test
	void marcasYMovimientosTienenCrudYActualizanElStock() throws Exception {
		registrarUsuarioInventario();
		long productoId = crearProductoInventario();

		performAsInventoryUser(post("/api/marcas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Acme\",\"descripcion\":\"Marca de prueba\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
		performAsInventoryUser(get("/api/marcas")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Acme"));
		performAsInventoryUser(get("/api/marcas/1")).andExpect(status().isOk());
		performAsInventoryUser(put("/api/marcas/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Acme Pro\",\"descripcion\":\"Actualizada\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Acme Pro"));
		performAsInventoryUser(delete("/api/marcas/1")).andExpect(status().isNoContent());
		performAsInventoryUser(get("/api/marcas/1")).andExpect(status().isNotFound());

		performAsInventoryUser(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
				.content(movimientoJson(productoId, "ENTRADA", 5)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.cantidad").value(5));
		performAsInventoryUser(get("/api/movimientos")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tipo").value("ENTRADA"));
		performAsInventoryUser(get("/api/movimientos/1")).andExpect(status().isOk());
		performAsInventoryUser(get("/api/productos/" + productoId)).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(15));
		performAsInventoryUser(put("/api/movimientos/1").contentType(MediaType.APPLICATION_JSON)
				.content(movimientoJson(productoId, "SALIDA", 3)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("SALIDA"));
		performAsInventoryUser(get("/api/productos/" + productoId)).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(7));
		performAsInventoryUser(delete("/api/movimientos/1")).andExpect(status().isNoContent());
		performAsInventoryUser(get("/api/productos/" + productoId)).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(10));
		performAsInventoryUser(get("/api/movimientos/1")).andExpect(status().isNotFound());
		performAsInventoryUser(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
				.content(movimientoJson(productoId, "SALIDA", 11)))
				.andExpect(status().isConflict());
	}

	@Test
	void autenticacionYDerechosArcoRequierenCredencialesYOperanSobreElUsuarioActual() throws Exception {
		mockMvc.perform(get("/api/categorias")).andExpect(status().isUnauthorized());

		String credentials = "{\"username\":\"arco-user\",\"password\":\"ClaveSegura123!\","
				+ "\"nombreCompleto\":\"Usuario de Prueba\",\"email\":\"arco-user@example.test\","
				+ "\"telefono\":\"5551234\",\"domicilio\":\"Calle Uno\"}";

		mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(credentials))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("arco-user"))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
		mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"otro-usuario\",\"password\":\"OtraClaveSegura123!\","
						+ "\"nombreCompleto\":\"Otro Usuario\",\"email\":\"otro@example.test\"}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/arco/perfil")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/login").with(httpBasic("arco-user", "ClaveSegura123!")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Autenticación correcta"));
		mockMvc.perform(get("/api/arco/perfil").with(httpBasic("arco-user", "ClaveSegura123!")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.email").value("arco-user@example.test"));
		mockMvc.perform(put("/api/arco/perfil").with(httpBasic("arco-user", "ClaveSegura123!"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreCompleto\":\"Usuario Rectificado\",\"email\":\"arco-user@example.test\","
						+ "\"telefono\":\"5559999\",\"domicilio\":\"Calle Dos\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nombreCompleto").value("Usuario Rectificado"));
		mockMvc.perform(patch("/api/arco/oposicion").with(httpBasic("arco-user", "ClaveSegura123!"))
				.contentType(MediaType.APPLICATION_JSON).content("{\"oposicionAlTratamiento\":true}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.oposicionAlTratamiento").value(true));
		mockMvc.perform(delete("/api/arco/perfil").with(httpBasic("arco-user", "ClaveSegura123!")))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/arco/perfil").with(httpBasic("arco-user", "ClaveSegura123!")))
				.andExpect(status().isUnauthorized());
	}

	private String productoJson(String nombre, long categoriaId, long proveedorId) {
		return "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"Uso general\",\"precio\":25.50,\"stock\":10,\"categoriaId\":"
				+ categoriaId + ",\"proveedorId\":" + proveedorId + "}";
	}

	private void registrarUsuarioInventario() throws Exception {
		mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"inventario-user\",\"password\":\"ClaveInventario123!\","
						+ "\"nombreCompleto\":\"Usuario de Inventario\",\"email\":\"inventario@example.test\"}"))
				.andExpect(status().isCreated());
	}

	private long crearProductoInventario() throws Exception {
		long categoriaId = idFrom(performAsInventoryUser(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Categoría movimiento\",\"descripcion\":\"Prueba\"}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
		long proveedorId = idFrom(performAsInventoryUser(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Proveedor movimiento\",\"email\":\"movimiento@example.test\"}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
		return idFrom(performAsInventoryUser(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
				.content(productoJson("Producto movimiento", categoriaId, proveedorId)))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
	}

	private long idFrom(String json) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"id\":(\\d+)").matcher(json);
		if (!matcher.find()) {
			throw new AssertionError("La respuesta creada no contiene id: " + json);
		}
		return Long.parseLong(matcher.group(1));
	}

	private String movimientoJson(long productoId, String tipo, int cantidad) {
		return "{\"productoId\":" + productoId + ",\"tipo\":\"" + tipo + "\",\"cantidad\":" + cantidad
				+ ",\"observacion\":\"Prueba\"}";
	}

	private ResultActions performAsInventoryUser(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.with(httpBasic("inventario-user", "ClaveInventario123!")));
	}
}
