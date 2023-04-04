package id.customer.core.repository;

import java.util.Optional;

import id.customer.core.models.RefreshToken;
import id.customer.core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @Transactional
  Optional<RefreshToken> findByToken(String token);

  @Modifying
  @Transactional
  int deleteByUser(User user);
}
