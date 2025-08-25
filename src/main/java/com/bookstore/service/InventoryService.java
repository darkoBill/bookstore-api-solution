package com.bookstore.service;

import com.bookstore.dto.InventoryAdjustmentDto;
import com.bookstore.dto.BookDto;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    
    /**
     * Reserve inventory for a book
     */
    void reserveInventory(UUID bookId, Integer quantity);
    
    /**
     * Release reserved inventory
     */
    void releaseReservation(UUID bookId, Integer quantity);
    
    /**
     * Adjust inventory levels (stock received, damaged items, etc.)
     */
    void adjustInventory(UUID bookId, InventoryAdjustmentDto adjustment);
    
    /**
     * Get books that need restocking
     */
    List<BookDto> getBooksNeedingRestock();
    
    /**
     * Get low stock books
     */
    List<BookDto> getLowStockBooks(int threshold);
    
    /**
     * Update reorder levels for books
     */
    void updateReorderLevel(UUID bookId, Integer newLevel);
    
    /**
     * Bulk inventory update
     */
    void bulkInventoryUpdate(List<InventoryAdjustmentDto> adjustments);
}