package com.mainthreadlab.weinv.controller;

import com.itextpdf.text.DocumentException;
import com.mainthreadlab.weinv.commons.Pagination;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.config.security.annotation.JwtUserClaims;
import com.mainthreadlab.weinv.dto.request.EventRequest;
import com.mainthreadlab.weinv.dto.request.EventUpdateRequest;
import com.mainthreadlab.weinv.dto.request.UpdateInvitationRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.response.ErrorResponse;
import com.mainthreadlab.weinv.dto.response.EventResponse;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.ResponsePage;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import com.mainthreadlab.weinv.service.EventService;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
public class EventController {


    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping()
    @Operation(operationId = "createEvent", summary = "create an event", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)), headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<String> createEvent(
            @Valid @RequestBody EventRequest eventRequest,
            HttpServletRequest request) {

        log.info("[create event] - request: {}", request.getRequestURI());
        String uuid = eventService.createEvent(eventRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/{uuid}").buildAndExpand(uuid).toUri();
        return ResponseEntity.created(location).body(uuid);
    }


    @PostMapping("/{uuid}/invite")
    @Operation(operationId = "invite", summary = "send an invitation", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)), headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(implementation = URI.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> invite(
            @Valid @RequestBody UserRequest userRequest,
            @PathVariable String uuid,
            HttpServletRequest request) throws URISyntaxException, IOException {

        log.info("[invite] - request: {}", request.getRequestURI());
        eventService.invite(uuid, userRequest);
        return ResponseEntity.ok().build();
    }


    @PatchMapping(path = "/{uuid}/invitation/status")
    @Operation(operationId = "updateInvitationStatus", summary = "update user's invitation status", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> updateInvitationStatus(
            @PathVariable("uuid") String uuidEvent,
            @RequestParam("guest") String uuidGuest,
            @Valid @RequestBody UpdateInvitationRequest updateInvitationRequest,
            HttpServletRequest request) {

        log.info("[update invitation status] - request: {}", request.getRequestURI());
        eventService.updateInvitationStatus(updateInvitationRequest, uuidEvent, uuidGuest);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{uuid}")
    @Operation(operationId = "getEvent", summary = "get event", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable String uuid,
            HttpServletRequest request) {

        log.info("[get event] - request: {}", request.getRequestURI());
        EventResponse eventResponse = eventService.getEvent(uuid);
        return ResponseEntity.ok().body(eventResponse);
    }


    @GetMapping("/{uuid}/image")
    @Operation(operationId = "getEventImage", summary = "get event image", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<String> getEventImage(
            @PathVariable String uuid,
            HttpServletRequest request) {

        log.info("[get event image] - request: {}", request.getRequestURI());
        String eventImage = eventService.getEventImage(uuid);
        return ResponseEntity.ok().body(eventImage);
    }


    @GetMapping("/{uuid}/invitations")
    @Operation(operationId = "getEventInvitations", summary = "get invitations by status", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<ResponsePage<InvitationResponse>> getEventInvitations(
            @PathVariable String uuid,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false) InvitationStatus status,
            @RequestParam(required = false, defaultValue = "guest.firstName:ASC") String sortingKeys,
            HttpServletRequest request) {

        log.info("[get event invitations] - request: {}", request.getRequestURI());
        ResponsePage<InvitationResponse> invitationsResponse = eventService.getEventInvitations(uuid, searchKeyword, Pagination.toPageable(offset, limit, sortingKeys), status);
        return ResponseEntity.ok().body(invitationsResponse);
    }


    @GetMapping("/all")
    @Operation(operationId = "getEvents", summary = "get all events", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<List<EventResponse>> getEvents(
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "date:DESC") String sortingKeys,
            HttpServletRequest request) {

        log.info("[get events] - request: {}", request.getRequestURI());
        List<EventResponse> response = eventService.getEvents(Pagination.toPageable(offset, limit, sortingKeys));
        return ResponseEntity.ok().body(response);
    }


    @PatchMapping("/{uuid}")
    @Operation(operationId = "updateEvent", summary = "update event", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> updateEvent(
            @PathVariable String uuid,
            @RequestBody EventUpdateRequest eventRequest,
            @JwtUserClaims JwtDetails jwtDetails,
            HttpServletRequest request) {

        log.info("[update event] - request: {}", request.getRequestURI());
        eventService.updateEvent(uuid, eventRequest, jwtDetails);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{uuid}")
    @Operation(operationId = "deleteEvent", summary = "delete event", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String uuid,
            HttpServletRequest request) {

        log.info("[delete event] - request: {}", request.getRequestURI());
        eventService.deleteEvent(uuid);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{uuid}/downloadPdf")
    @Operation(operationId = "downloadPdf", summary = "download pdf file that contains event information along with guest list", tags = {"event"}, responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    public void downloadPdf(
            @PathVariable String uuid,
            HttpServletRequest request,
            HttpServletResponse httpResponse) throws DocumentException, IOException {

        log.info("[download pdf] - request: {}", request.getRequestURI());
        httpResponse.setContentType("application/pdf");
        httpResponse.setHeader("Content-disposition", "attachment; filename=invités.pdf");
        eventService.downloadPdf(uuid, httpResponse);
    }

}
