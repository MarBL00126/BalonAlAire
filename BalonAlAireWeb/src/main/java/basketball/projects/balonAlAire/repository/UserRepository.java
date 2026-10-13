package basketball.projects.balonAlAire.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.User;
import jakarta.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    // Bloqueo de escritura: dos pedidos simultáneos con el mismo token no pueden consumirlo ambos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findByPasswordResetToken(String passwordResetTokenHash);
}
