/* (C)2026 */
package com.rjain.spring_demo.base.hibernate.repository;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.rjain.spring_demo.base.hibernate.entity.BaseEntity;

import jakarta.persistence.criteria.Expression;

/**
 * Base repository shared by all JPA repositories in the project. Provides cursor (keyset)
 * pagination helpers using the primary key `id` as cursor.
 */
@NoRepositoryBean
public interface BaseRepository<
                E extends BaseEntity, ID extends Comparable<? super ID> & Serializable>
        extends JpaRepository<E, @NonNull ID>, JpaSpecificationExecutor<E> {

    /**
     * Convenience default method that creates a PageRequest (page 0) for the given page size and
     * delegates to the JPQL query method below. This implements keyset pagination semantics similar
     * to: SELECT * FROM product WHERE id > :cursor ORDER BY id ASC LIMIT :size
     *
     * @param cursor last-seen id (exclusive). If null, starts from beginning.
     * @param size page size (limit)
     * @return up to `size` entities after the cursor ordered by id asc
     */
    default List<E> fetchNextPage(ID cursor, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }

        PageRequest page = PageRequest.of(0, size, Sort.by("id").ascending());

        if (cursor == null) {
            // No cursor: just return the first `size` rows ordered by id asc
            return findAll(page).getContent();
        }

        // Specification filtering by id > cursor
        Specification<E> spec =
                (root, query, cb) -> {
                    Expression<ID> id = root.get("id");
                    return cb.greaterThan(id, cursor);
                };

        return findAll(spec, page).getContent();
    }
}
