package com.ecommerce.wishlist_service.service.impl;
import com.ecommerce.wishlist_service.client.BookServiceClient;
import com.ecommerce.wishlist_service.client.CartServiceClient;
import com.ecommerce.wishlist_service.dto.*;
import com.ecommerce.wishlist_service.entity.Wishlist;
import com.ecommerce.wishlist_service.entity.WishlistItem;
import com.ecommerce.wishlist_service.exception.*;
import com.ecommerce.wishlist_service.repository.WishlistItemRepository;
import com.ecommerce.wishlist_service.repository.WishlistRepository;
import com.ecommerce.wishlist_service.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final BookServiceClient bookServiceClient;
    private final CartServiceClient cartServiceClient;

    @Value("${wishlist.max-items:100}")
    private int maxWishlistItems;

    @Override
    public WishlistDTO getWishlistByUserId(Long userId) {
        log.info("Fetching wishlist for user: {}", userId);
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createNewWishlist(userId));
        return convertToDTO(wishlist);
    }

    @Override
    public WishlistDTO addToWishlist(AddToWishlistRequest request) {
        log.info("Adding book {} to wishlist for user {}", request.getBookId(), request.getUserId());

        // Verify book exists
        if (!bookServiceClient.bookExists(request.getBookId())) {
            throw new WishlistServiceException("Book not found with id: " + request.getBookId());
        }

        // Get or create wishlist
        Wishlist wishlist = wishlistRepository.findByUserId(request.getUserId())
                .orElseGet(() -> createNewWishlist(request.getUserId()));

        // Check wishlist capacity
        int currentItemCount = wishlistItemRepository.countItemsByWishlistId(wishlist.getId());
        if (currentItemCount >= maxWishlistItems) {
            throw new WishlistLimitExceededException("Wishlist cannot contain more than " + maxWishlistItems + " items");
        }

        // Check if book already in wishlist
        boolean exists = wishlistItemRepository.existsByWishlistIdAndBookId(wishlist.getId(), request.getBookId());
        if (exists) {
            throw new DuplicateWishlistItemException("Book already in wishlist");
        }

        // Fetch book details
        BookInfoDTO bookInfo = bookServiceClient.getBookInfo(request.getBookId());
        log.info("Fetched book info: {}", bookInfo); // add this

        // Create wishlist item
        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setBookId(bookInfo.getId());
        wishlistItem.setBookTitle(bookInfo.getTitle());
        wishlistItem.setBookAuthor(bookInfo.getAuthor());
        wishlistItem.setBookCoverImage(bookInfo.getCoverImageUrl());
        wishlistItem.setBookPrice(bookInfo.getPrice());
        wishlistItem.setWishlist(wishlist);

        wishlist.getItems().add(wishlistItem);
        wishlistItemRepository.save(wishlistItem);

        // Update wishlist totals
        updateWishlistTotals(wishlist);

        log.info("Successfully added book to wishlist. Wishlist ID: {}", wishlist.getId());
        return convertToDTO(wishlist);
    }

    @Override
    public WishlistDTO removeFromWishlist(Long userId, Long wishlistItemId) {
        log.info("Removing wishlist item {} for user {}", wishlistItemId, userId);

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found for user: " + userId));

        WishlistItem wishlistItem = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new WishlistItemNotFoundException("Wishlist item not found: " + wishlistItemId));

        // Verify item belongs to user's wishlist
        if (!wishlistItem.getWishlist().getId().equals(wishlist.getId())) {
            throw new WishlistServiceException("Wishlist item does not belong to user");
        }

        wishlist.getItems().remove(wishlistItem);
        wishlistItemRepository.delete(wishlistItem);

        // Update wishlist totals
        updateWishlistTotals(wishlist);

        log.info("Successfully removed item from wishlist");
        return convertToDTO(wishlist);
    }

    @Override
    public WishlistDTO removeBookFromWishlist(Long userId, Long bookId) {
        log.info("Removing book {} from wishlist for user {}", bookId, userId);

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found for user: " + userId));

        wishlistItemRepository.deleteByWishlistIdAndBookId(wishlist.getId(), bookId);

        // Refresh wishlist items
        wishlist.setItems(wishlistItemRepository.findByWishlistId(wishlist.getId()));
        updateWishlistTotals(wishlist);

        log.info("Successfully removed book from wishlist");
        return convertToDTO(wishlist);
    }

    @Override
    public WishlistDTO clearWishlist(Long userId) {
        log.info("Clearing wishlist for user {}", userId);

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found for user: " + userId));

        wishlistItemRepository.deleteAllByWishlistId(wishlist.getId());
        wishlist.getItems().clear();

        // Reset totals
        wishlist.setTotalItems(0);
        wishlistRepository.save(wishlist);

        log.info("Successfully cleared wishlist");
        return convertToDTO(wishlist);
    }

    @Override
    public void deleteWishlist(Long userId) {
        log.info("Deleting wishlist for user {}", userId);
        wishlistRepository.deleteByUserId(userId);
    }

    @Override
    public WishlistDTO moveToCart(MoveToCartRequest request) {
        log.info("Moving wishlist item {} to cart for user {}", request.getWishlistItemId(), request.getUserId());

        Wishlist wishlist = wishlistRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found for user: " + request.getUserId()));

        WishlistItem wishlistItem = wishlistItemRepository.findById(request.getWishlistItemId())
                .orElseThrow(() -> new WishlistItemNotFoundException("Wishlist item not found: " + request.getWishlistItemId()));

        // Verify item belongs to user's wishlist
        if (!wishlistItem.getWishlist().getId().equals(wishlist.getId())) {
            throw new WishlistServiceException("Wishlist item does not belong to user");
        }

        // Add to cart
        cartServiceClient.addToCart(request.getUserId(), wishlistItem.getBookId(), request.getQuantity());

        // Remove from wishlist
        wishlist.getItems().remove(wishlistItem);
        wishlistItemRepository.delete(wishlistItem);

        // Update wishlist totals
        updateWishlistTotals(wishlist);

        log.info("Successfully moved item from wishlist to cart");
        return convertToDTO(wishlist);
    }

    @Override
    public boolean isBookInWishlist(Long userId, Long bookId) {
        return wishlistRepository.findByUserId(userId)
                .map(wishlist -> wishlistItemRepository.existsByWishlistIdAndBookId(wishlist.getId(), bookId))
                .orElse(false);
    }

    @Override
    public Integer getWishlistItemCount(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .map(wishlist -> wishlistItemRepository.countItemsByWishlistId(wishlist.getId()))
                .orElse(0);
    }

    private Wishlist createNewWishlist(Long userId) {
        Wishlist newWishlist = new Wishlist();
        newWishlist.setUserId(userId);
        newWishlist.setTotalItems(0);
        return wishlistRepository.save(newWishlist);
    }

    private void updateWishlistTotals(Wishlist wishlist) {
        int totalItems = wishlistItemRepository.countItemsByWishlistId(wishlist.getId());
        wishlist.setTotalItems(totalItems);
        wishlistRepository.save(wishlist);
    }

    private WishlistDTO convertToDTO(Wishlist wishlist) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(wishlist.getId());
        dto.setUserId(wishlist.getUserId());
        dto.setTotalItems(wishlist.getTotalItems());
        dto.setCreatedAt(wishlist.getCreatedAt() != null ? wishlist.getCreatedAt().toString() : null);
        dto.setUpdatedAt(wishlist.getUpdatedAt() != null ? wishlist.getUpdatedAt().toString() : null);

        if (wishlist.getItems() != null) {
            dto.setItems(wishlist.getItems().stream()
                    .map(this::convertToItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private WishlistItemDTO convertToItemDTO(WishlistItem item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(item.getId());
        dto.setBookId(item.getBookId());
        dto.setBookTitle(item.getBookTitle());
        dto.setBookAuthor(item.getBookAuthor());
        dto.setBookCoverImage(item.getBookCoverImage());
        dto.setBookPrice(item.getBookPrice());
        dto.setAddedAt(item.getAddedAt() != null ? item.getAddedAt().toString() : null);
        return dto;
    }
}
