package vhbtpm.k22a.user_service;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        // Khởi tạo MockMvc độc lập cho UserController mà không cần load Spring Context
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    // --- TEST CASES CHO API /login ---

    @Test
    @DisplayName("POST /api/users/login - Đăng nhập thành công trả về 200 và token")
    void login_ValidCredentials_ReturnsOkWithToken() throws Exception {
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mocked_token";
        when(jwtUtil.generateToken("admin")).thenReturn(mockToken);

        Map<String, String> credentials = Map.of(
                "username", "admin",
                "password", "123456"
        );

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken));

        verify(jwtUtil, times(1)).generateToken("admin");
    }

    @Test
    @DisplayName("POST /api/users/login - Sai mật khẩu trả về 401 và thông báo lỗi")
    void login_WrongPassword_ReturnsUnauthorized() throws Exception {
        Map<String, String> credentials = Map.of(
                "username", "admin",
                "password", "wrong_pass"
        );

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sai tài khoản hoặc mật khẩu"));

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("POST /api/users/login - Thiếu username hoặc password trả về 401")
    void login_MissingCredentials_ReturnsUnauthorized() throws Exception {
        Map<String, String> emptyCredentials = Map.of();

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyCredentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Sai tài khoản hoặc mật khẩu"));

        verify(jwtUtil, never()).generateToken(anyString());
    }

    // --- TEST CASES CHO API /profile ---

    @Test
    @DisplayName("GET /api/users/profile - Có header X-User-Name trả về đúng username")
    void getProfile_WithHeader_ReturnsProfileWithUsername() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("X-User-Name", "nguyen_van_a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nguyen_van_a"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.service").value("user-service"));
    }

    @Test
    @DisplayName("GET /api/users/profile - Không có header X-User-Name trả về username là Unknown")
    void getProfile_WithoutHeader_ReturnsProfileWithUnknown() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Unknown"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.service").value("user-service"));
    }
}