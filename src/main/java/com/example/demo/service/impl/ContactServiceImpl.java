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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ContactDTO createContact(ContactCreationDTO dto, String username) {
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + dto.getShopId()));

        Contact contact = new Contact();
        contact.setShop(shop);
        contact.setSubject(dto.getSubject());
        contact.setMessage(dto.getMessage());

        // Nếu người dùng đã đăng nhập, ưu tiên lấy thông tin từ user
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                contact.setUser(user);
                contact.setFullName(user.getFullName());
                contact.setEmail(user.getEmail());
                contact.setPhoneNumber(user.getPhoneNumber());
            } else {
                // Fallback nếu không tìm thấy user (hiếm) hoặc cho guest
                setGuestContactDetails(contact, dto);
            }
        } else { // Nếu là guest
            setGuestContactDetails(contact, dto);
        }

        Contact savedContact = contactRepository.save(contact);
        ContactDTO notificationDTO = convertToDto(savedContact);

        // ### START FIX ###
        // Kiểm tra vendor có tồn tại không trước khi gửi WebSocket
        User vendor = shop.getVendor();
        if (vendor != null && vendor.getUserId() != null) {
            String destination = "/topic/vendor/" + vendor.getUserId() + "/tickets";
            System.out.println("Sending WebSocket notification to: " + destination);
            messagingTemplate.convertAndSend(destination, notificationDTO);
        } else {
            // Ghi log cảnh báo thay vì gây lỗi
            System.out.println("WARN: Shop with ID " + shop.getId() + " does not have an associated vendor. Skipping WebSocket notification.");
        }
        // ### END FIX ###

        return notificationDTO;
    }

    private void setGuestContactDetails(Contact contact, ContactCreationDTO dto) {
        contact.setFullName(dto.getFullName());
        contact.setEmail(dto.getEmail());
        contact.setPhoneNumber(dto.getPhoneNumber());
    }

    private ContactDTO convertToDto(Contact contact) {
        // Sử dụng ModelMapper để chuyển đổi an toàn và ngắn gọn
        ContactDTO dto = modelMapper.map(contact, ContactDTO.class);

        // Xử lý các trường mapping phức tạp hơn nếu cần
        if (contact.getShop() != null) {
            dto.setShopId(contact.getShop().getId());
        }
        if (contact.getUser() != null) {
            dto.setUserId(contact.getUser().getUserId());
            dto.setUserFullName(contact.getUser().getFullName());
            dto.setUserAvatar(contact.getUser().getAvatarUrl());
        } else {
            dto.setUserAvatar("/images/default-avatar.png");
        }
        return dto;
    }
}
