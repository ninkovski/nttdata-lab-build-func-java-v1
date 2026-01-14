package com.nttdata.lab;

import com.microsoft.azure.functions.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LabFunction.
 * 
 * IMPORTANTE: Los alumnos deben completar estos tests para que el CI/CD funcione.
 * Si estos tests fallan, el despliegue NO se ejecutará.
 */
class LabFunctionTest {

    private LabFunction function;
    private HttpRequestMessage<Optional<String>> request;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        function = new LabFunction();
        
        // Mock del contexto
        context = mock(ExecutionContext.class);
        Logger logger = Logger.getLogger(LabFunctionTest.class.getName());
        when(context.getLogger()).thenReturn(logger);
    }

    /**
     * TEST 1: Verificar que el endpoint /api/saludar responde con saludo personalizado
     */
    @Test
    @DisplayName("Saludar debe devolver mensaje personalizado cuando se proporciona nombre")
    void testSaludarConNombre() {
        // Arrange - Preparar
        request = createMockRequest("nombre", "Juan");

        // Act - Ejecutar
        HttpResponseMessage response = function.saludar(request, context);

        // Assert - Verificar
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatus(), "El status debe ser 200 OK");
        
        String body = (String) response.getBody();
        assertNotNull(body, "El body no debe ser null");
        assertTrue(body.contains("Juan"), "El mensaje debe contener el nombre 'Juan'");
        assertTrue(body.contains("Bienvenido"), "El mensaje debe contener 'Bienvenido'");
    }

    /**
     * TEST 2: Verificar que el endpoint /api/saludar responde cuando NO hay nombre
     */
    @Test
    @DisplayName("Saludar debe pedir nombre cuando no se proporciona")
    void testSaludarSinNombre() {
        // Arrange
        request = createMockRequest(null, null);

        // Act
        HttpResponseMessage response = function.saludar(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        
        String body = (String) response.getBody();
        assertNotNull(body);
        assertTrue(body.toLowerCase().contains("nombre"), 
            "El mensaje debe mencionar 'nombre' cuando no se proporciona");
    }

    /**
     * TEST 3: Verificar que /api/calcular suma correctamente
     */
    @Test
    @DisplayName("Calcular debe sumar dos números correctamente")
    void testCalcularSumaCorrecta() {
        // Arrange
        request = createMockRequestWithParams(Map.of("a", "5", "b", "3"));

        // Act
        HttpResponseMessage response = function.calcular(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        
        String body = (String) response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"resultado\": 8"), 
            "El resultado debe ser 8 cuando se suma 5 + 3");
        assertTrue(body.contains("5 + 3 = 8"), 
            "La respuesta debe incluir la operación completa");
    }

    /**
     * TEST 4: Verificar que /api/calcular maneja parámetros faltantes
     */
    @Test
    @DisplayName("Calcular debe devolver error 400 cuando faltan parámetros")
    void testCalcularSinParametros() {
        // Arrange
        request = createMockRequest(null, null);

        // Act
        HttpResponseMessage response = function.calcular(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), 
            "Debe devolver 400 cuando faltan parámetros");
        
        String body = (String) response.getBody();
        assertTrue(body.contains("error"), 
            "La respuesta debe indicar que hay un error");
    }

    /**
     * TEST 5: Verificar que /api/calcular maneja parámetros no numéricos
     */
    @Test
    @DisplayName("Calcular debe devolver error 400 con parámetros no numéricos")
    void testCalcularConParametrosInvalidos() {
        // Arrange
        request = createMockRequestWithParams(Map.of("a", "abc", "b", "xyz"));

        // Act
        HttpResponseMessage response = function.calcular(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), 
            "Debe devolver 400 con parámetros no numéricos");
    }

    /**
     * TEST 6: Verificar que /api/health responde correctamente
     */
    @Test
    @DisplayName("Health debe devolver status OK")
    void testHealthCheck() {
        // Arrange
        request = createMockRequest(null, null);

        // Act
        HttpResponseMessage response = function.health(request, context);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        
        String body = (String) response.getBody();
        assertTrue(body.contains("\"status\": \"OK\""), 
            "Health check debe devolver status OK");
    }

    // ===== MÉTODOS AUXILIARES PARA CREAR MOCKS =====

    /**
     * Crea un mock de HttpRequestMessage con un parámetro específico
     */
    @SuppressWarnings("unchecked")
    private HttpRequestMessage<Optional<String>> createMockRequest(String paramName, String paramValue) {
        HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);
        
        Map<String, String> queryParams = new HashMap<>();
        if (paramName != null && paramValue != null) {
            queryParams.put(paramName, paramValue);
        }
        
        when(req.getQueryParameters()).thenReturn(queryParams);
        
        // Mock del builder y response usando Mockito
        HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage response = mock(HttpResponseMessage.class);
        
        when(req.createResponseBuilder(any(HttpStatus.class))).thenReturn(builder);
        when(builder.status(any(HttpStatus.class))).thenReturn(builder);
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(builder.body(any())).thenAnswer(invocation -> {
            when(response.getBody()).thenReturn(invocation.getArgument(0));
            when(response.getStatus()).thenReturn(HttpStatus.OK);
            return builder;
        });
        when(builder.build()).thenReturn(response);
        
        return req;
    }

    /**
     * Crea un mock con múltiples parámetros
     */
    @SuppressWarnings("unchecked")
    private HttpRequestMessage<Optional<String>> createMockRequestWithParams(Map<String, String> params) {
        HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);
        
        when(req.getQueryParameters()).thenReturn(params);
        
        // Mock del builder y response usando Mockito
        HttpResponseMessage.Builder builder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage response = mock(HttpResponseMessage.class);
        
        when(req.createResponseBuilder(any(HttpStatus.class))).thenReturn(builder);
        when(builder.status(any(HttpStatus.class))).thenReturn(builder);
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(builder.body(any())).thenAnswer(invocation -> {
            when(response.getBody()).thenReturn(invocation.getArgument(0));
            when(response.getStatus()).thenReturn(HttpStatus.OK);
            return builder;
        });
        when(builder.build()).thenReturn(response);
        
        return req;
    }
}
