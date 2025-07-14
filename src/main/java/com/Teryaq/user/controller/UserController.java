package com.Teryaq.user.controller;

import com.Teryaq.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;



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