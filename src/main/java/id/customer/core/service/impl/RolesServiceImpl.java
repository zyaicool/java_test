package id.customer.core.service.impl;

import id.customer.core.models.Result;
import id.customer.core.models.Role;
import id.customer.core.utils.StringUtil;
import id.customer.core.repository.RolesRepository;
import id.customer.core.service.RolesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RolesServiceImpl implements RolesService {
    private Result result;

    @Autowired
    StringUtil stringUtil;

    @Autowired
    RolesRepository rolesRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public ResponseEntity<Result> getAllRoles(String Uri) {
        result = new Result();
        try {
            Map<String, List<Role>> items = new HashMap<>();
            items.put("items", rolesRepository.findAll());
            result.setData(items);
        } catch (Exception e) {
            logger.error(stringUtil.getError(e));
        }
        return ResponseEntity.ok(result);
    }
}
