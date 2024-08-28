package com.example.chatapi.controller;

import com.example.chatapi.config.UserDetailsImpl;
import com.example.chatapi.entity.Role;
import com.example.chatapi.entity.User;
import com.example.chatapi.model.JwtResponse;
import com.example.chatapi.model.LoginRequest;
import com.example.chatapi.model.MessageResponse;
import com.example.chatapi.model.SignupRequest;
import com.example.chatapi.service.IRoleService;
import com.example.chatapi.service.IUserService;
import com.example.chatapi.service.impl.RoleService;
import com.example.chatapi.service.impl.UserService;
import com.example.chatapi.utils.JWTUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;

    private final IUserService userService;

    private final IRoleService roleService;

    private final PasswordEncoder encoder;

    private final JWTUtils jwtUtils;

    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            RoleService roleService,
            PasswordEncoder passwordEncoder,
            JWTUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleService = roleService;
        this.encoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Optional<User> user = userService.getUserByUsername(loginRequest.getUsername());

        if (user.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User with this Username does not exists"));
        }

        if (!encoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Wrong password"));
        }


        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles =
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        JwtResponse jwtResponse =
                JwtResponse.builder()
                        .token(jwt)
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .roles(roles)
                        .build();

        response.addCookie(new Cookie("access_token", jwt));
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userService.userExistsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userService.userExistsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user =
                User.builder()
                        .id(UUID.randomUUID())
                        .username(signUpRequest.getUsername())
                        .email(signUpRequest.getEmail())
                        .password(encoder.encode(signUpRequest.getPassword()))
                        .build();

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole =
                    roleService
                            .getByName(Role.ERole.USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(
                    role -> {
                        switch (role) {
                            case "ADMIN":
                                Role adminRole =
                                        roleService.getByName(Role.ERole.ADMIN)
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                roles.add(adminRole);

                                break;
                            case "MODERATOR":
                                Role modRole =
                                        roleService.getByName(Role.ERole.MODERATOR)
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                roles.add(modRole);

                                break;
                            default:
                                Role userRole =
                                        roleService.getByName(Role.ERole.USER)
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                roles.add(userRole);
                        }
                    });
        }

        user.setRoles(roles);
        userService.addUser(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}