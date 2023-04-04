package id.customer.core.repository.wilayah;

import id.customer.core.models.wilayah.MasterKota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KotaRepository extends JpaRepository<MasterKota,Long> {

}
