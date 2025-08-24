package com.bookstore.controller;

import com.bookstore.dto.BookDto;
import com.bookstore.dto.InventoryAdjustmentDto;
import com.bookstore.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Endpoints for managing book inventory")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @PostMapping("/{bookId}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Reserve inventory for a book", 
               description = "Reserve specified quantity of books for purchase")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventory reserved successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient inventory available")
    })
    public void reserveInventory(
            @Parameter(description = "Book ID") @PathVariable UUID bookId,
            @Parameter(description = "Quantity to reserve") @RequestParam @Min(1) Integer quantity) {
        inventoryService.reserveInventory(bookId, quantity);
    }
    
    @DeleteMapping("/{bookId}/reservation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Release inventory reservation", 
               description = "Release previously reserved inventory")
    public void releaseReservation(
            @Parameter(description = "Book ID") @PathVariable UUID bookId,
            @Parameter(description = "Quantity to release") @RequestParam @Min(1) Integer quantity) {
        inventoryService.releaseReservation(bookId, quantity);
    }
    
    @PostMapping("/{bookId}/adjust")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adjust inventory levels", 
               description = "Manually adjust inventory for stock received, damaged items, etc.")
    public void adjustInventory(
            @Parameter(description = "Book ID") @PathVariable UUID bookId,
            @Valid @RequestBody InventoryAdjustmentDto adjustment) {
        inventoryService.adjustInventory(bookId, adjustment);
    }
    
    @GetMapping("/restock-needed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get books needing restock", 
               description = "Retrieve books that are at or below reorder level")
    public List<BookDto> getBooksNeedingRestock() {
        return inventoryService.getBooksNeedingRestock();
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get low stock books", 
               description = "Retrieve books below specified threshold")
    public List<BookDto> getLowStockBooks(
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") @Min(0) Integer threshold) {
        return inventoryService.getLowStockBooks(threshold);
    }
    
    @PutMapping("/{bookId}/reorder-level")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update reorder level", 
               description = "Set new reorder level for automatic restocking alerts")
    public void updateReorderLevel(
            @Parameter(description = "Book ID") @PathVariable UUID bookId,
            @Parameter(description = "New reorder level") @RequestParam @Min(0) Integer level) {
        inventoryService.updateReorderLevel(bookId, level);
    }
    
    @PostMapping("/bulk-adjust")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk inventory adjustment", 
               description = "Adjust inventory for multiple books at once")
    public void bulkInventoryUpdate(@Valid @RequestBody List<InventoryAdjustmentDto> adjustments) {
        inventoryService.bulkInventoryUpdate(adjustments);
    }
}