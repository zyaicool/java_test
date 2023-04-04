package id.customer.core.repository.wilayah;

import id.customer.core.models.wilayah.MasterKecamatan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KecamatanRepository extends JpaRepository<MasterKecamatan,Long> {
}
