package id.customer.core.repository.wilayah;

import id.customer.core.models.wilayah.MasterKelurahan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KelurahanRepository extends JpaRepository<MasterKelurahan,Long> {
}
