package com.template.webserver;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.boot.WebApplicationType.SERVLET;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
public class BFNWebApi {
    /**
     * Starts our Spring Boot application.
     */
    private static final Logger LOGGER = Logger.getLogger(BFNWebApi.class.getSimpleName());

    public static void main(String[] args) {
        LOGGER.log(Level.INFO," \uD83D\uDD06  \uD83D\uDD06 BFNWebApi starting   \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06️");
        SpringApplication app = new SpringApplication(BFNWebApi.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(SERVLET);
        app.run(args);

        LOGGER.log(Level.INFO," \uD83D\uDD06  \uD83D\uDD06  \uD83D\uDD06  BFNWebApi:  started ....  ❤️ \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 " +
                new Date().toString() + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A");
    }
}
