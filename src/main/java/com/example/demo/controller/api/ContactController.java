package com.example.demo.controller.api;

import com.example.demo.dto.ContactCreationDTO;
import com.example.demo.dto.ContactDTO;
import com.example.demo.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactCreationDTO contactCreationDTO) {
        // Lấy thông tin người dùng đang đăng nhập (nếu có)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            username = authentication.getName();
        }

        ContactDTO createdContact = contactService.createContact(contactCreationDTO, username);
        return new ResponseEntity<>(createdContact, HttpStatus.CREATED);
    }
}
