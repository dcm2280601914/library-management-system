package com.example.libmanagement.repository;

import com.example.libmanagement.dto.CategoryStatisticsDto;
import com.example.libmanagement.entity.Category;
import com.example.libmanagement.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Category> findByStatus(CategoryStatus status);

    @Query(
            value = """
        select new com.example.libmanagement.dto.CategoryStatisticsDto(
            c.id,
            c.name,
            c.description,
            c.status,
            count(distinct b.id),
            count(br.id)
        )
        from Category c
        left join Book b on b.category.id = c.id
        left join BorrowRecord br on br.book.id = b.id
        where (:keyword is null or :keyword = '' or lower(c.name) like lower(concat('%', :keyword, '%')))
          and (:status is null or c.status = :status)
        group by c.id, c.name, c.description, c.status
        """,
            countQuery = """
        select count(c)
        from Category c
        where (:keyword is null or :keyword = '' or lower(c.name) like lower(concat('%', :keyword, '%')))
          and (:status is null or c.status = :status)
        """
    )
    Page<CategoryStatisticsDto> findCategoryStatistics(@Param("keyword") String keyword,
                                                       @Param("status") CategoryStatus status,
                                                       Pageable pageable);

    @Query("""
        select new com.example.libmanagement.dto.CategoryStatisticsDto(
            c.id,
            c.name,
            c.description,
            c.status,
            count(distinct b.id),
            count(br.id)
        )
        from Category c
        left join Book b on b.category.id = c.id
        left join BorrowRecord br on br.book.id = b.id
        where c.id = :id
        group by c.id, c.name, c.description, c.status
        """)
    Optional<CategoryStatisticsDto> findCategoryStatisticsById(@Param("id") Long id);
}