package basketball.projects.balonAlAire.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.Advertisement;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findAllByActiveTrue();

    // Un solo DELETE: devuelve cuántas filas borró (0 = no existía)
    @Modifying
    @Query("delete from Advertisement a where a.id = :id")
    int deleteOneById(@Param("id") Long id);
}
