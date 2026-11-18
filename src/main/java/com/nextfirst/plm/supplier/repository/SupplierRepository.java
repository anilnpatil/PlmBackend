package com.nextfirst.plm.supplier.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nextfirst.plm.supplier.dto.SupplierSuggestionView;
import com.nextfirst.plm.supplier.entity.Supplier;
import com.nextfirst.plm.supplier.entity.SupplierServiceType;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {

    @Query("select s from Supplier s where lower(trim(s.name)) = lower(trim(:name))")
    Optional<Supplier> findByNormalizedName(@Param("name") String name);

    boolean existsBySupplierNumberIgnoreCase(String supplierNumber);

    boolean existsBySupplierNumberIgnoreCaseAndIdNot(String supplierNumber, Long id);

    @Query("""
            select s.id as id, s.supplierNumber as supplierNumber, s.name as name
            from Supplier s
            where s.active = true
              and :serviceType member of s.serviceTypes
              and (lower(s.name) like lower(concat(:query, '%')) escape '!'
                   or lower(s.supplierNumber) like lower(concat(:query, '%')) escape '!')
            order by s.name asc
            """)
    List<SupplierSuggestionView> findSuggestions(
            @Param("query") String query,
            @Param("serviceType") SupplierServiceType serviceType,
            Pageable pageable);
}
