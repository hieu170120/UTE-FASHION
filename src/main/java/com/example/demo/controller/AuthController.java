package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtUtil jwtUtil;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		User currentUser = (User) session.getAttribute("currentUser");
		if (currentUser == null) {
			return "redirect:/login";
		}
		model.addAttribute("currentUser", currentUser);
		String jwtToken = (String) session.getAttribute("jwtToken");
		if (jwtToken != null) {
			model.addAttribute("jwtToken", jwtToken);
		}
		return "dashboard";
	}

	@GetMapping("/login")
	public String showLoginPage(Model model, HttpSession session) {
		if (session.getAttribute("currentUser") != null) {
			return "redirect:/";
		}
		model.addAttribute("loginRequest", new LoginRequest());
		return "auth/login";
	}

	@PostMapping("/login")
	public String login(@Valid @ModelAttribute LoginRequest loginRequest, BindingResult result, HttpSession session,
			jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response,
			RedirectAttributes redirectAttributes, Model model) {

		if (result.hasErrors()) {
			return "auth/login";
		}

		try {
			// 1. Fetch your custom User entity
			User user = authService.loginUser(loginRequest);
			String token = jwtUtil.generateToken(user.getUsername());

			// Set attributes for other parts of the app that might use the session directly
			session.setAttribute("currentUser", user);
			session.setAttribute("username", user.getUsername());
			session.setAttribute("jwtToken", token);

			// 2. Prepare authorities for Spring Security
			var authorities = user.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName())).collect(Collectors.toList());

			// 3. THE FIX: Create the authentication token using YOUR User object as the
			// principal
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, // Use
																												// your
																												// custom
																												// User
																												// object
																												// here
					null, // Credentials are not needed post-authentication
					authorities);

			// 4. Set the authentication in the SecurityContext
			var securityContext = SecurityContextHolder.createEmptyContext();
			securityContext.setAuthentication(authentication);
			SecurityContextHolder.setContext(securityContext);

			// Persist the security context to the session
			securityContextRepository.saveContext(securityContext, request, response);

			redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");

			// Redirect based on role
			boolean isShipper = user.getRoles().stream().anyMatch(role -> "SHIPPER".equals(role.getRoleName()));

			if (isShipper) {
				return "redirect:/shipper";
			}

			return "redirect:/";

		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "auth/login";
		}
	}

	@PostMapping("/api/auth/login")
	@ResponseBody
	public ResponseEntity<?> loginApi(@Valid @RequestBody LoginRequest loginRequest) {
		try {
			AuthResponse authResponse = authService.login(loginRequest);
			return ResponseEntity.ok(authResponse);
		} catch (Exception e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(error);
		}
	}

	@GetMapping("/register")
	public String showRegisterPage(Model model, HttpSession session) {
		if (session.getAttribute("currentUser") != null) {
			return "redirect:/";
		}
		model.addAttribute("registerRequest", new RegisterRequest());
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute RegisterRequest registerRequest, BindingResult result,
			RedirectAttributes redirectAttributes, Model model, HttpSession session) {

		if (result.hasErrors()) {
			return "auth/register";
		}

		try {
			var pendingUser = authService.register(registerRequest);
			session.setAttribute("pendingEmail", registerRequest.getEmail());
			redirectAttributes.addFlashAttribute("successMessage",
					"Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
			return "redirect:/verify-email?email=" + registerRequest.getEmail();
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "auth/register";
		}
	}

	@PostMapping("/api/auth/register")
	@ResponseBody
	public ResponseEntity<?> registerApi(@Valid @RequestBody RegisterRequest registerRequest) {
		try {
			var pendingUser = authService.register(registerRequest);
			Map<String, Object> response = new HashMap<>();
			response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
			response.put("pendingId", pendingUser.getPendingId());
			response.put("username", pendingUser.getUsername());
			response.put("email", pendingUser.getEmail());
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(error);
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
		session.invalidate();
		SecurityContextHolder.clearContext(); // Also clear the security context
		redirectAttributes.addFlashAttribute("successMessage", "Đăng xuất thành công!");
		return "redirect:/login";
	}

	@PostMapping("/api/auth/logout")
	@ResponseBody
	public ResponseEntity<?> logoutApi() {
		SecurityContextHolder.clearContext();
		Map<String, String> response = new HashMap<>();
		response.put("message", "Đăng xuất thành công! Vui lòng xóa token ở phía client.");
		return ResponseEntity.ok(response);
	}
}
