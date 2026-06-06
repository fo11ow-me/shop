import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm4sEPhMNPfFhpYN76uO";
        String[] passwords = {"admin123", "123456", "admin", "password", "user", "test", "12345678"};
        for (String pwd : passwords) {
            boolean match = encoder.matches(pwd, hash);
            System.out.println(pwd + " -> " + match);
        }
    }
}
