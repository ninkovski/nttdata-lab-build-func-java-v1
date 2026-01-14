package com.nttdata.lab;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.Optional;

/**
 * Función Azure HTTP Trigger para el laboratorio NTT Data.
 * 
 * Los alumnos deben implementar la lógica de negocio en este endpoint.
 */
public class LabFunction {

    /**
     * Endpoint principal: Saludo personalizado
     * URL: /api/saludar?nombre=Juan
     * 
     * TAREA: Implementar la lógica para que:
     * - Si recibe un parámetro 'nombre', devuelva: "¡Hola, {nombre}! Bienvenido al laboratorio NTT Data"
     * - Si NO recibe nombre, devuelva: "¡Hola! Por favor, proporciona tu nombre"
     */
    @FunctionName("Saludar")
    public HttpResponseMessage saludar(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "saludar"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Procesando petición HTTP en /api/saludar");

        // Obtener el parámetro 'nombre' de la query string
        String nombre = request.getQueryParameters().get("nombre");

        // TODO: Implementar la lógica de saludo personalizado
        String mensaje;
        if (nombre != null && !nombre.trim().isEmpty()) {
            mensaje = String.format("¡Hola, %s! Bienvenido al laboratorio NTT Data", nombre);
        } else {
            mensaje = "¡Hola! Por favor, proporciona tu nombre";
        }

        return request.createResponseBuilder(HttpStatus.OK)
                .body(mensaje)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .build();
    }

    /**
     * Endpoint de cálculo: Suma dos números
     * URL: /api/calcular?a=5&b=3
     * 
     * TAREA: Implementar la lógica para:
     * - Recibir dos parámetros numéricos 'a' y 'b'
     * - Devolver la suma en formato JSON: {"resultado": 8, "operacion": "5 + 3 = 8"}
     * - Si faltan parámetros o no son números, devolver error 400
     */
    @FunctionName("Calcular")
    public HttpResponseMessage calcular(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "calcular"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Procesando petición HTTP en /api/calcular");

        try {
            // Obtener parámetros
            String aParam = request.getQueryParameters().get("a");
            String bParam = request.getQueryParameters().get("b");

            // Validar que existan los parámetros
            if (aParam == null || bParam == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Faltan parámetros. Proporciona 'a' y 'b'\"}")
                        .header("Content-Type", "application/json")
                        .build();
            }

            // Convertir a números
            int a = Integer.parseInt(aParam);
            int b = Integer.parseInt(bParam);

            // Calcular resultado
            int resultado = a + b;
            String operacion = String.format("%d + %d = %d", a, b, resultado);

            // Construir respuesta JSON
            String jsonResponse = String.format(
                "{\"resultado\": %d, \"operacion\": \"%s\"}",
                resultado, operacion
            );

            return request.createResponseBuilder(HttpStatus.OK)
                    .body(jsonResponse)
                    .header("Content-Type", "application/json")
                    .build();

        } catch (NumberFormatException e) {
            context.getLogger().warning("Error al parsear números: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Los parámetros deben ser números válidos\"}")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }

    /**
     * Endpoint de health check
     * URL: /api/health
     * 
     * Este endpoint se usará para verificar que la función está desplegada correctamente.
     */
    @FunctionName("Health")
    public HttpResponseMessage health(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "health"
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Health check solicitado");

        String response = "{\"status\": \"OK\", \"message\": \"Azure Function está funcionando correctamente\"}";

        return request.createResponseBuilder(HttpStatus.OK)
                .body(response)
                .header("Content-Type", "application/json")
                .build();
    }
}
