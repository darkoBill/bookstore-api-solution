package com.bookstore.service.impl;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.InventoryAdjustmentDto;
import com.bookstore.exception.InsufficientInventoryException;
import com.bookstore.exception.InvalidInventoryAdjustmentException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    @Override
    @Transactional
    public void reserveInventory(UUID bookId, Integer quantity) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
            
        if (book.getAvailableQuantity() < quantity) {
            throw new InsufficientInventoryException(bookId, quantity, book.getAvailableQuantity());
        }
        
        book.setReservedQuantity(book.getReservedQuantity() + quantity);
        bookRepository.save(book);
        
        log.info("Reserved {} units for book {}", quantity, bookId);
    }
    
    @Override
    @Transactional
    public void releaseReservation(UUID bookId, Integer quantity) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
            
        int newReservedQuantity = Math.max(0, book.getReservedQuantity() - quantity);
        book.setReservedQuantity(newReservedQuantity);
        bookRepository.save(book);
        
        log.info("Released {} units reservation for book {}", quantity, bookId);
    }
    
    @Override
    @Transactional
    public void adjustInventory(UUID bookId, InventoryAdjustmentDto adjustment) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
            
        int newQuantity = book.getQuantityInStock() + adjustment.quantityChange();
        if (newQuantity < 0) {
            throw new InvalidInventoryAdjustmentException(
                bookId,
                book.getQuantityInStock(),
                adjustment.quantityChange()
            );
        }
        
        book.setQuantityInStock(newQuantity);
        bookRepository.save(book);
        
        log.info("Adjusted inventory for book {} by {} units. Reason: {}", 
                bookId, adjustment.quantityChange(), adjustment.reason());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDto> getBooksNeedingRestock() {
        return bookRepository.findBooksNeedingRestock()
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookDto> getLowStockBooks(int threshold) {
        return bookRepository.findBooksWithLowStock(threshold)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional
    public void updateReorderLevel(UUID bookId, Integer newLevel) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
            
        book.setReorderLevel(newLevel);
        bookRepository.save(book);
        
        log.info("Updated reorder level for book {} to {}", bookId, newLevel);
    }
    
    @Override
    @Transactional
    public void bulkInventoryUpdate(List<InventoryAdjustmentDto> adjustments) {
        for (InventoryAdjustmentDto adjustment : adjustments) {
            adjustInventory(adjustment.bookId(), adjustment);
        }
        log.info("Completed bulk inventory update for {} books", adjustments.size());
    }
}