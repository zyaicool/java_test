package id.customer.core.repository.wilayah;

import id.customer.core.models.wilayah.MasterProvinsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinsiRepository extends JpaRepository<MasterProvinsi, Long> {
}
