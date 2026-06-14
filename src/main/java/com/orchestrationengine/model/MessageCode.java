package com.orchestrationengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_code", schema = "maindb")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "service_code", nullable = false, length = 100)
    private String serviceCode;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "http_code", nullable = false)
    private Integer httpCode;

    @Column(name = "language", nullable = false, length = 10)
    private String language;
}
