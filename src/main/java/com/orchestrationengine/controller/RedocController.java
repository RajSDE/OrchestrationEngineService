package com.orchestrationengine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

/**
 * Controller serving the Redocly API documentation page.
 */
@RestController
public class RedocController {

    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    public String getRedocPage() {
        return """
                <!DOCTYPE html>
                <html>
                  <head>
                    <title>Orchestration Engine Service - API Documentation</title>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
                    <style>
                      body {
                        margin: 0;
                        padding: 0;
                      }
                    </style>
                  </head>
                  <body>
                    <redoc spec-url='/v3/api-docs'></redoc>
                    <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
                  </body>
                </html>
                """;
    }

    @GetMapping(value = "/redoc", produces = MediaType.TEXT_HTML_VALUE)
    public String getRedocPageAlias() {
        return getRedocPage();
    }
}
