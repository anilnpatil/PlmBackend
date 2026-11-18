package com.nextfirst.plm.supplier.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import com.nextfirst.plm.supplier.dto.SupplierSuggestionView;
import com.nextfirst.plm.supplier.dto.SupplierUpsertRequest;
import com.nextfirst.plm.supplier.entity.SupplierServiceType;
import com.nextfirst.plm.supplier.exception.DuplicateSupplierException;
import com.nextfirst.plm.supplier.mapper.SupplierMapper;
import com.nextfirst.plm.supplier.repository.SupplierRepository;

class SupplierServiceTest {

    private final SupplierRepository supplierRepository = org.mockito.Mockito.mock(SupplierRepository.class);
    private final SupplierService supplierService = new SupplierService(supplierRepository, new SupplierMapper());

    @Test
    void suggestionsRequireTwoCharactersAndAreBounded() {
        assertEquals(List.of(), supplierService.suggestions("A", SupplierServiceType.PURCHASE));
        verify(supplierRepository, never()).findSuggestions(anyString(), any(), any());

        SupplierSuggestionView suggestion = org.mockito.Mockito.mock(SupplierSuggestionView.class);
        when(suggestion.getId()).thenReturn(3L);
        when(suggestion.getSupplierNumber()).thenReturn("SUP-003");
        when(suggestion.getName()).thenReturn("Acme Components");
        when(supplierRepository.findSuggestions("Ac", SupplierServiceType.PURCHASE, PageRequest.of(0, 10)))
                .thenReturn(List.of(suggestion));

        var result = supplierService.suggestions(" Ac ", SupplierServiceType.PURCHASE);

        assertEquals(1, result.size());
        assertEquals("Acme Components", result.get(0).name());
        verify(supplierRepository).findSuggestions("Ac", SupplierServiceType.PURCHASE, PageRequest.of(0, 10));
    }

    @Test
    void createRejectsDuplicateSupplierNumbersBeforeSaving() {
        when(supplierRepository.existsBySupplierNumberIgnoreCase("SUP-001")).thenReturn(true);
        SupplierUpsertRequest request = new SupplierUpsertRequest(
                " SUP-001 ", "Acme", Set.of(SupplierServiceType.PURCHASE),
                null, null, null, null, null);

        assertThrows(DuplicateSupplierException.class, () -> supplierService.create(request));

        verify(supplierRepository, never()).save(any());
    }

    @Test
    void createRejectsSupplierNamesWithoutCaseSensitivity() {
        when(supplierRepository.findByNormalizedName("Acme")).thenReturn(Optional.of(
                new com.nextfirst.plm.supplier.entity.Supplier()));
        SupplierUpsertRequest request = new SupplierUpsertRequest(
                "SUP-002", "Acme", Set.of(SupplierServiceType.MANUFACTURING),
                null, null, null, null, null);

        assertThrows(DuplicateSupplierException.class, () -> supplierService.create(request));
        verify(supplierRepository, never()).save(any());
    }
}
