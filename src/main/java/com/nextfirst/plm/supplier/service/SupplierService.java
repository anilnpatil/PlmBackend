package com.nextfirst.plm.supplier.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.supplier.dto.SupplierPageResponse;
import com.nextfirst.plm.supplier.dto.SupplierResponse;
import com.nextfirst.plm.supplier.dto.SupplierSuggestion;
import com.nextfirst.plm.supplier.dto.SupplierSuggestionView;
import com.nextfirst.plm.supplier.dto.SupplierUpsertRequest;
import com.nextfirst.plm.supplier.entity.Supplier;
import com.nextfirst.plm.supplier.entity.SupplierServiceType;
import com.nextfirst.plm.supplier.exception.DuplicateSupplierException;
import com.nextfirst.plm.supplier.exception.SupplierNotFoundException;
import com.nextfirst.plm.supplier.mapper.SupplierMapper;
import com.nextfirst.plm.supplier.repository.SupplierRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class SupplierService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_SUGGESTIONS = 10;

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public List<SupplierSuggestion> suggestions(String query, SupplierServiceType serviceType) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.length() < 2 || serviceType == null) {
            return List.of();
        }
        if (normalizedQuery.length() > 100) {
            throw new IllegalArgumentException("query must not exceed 100 characters");
        }
        return supplierRepository.findSuggestions(
                        escapeSuggestionQuery(normalizedQuery), serviceType, PageRequest.of(0, MAX_SUGGESTIONS)).stream()
                .map(this::toSuggestion)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierPageResponse search(String query, Boolean active, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Specification<Supplier> specification = (root, criteriaQuery, builder) -> {
            Predicate predicate = builder.conjunction();
            if (active != null) {
                predicate = builder.and(predicate, builder.equal(root.get("active"), active));
            }
            if (!normalizedQuery.isEmpty()) {
                String pattern = "%" + escapeLike(normalizedQuery) + "%";
                Predicate name = builder.like(builder.lower(root.get("name")), pattern, '\\');
                Predicate number = builder.like(builder.lower(root.get("supplierNumber")), pattern, '\\');
                predicate = builder.and(predicate, builder.or(name, number));
            }
            return predicate;
        };

        Page<Supplier> suppliers = supplierRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"))));
        return new SupplierPageResponse(
                suppliers.map(supplierMapper::toResponse).getContent(),
                suppliers.getNumber(),
                suppliers.getSize(),
                suppliers.getTotalElements(),
                suppliers.getTotalPages(),
                suppliers.isFirst(),
                suppliers.isLast());
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return supplierMapper.toResponse(findSupplier(id));
    }

    @Transactional
    public SupplierResponse create(SupplierUpsertRequest request) {
        validateUniqueFields(request, null);
        Supplier supplier = supplierRepository.save(supplierMapper.toEntity(request));
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierUpsertRequest request) {
        Supplier supplier = findSupplier(id);
        validateUniqueFields(request, id);
        supplierMapper.updateEntity(supplier, request);
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateStatus(Long id, boolean active) {
        Supplier supplier = findSupplier(id);
        supplier.setActive(active);
        return supplierMapper.toResponse(supplier);
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new SupplierNotFoundException(id));
    }

    private void validateUniqueFields(SupplierUpsertRequest request, Long excludedId) {
        String number = request.supplierNumber().trim();
        boolean duplicateNumber = excludedId == null
                ? supplierRepository.existsBySupplierNumberIgnoreCase(number)
                : supplierRepository.existsBySupplierNumberIgnoreCaseAndIdNot(number, excludedId);
        if (duplicateNumber) {
            throw new DuplicateSupplierException("number", number);
        }

        supplierRepository.findByNormalizedName(request.name().trim())
                .filter(existing -> excludedId == null || !excludedId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateSupplierException("name", request.name().trim());
                });
    }

    private SupplierSuggestion toSuggestion(SupplierSuggestionView view) {
        return new SupplierSuggestion(view.getId(), view.getSupplierNumber(), view.getName());
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private String escapeSuggestionQuery(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
