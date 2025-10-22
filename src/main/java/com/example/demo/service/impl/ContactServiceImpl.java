package com.example.demo.service.impl;

import com.example.demo.dto.ContactCreationDTO;
import com.example.demo.dto.ContactDTO;
import com.example.demo.entity.Contact;
import com.example.demo.entity.Shop;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ContactRepository;
import com.example.demo.repository.ShopRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ContactService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ContactDTO createContact(ContactCreationDTO dto, String username) {
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + dto.getShopId()));

        Contact contact = new Contact();
        contact.setShop(shop);
        contact.setSubject(dto.getSubject());
        contact.setMessage(dto.getMessage());

        // Nếu người dùng đã đăng nhập (username != null)
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                contact.setUser(user);
                contact.setFullName(user.getFullName()); // Tự điền thông tin
                contact.setEmail(user.getEmail());
                contact.setPhoneNumber(user.getPhoneNumber());
            } else {
                 // Fallback cho guest nếu user không tìm thấy (trường hợp hiếm)
                contact.setFullName(dto.getFullName());
                contact.setEmail(dto.getEmail());
                contact.setPhoneNumber(dto.getPhoneNumber());
            }
        } else { // Nếu là guest
            contact.setFullName(dto.getFullName());
            contact.setEmail(dto.getEmail());
            contact.setPhoneNumber(dto.getPhoneNumber());
        }

        Contact savedContact = contactRepository.save(contact);

        // Chuyển đổi sang DTO để gửi thông báo
        ContactDTO notificationDTO = convertToDto(savedContact);

        // Gửi thông báo WebSocket đến một kênh riêng của vendor
        // Ví dụ kênh: /topic/vendor/123/tickets (Sửa thành ID của vendor)
        String destination = "/topic/vendor/" + shop.getVendor().getUserId() + "/tickets";
        messagingTemplate.convertAndSend(destination, notificationDTO);

        return notificationDTO;
    }

    private ContactDTO convertToDto(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setContactId(contact.getContactId());
        dto.setShopId(contact.getShop().getId()); // Sửa thành getId()
        dto.setSubject(contact.getSubject());
        dto.setMessage(contact.getMessage());
        dto.setStatus(contact.getStatus());
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setFullName(contact.getFullName());
        dto.setEmail(contact.getEmail());
        dto.setPhoneNumber(contact.getPhoneNumber());

        if (contact.getUser() != null) {
            dto.setUserId(contact.getUser().getUserId());
            dto.setUserFullName(contact.getUser().getFullName());
            dto.setUserAvatar(contact.getUser().getAvatarUrl());
        } else {
            // Nếu là guest, có thể set một avatar mặc định
            dto.setUserAvatar("/images/default-avatar.png");
        }

        return dto;
    }
}
