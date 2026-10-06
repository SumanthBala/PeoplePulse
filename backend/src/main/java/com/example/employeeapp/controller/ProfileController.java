package com.example.employeeapp.controller;

import com.example.employeeapp.entity.User;
import com.example.employeeapp.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserRepository users;

    public ProfileController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        User user = users.findByUsername(username.trim()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> updateProfile(@PathVariable String username, @RequestBody Map<String, String> body) {
        User user = users.findByUsername(username.trim()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (body == null) body = Map.of();

        String firstName = trim(body.get("firstName"));
        String lastName = trim(body.get("lastName"));
        String email = trim(body.get("email"));
        String phone = trim(body.get("phone"));
        String skills = trim(body.get("skills"));

        if (firstName.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "First name is required."));
        if (email.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        if (phone.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "Phone number is required."));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setName((firstName + (lastName.isBlank() ? "" : " " + lastName)).trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setSkills(skills);
        users.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{username}/photo")
    public ResponseEntity<?> removePhoto(@PathVariable String username) {
        User user = users.findByUsername(username.trim()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setProfilePic(null);
        user.setProfilePicContentType(null);
        users.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping(value = "/{username}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(@PathVariable String username, @RequestPart("photo") MultipartFile photo) throws IOException {
        User user = users.findByUsername(username.trim()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (photo == null || photo.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Please select a profile picture."));
        if (photo.getSize() > 5 * 1024 * 1024) return ResponseEntity.badRequest().body(Map.of("message", "Profile picture must be 5 MB or smaller."));
        String contentType = photo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/"))
            return ResponseEntity.badRequest().body(Map.of("message", "Only image files are supported."));
        user.setProfilePic(photo.getBytes());
        user.setProfilePicContentType(contentType);
        users.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    private Map<String, Object> toResponse(User u) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("username", u.getUsername());
        out.put("firstName", value(u.getFirstName(), firstFromName(u.getName())));
        out.put("lastName", value(u.getLastName(), lastFromName(u.getName())));
        out.put("name", u.getName());
        out.put("email", value(u.getEmail(), ""));
        out.put("phone", value(u.getPhone(), ""));
        out.put("role", u.getRole().name());
        out.put("skills", value(u.getSkills(), ""));
        if (u.getProfilePic() != null && u.getProfilePic().length > 0)
            out.put("profilePic", "data:" + (u.getProfilePicContentType() == null ? "image/jpeg" : u.getProfilePicContentType()) + ";base64," + Base64.getEncoder().encodeToString(u.getProfilePic()));
        else out.put("profilePic", null);
        return out;
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
    private String value(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String firstFromName(String name) {
        if (name == null || name.isBlank()) return "";
        int i = name.trim().indexOf(' ');
        return i < 0 ? name.trim() : name.trim().substring(0, i);
    }
    private String lastFromName(String name) {
        if (name == null || name.isBlank()) return "";
        String n = name.trim(); int i = n.indexOf(' ');
        return i < 0 ? "" : n.substring(i + 1);
    }
}
