import { useState } from 'react';

// Địa chỉ duy nhất của API Gateway (cổng 18000)
const GATEWAY_URL = 'http://localhost:18000';

function App() {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('123456');
  const [token, setToken] = useState(localStorage.getItem('jwt_token') || '');
  const [profile, setProfile] = useState(null);
  const [products, setProducts] = useState([]);
  const [message, setMessage] = useState('');

  // 1. Gọi API Login qua Gateway -> user-service
  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage('Đang đăng nhập...');
    try {
      const response = await fetch(`${GATEWAY_URL}/api/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();
      if (response.ok) {
        localStorage.setItem('jwt_token', data.token);
        setToken(data.token);
        setMessage('Đăng nhập thành công! Đã lưu JWT Token.');
      } else {
        setMessage(data.error || 'Đăng nhập thất bại');
      }
    } catch (error) {
      setMessage('Lỗi kết nối tới API Gateway: ' + error.message);
    }
  };

  // 2. Gọi API Protected (/profile) qua Gateway -> user-service
  const fetchProfile = async () => {
    setMessage('Đang lấy thông tin user...');
    try {
      const response = await fetch(`${GATEWAY_URL}/api/users/profile`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setProfile(data);
        setMessage('Lấy thông tin Profile thành công!');
      } else {
        setProfile(null);
        setMessage(`Bị từ chối truy cập (HTTP ${response.status}) - Token không hợp lệ hoặc thiếu!`);
      }
    } catch (error) {
      setMessage('Lỗi kết nối: ' + error.message);
    }
  };

  // 3. Gọi API Catalog (/products) qua Gateway -> catalog-service
  const fetchProducts = async () => {
    setMessage('Đang tải danh sách sản phẩm từ Catalog Service...');
    try {
      const response = await fetch(`${GATEWAY_URL}/api/catalog/products`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setProducts(data.items || []);
        setMessage(`Tải thành công ${data.total} sản phẩm (Requested by: ${data.requestedBy})`);
      } else {
        setProducts([]);
        setMessage(`Không thể tải sản phẩm (HTTP ${response.status}) - Vui lòng kiểm tra lại Token!`);
      }
    } catch (error) {
      setMessage('Lỗi kết nối tới Catalog Service: ' + error.message);
    }
  };

  // 4. Đăng xuất (Xóa Token và dữ liệu state)
  const handleLogout = () => {
    localStorage.removeItem('jwt_token');
    setToken('');
    setProfile(null);
    setProducts([]);
    setMessage('Đã đăng xuất.');
  };

  return (
    <div style={{ maxWidth: '600px', margin: '40px auto', fontFamily: 'sans-serif', lineHeight: '1.6' }}>
      <h2>Demo Microservices Frontend</h2>
      <p style={{ color: '#555' }}>Kết nối tới API Gateway: <b>{GATEWAY_URL}</b></p>

      {/* FORM ĐĂNG NHẬP */}
      {!token ? (
        <form onSubmit={handleLogin} style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px' }}>
          <h3>Đăng nhập</h3>
          <div style={{ marginBottom: '10px' }}>
            <label>Username: </label>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} />
          </div>
          <div style={{ marginBottom: '10px' }}>
            <label>Password: </label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <button type="submit" style={{ padding: '8px 16px', cursor: 'pointer' }}>Đăng nhập</button>
        </form>
      ) : (
        /* KHU VỰC ĐÃ ĐĂNG NHẬP */
        <div style={{ border: '1px solid #4CAF50', padding: '20px', borderRadius: '8px' }}>
          <h3>Đã xác thực</h3>
          <p style={{ wordBreak: 'break-all', fontSize: '12px', background: '#f4f4f4', padding: '8px' }}>
            <b>Token:</b> {token}
          </p>
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <button onClick={fetchProfile} style={{ padding: '8px 16px', cursor: 'pointer' }}>
              Gọi API /profile
            </button>
            <button
              onClick={fetchProducts}
              style={{ padding: '8px 16px', cursor: 'pointer', background: '#007bff', color: '#fff', border: 'none', borderRadius: '4px' }}
            >
              Lấy danh sách sản phẩm (/catalog)
            </button>
            <button onClick={handleLogout} style={{ padding: '8px 16px', cursor: 'pointer' }}>
              Đăng xuất
            </button>
          </div>
        </div>
      )}

      {/* HIỂN THỊ THÔNG BÁO */}
      {message && <p style={{ marginTop: '15px', fontWeight: 'bold', color: '#0056b3' }}>{message}</p>}

      {/* KẾT QUẢ TỪ USER-SERVICE */}
      {profile && (
        <div style={{ marginTop: '15px' }}>
          <h4>Thông tin User (từ user-service):</h4>
          <pre style={{ background: '#222', color: '#0f0', padding: '15px', borderRadius: '8px' }}>
            {JSON.stringify(profile, null, 2)}
          </pre>
        </div>
      )}

      {/* KẾT QUẢ TỪ CATALOG-SERVICE */}
      {products.length > 0 && (
        <div style={{ marginTop: '20px' }}>
          <h4>Danh sách sản phẩm (từ catalog-service):</h4>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ background: '#f0f0f0', borderBottom: '2px solid #ccc' }}>
                <th style={{ padding: '8px' }}>ID</th>
                <th style={{ padding: '8px' }}>Tên sản phẩm</th>
                <th style={{ padding: '8px' }}>Giá (VNĐ)</th>
              </tr>
            </thead>
            <tbody>
              {products.map((item) => (
                <tr key={item.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '8px' }}>{item.id}</td>
                  <td style={{ padding: '8px' }}>{item.name}</td>
                  <td style={{ padding: '8px' }}>{item.price.toLocaleString('vi-VN')} đ</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default App;