package id.customer.core.service;

import id.customer.core.models.Result;
import org.springframework.http.ResponseEntity;

public interface RolesService {
    ResponseEntity<Result> getAllRoles(String Uri);
}
