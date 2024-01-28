package com.mainthreadlab.weinv.controller;

import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.config.security.annotation.JwtUserClaims;
import com.mainthreadlab.weinv.dto.request.UpdateUserRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.response.ErrorResponse;
import com.mainthreadlab.weinv.dto.response.UserResponse;
import com.mainthreadlab.weinv.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    @Operation(operationId = "registerWeddingResponsible", summary = "Register a responsible for an event (only 'admin' can do that)", tags = {"User"}, responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)), headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> registerWeddingResponsible(
            @Valid @RequestBody UserRequest userRequest,
            @JwtUserClaims JwtDetails jwtDetails,
            HttpServletRequest request) throws IOException, URISyntaxException {

        log.info("[registerWeddingResponsible] - request: {}", request.getRequestURI());
        String uuid = userService.registerWeddingResponsible(jwtDetails, userRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/{uuid}").buildAndExpand(uuid).toUri();
        return ResponseEntity.created(location).build();
    }


    @GetMapping("/{uuid}")
    @Operation(
            operationId = "getUser",
            summary = "get user",
            tags = {"User"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String uuid,
            @JwtUserClaims JwtDetails jwtDetails,
            HttpServletRequest request) {

        log.info("[GetUser] request: {}", request.getRequestURI());
        UserResponse userResponse = userService.getUser(uuid);
        return ResponseEntity.ok().body(userResponse);
    }


    // used by the responsible for wedding to update its account data or guest info's
    @PatchMapping("/{uuid}")
    @Operation(
            operationId = "updateUser",
            summary = "update user",
            tags = {"User"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> updateUser(
            @PathVariable String uuid,
            @RequestParam(required = false) String uuidWedding,
            @Valid @RequestBody UpdateUserRequest updateUserRequest,
            @JwtUserClaims JwtDetails jwtDetails,
            HttpServletRequest request) throws URISyntaxException {

        log.info("[update user] request: {}", request.getRequestURI());
        userService.updateUser(uuid, uuidWedding, updateUserRequest);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{uuid}")
    @Operation(operationId = "deleteGuestInvitation", summary = "delete guest's invitation", tags = {"User"}, responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> deleteGuestInvitation(
            @PathVariable String uuid,
            @RequestParam(required = false) String uuidWedding,
            @JwtUserClaims JwtDetails jwtDetails,
            HttpServletRequest request) throws URISyntaxException {

        log.info("[delete guest invitation] request: {}", request.getRequestURI());
        userService.deleteGuestInvitation(uuid, uuidWedding);
        return ResponseEntity.ok().build();
    }

}
