package com.paqrap.logistics;

import com.paqrap.logistics.planificacion.algoritmo.BenchmarkMetaheuristicas;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal de la aplicación Spring Boot para el sistema de logística de PaqRap.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }

    @Bean
    public CommandLineRunner benchmarkCommandLineRunner(ApplicationArguments appArgs) {
        return args -> {
            if (appArgs.containsOption("benchmark") || "true".equalsIgnoreCase(System.getenv("RUN_BENCHMARK"))) {
                String mes = appArgs.containsOption("mes") ? appArgs.getOptionValues("mes").get(0) : "202601";
                String pedidos = appArgs.containsOption("pedidos") ? appArgs.getOptionValues("pedidos").get(0) : "30";
                boolean averia = appArgs.containsOption("averia") || appArgs.getNonOptionArgs().contains("averia")
                        || "true".equalsIgnoreCase(System.getenv("SIMULAR_AVERIA"));
                BenchmarkMetaheuristicas.main(new String[]{mes, "", pedidos, averia ? "averia" : ""});
                System.exit(0);
            }
        };
    }
}

