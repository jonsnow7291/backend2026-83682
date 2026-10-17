package com.uniminuto.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot del backend de la clínica veterinaria.
 */
@SpringBootApplication
public class ClinicaApplication {

	/**
	 * Inicia el contexto de Spring y el servidor web embebido.
	 *
	 * @param args argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ClinicaApplication.class, args);
	}

}
