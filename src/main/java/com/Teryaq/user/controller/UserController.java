package com.Teryaq.user.controller;

import com.Teryaq.user.dto.*;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.service.UserService;
import com.Teryaq.user.dto.AuthenticationRequest;
import com.Teryaq.user.dto.UserAuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing system users and their information")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

//    @PostMapping("/login")
//    @Operation(
//        summary = "User login",
//        description = "Authenticates a user and returns a JWT token"
//    )
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Login successful",
//            content = @Content(mediaType = "application/json",
//            schema = @Schema(implementation = UserAuthenticationResponse.class))),
//        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
//        @ApiResponse(responseCode = "429", description = "Too many login attempts")
//    })
//    public ResponseEntity<UserAuthenticationResponse> login(
//            @RequestBody AuthenticationRequest request,
//            HttpServletRequest httpServletRequest) {
//        UserAuthenticationResponse response = userService.login(request, httpServletRequest);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserResponse());
    }

} 