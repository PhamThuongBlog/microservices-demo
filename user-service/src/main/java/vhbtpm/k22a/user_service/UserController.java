package vhbtpm.k22a.user_service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    // 1. API Public: Đăng nhập để lấy Token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Giả lập kiểm tra DB
        if ("admin".equals(username) && "123456".equals(password)) {
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản hoặc mật khẩu"));
    }

    // 2. API Protected: Cần có Token mới gọi được qua Gateway
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "X-User-Name", required = false) String username) {
        return ResponseEntity.ok(Map.of(
                "username", username != null ? username : "Unknown",
                "role", "ADMIN",
                "service", "user-service"
        ));
    }
}