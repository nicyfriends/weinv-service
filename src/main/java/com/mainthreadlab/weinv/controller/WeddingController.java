package com.mainthreadlab.weinv.controller;

import com.itextpdf.text.DocumentException;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.config.security.annotation.JwtUserClaims;
import com.mainthreadlab.weinv.dto.request.ConfirmRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.request.WeddingUpdateRequest;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import com.mainthreadlab.weinv.service.WeddingService;
import com.mainthreadlab.weinv.dto.response.ErrorResponse;
import com.mainthreadlab.weinv.commons.Pagination;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/weddings")
public class WeddingController {

    
    private final WeddingService weddingService;

    @Autowired
    public WeddingController(WeddingService weddingService) {
        this.weddingService = weddingService;
    }


    @PostMapping()
    @Operation(
            operationId = "createWedding",
            summary = "create a wedding event",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)), headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<String> createWedding(
            @Valid @RequestBody WeddingRequest weddingRequest,
            HttpServletRequest request) {

        log.info("[create wedding] - request: {}", request.getRequestURI());
        String uuid = weddingService.createWedding(weddingRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/{uuid}").buildAndExpand(uuid).toUri();
        return ResponseEntity.created(location).body(uuid);
    }


    @PostMapping("/{uuid}/invite")
    @Operation(
            operationId = "invite",
            summary = "send an invitation",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)), headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> invite(
            @Valid @RequestBody UserRequest userRequest,
            @PathVariable String uuid,
            HttpServletRequest request) throws URISyntaxException, IOException {

        log.info("[invite] - request: {}", request.getRequestURI());
        weddingService.invite(uuid, userRequest);
        return ResponseEntity.ok().build();
    }


    @PostMapping(path = "/{uuid}/confirm")
    @Operation(
            operationId = "confirmInvitation",
            summary = "confirm invitation",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> confirmInvitation(
            @PathVariable("uuid") String uuidWedding,
            @RequestParam("guest") String uuidGuest,
            @Valid @RequestBody ConfirmRequest confirmRequest,
            HttpServletRequest request) {

        log.info("[confirm invitation] - request: {}", request.getRequestURI());
        weddingService.confirmInvitation(confirmRequest, uuidWedding, uuidGuest);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{uuid}")
    @Operation(
            operationId = "getWedding",
            summary = "get wedding",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<WeddingResponse> getWedding(
            @PathVariable String uuid,
            HttpServletRequest request) {

        log.info("[get wedding] - request: {}", request.getRequestURI());
        WeddingResponse weddingResponse = weddingService.getWedding(uuid);
        return ResponseEntity.ok().body(weddingResponse);
    }


    @GetMapping("/{uuid}/invitations")
    @Operation(
            operationId = "getWeddingInvitations",
            summary = "get all invitations",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Page<InvitationResponse>> getWeddingInvitations(
            @PathVariable String uuid,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false, defaultValue = "guest.firstName:ASC") String sortingKeys,
            HttpServletRequest request) {

        log.info("[get wedding invitations] - request: {}", request.getRequestURI());
        Page<InvitationResponse> invitationsResponse = weddingService.getWeddingInvitations(uuid, searchKeyword, Pagination.toPageable(offset, limit, sortingKeys));
        return ResponseEntity.ok().body(invitationsResponse);
    }


    @GetMapping("/all")
    @Operation(
            operationId = "getWeddings",
            summary = "get all weddings",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<List<WeddingResponse>> getWeddings(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "date:DESC") String sortingKeys,
            HttpServletRequest request) {

        log.info("[get weddings] - request: {}", request.getRequestURI());
        List<WeddingResponse> response = weddingService.getWeddings(Pagination.toPageable(offset, limit, sortingKeys));
        return ResponseEntity.ok().body(response);
    }


    @PatchMapping("/{uuid}")
    @Operation(
            operationId = "updateWedding",
            summary = "update wedding",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> updateWedding(
            @PathVariable String uuid,
            @RequestBody WeddingUpdateRequest weddingRequest,
            @JwtUserClaims JwtDetails jwtDetails,
            HttpServletRequest request) {

        log.info("[update wedding] - request: {}", request.getRequestURI());
        weddingService.updateWedding(uuid, weddingRequest, jwtDetails);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{uuid}")
    @Operation(
            operationId = "deleteWedding",
            summary = "delete wedding",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> deleteWedding(
            @PathVariable String uuid,
            HttpServletRequest request) {

        log.info("[delete wedding] - request: {}", request.getRequestURI());
        weddingService.deleteWedding(uuid);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{uuid}/downloadPdf")
    @Operation(
            operationId = "downloadPdf",
            summary = "download pdf file that contains wedding information along with guest list",
            tags = {"Wedding"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public void downloadPdf(
            @PathVariable String uuid,
            HttpServletRequest request, HttpServletResponse httpResponse) throws DocumentException, IOException {

        log.info("[download pdf] - request: {}", request.getRequestURI());

        httpResponse.setContentType("application/pdf");
        httpResponse.setHeader("Content-disposition", "attachment; filename=invités.pdf");
        weddingService.downloadPdf(uuid, httpResponse);
    }

}
