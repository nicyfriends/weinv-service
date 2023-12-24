package com.mainthreadlab.weinv.controller;

import com.mainthreadlab.weinv.dto.request.ContactUsRequest;
import com.mainthreadlab.weinv.dto.request.CredentialsRequest;
import com.mainthreadlab.weinv.dto.response.LoginResponse;
import com.mainthreadlab.weinv.service.EmailService;
import com.mainthreadlab.weinv.service.UserService;
import com.mainthreadlab.weinv.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;


@Slf4j
@Validated
@RestController
public class PubblicController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Value("${weinv.contact}")
    private String weinvContact;


    @PostMapping("/auth/login")
    @Operation(
            operationId = "login",
            summary = "Login",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody CredentialsRequest credentialsRequest,
            HttpServletRequest request) throws URISyntaxException, IOException {

        log.info("[Login] request: {}", request.getRequestURI());
        LoginResponse loginResponse = userService.login(credentialsRequest);
        return ResponseEntity.ok().body(loginResponse);
    }


    @PostMapping("/contact-us")
    @Operation(
            operationId = "contactus",
            summary = "Contact us",
            tags = {"Contact"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> contactus(
            @Valid @RequestBody ContactUsRequest contactUsRequest,
            HttpServletRequest request) {

        log.info("[Contactus] request: {}", request.getRequestURI());
        emailService.sendSimpleMessage(contactUsRequest.getFrom(), new String[]{weinvContact}, "contact-us", contactUsRequest.getMessage());
        return ResponseEntity.ok().build();
    }

}
