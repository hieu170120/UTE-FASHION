package com.example.demo.service;

import com.example.demo.dto.ContactCreationDTO;
import com.example.demo.dto.ContactDTO;

public interface ContactService {
    ContactDTO createContact(ContactCreationDTO contactCreationDTO, String username);
}
