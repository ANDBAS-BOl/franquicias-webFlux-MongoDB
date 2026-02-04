/**
 * Capa de infraestructura / adaptadores (Arquitectura Hexagonal).
 * <p>
 * Contiene los adaptadores que implementan los puertos del dominio:
 * - Adaptadores de persistencia: repositorios MongoDB reactivos.
 * - Adaptadores de entrada: controladores REST (WebFlux).
 * Esta capa depende del domain y del application.
 */
package com.seti.andres.infrastructure;
