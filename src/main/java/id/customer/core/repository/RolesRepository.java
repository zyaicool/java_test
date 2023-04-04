package id.customer.core.repository;

import id.customer.core.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository  extends JpaRepository<Role, Long> {
}
