package basketball.projects.balonAlAire.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    // Un solo DELETE: devuelve cuántas filas borró (0 = no existía)
    @Modifying
    @Query("delete from Category c where c.id = :id")
    int deleteOneById(@Param("id") Long id);
}
