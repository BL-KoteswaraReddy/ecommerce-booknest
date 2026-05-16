package com.ecommerce.service.impl;
import com.ecommerce.client.BookServiceClient;
import com.ecommerce.dto.*;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.exception.CartItemNotFoundException;
import com.ecommerce.exception.CartNotFoundException;
import com.ecommerce.exception.CartOperationException;
import com.ecommerce.exception.MaxCartLimitExceededException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.CartService;
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
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookServiceClient bookServiceClient;

    @Value("${cart.max-items:50}")
    private int maxCartItems;

    @Override
    public CartDTO getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return convertToDTO(cart);
    }

    @Override
    public CartDTO addToCart(AddToCartRequest request) {
        log.info("Adding book {} to cart for user {}", request.getBookId(), request.getUserId());

        // Get book information from book service
        BookInfoDTO bookInfo = bookServiceClient.getBookInfo(request.getBookId());
        if (bookInfo == null) {
            throw new CartOperationException("Book not available");
        }

        // Check stock availability
        if (!bookServiceClient.checkStock(request.getBookId(), request.getQuantity())) {
            throw new CartOperationException("Insufficient stock available");
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseGet(() -> createNewCart(request.getUserId()));

        // Check cart capacity
        if (cart.getItems().size() >= maxCartItems) {
            throw new MaxCartLimitExceededException("Cart cannot contain more than " + maxCartItems + " items");
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndBookId(cart.getId(), request.getBookId())
                .orElse(null);

        if (existingItem != null) {
            // Update quantity if already exists
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            existingItem.setQuantity(newQuantity);
            existingItem.calculateTotalPrice();
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setBookId(bookInfo.getId());
            newItem.setBookTitle(bookInfo.getTitle());
            newItem.setBookAuthor(bookInfo.getAuthor());
            newItem.setBookCoverImage(bookInfo.getCoverImageUrl());
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(bookInfo.getPrice());
            newItem.setCart(cart);
            newItem.calculateTotalPrice();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        // Update cart totals
        updateCartTotals(cart);

        log.info("Successfully added book to cart. Cart ID: {}", cart.getId());
        return convertToDTO(cart);
    }

    @Override
    public CartDTO updateCartItem(UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", request.getCartItemId(), request.getUserId());

        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + request.getUserId()));

        CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + request.getCartItemId()));

        // Verify cart item belongs to the cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartOperationException("Cart item does not belong to the user's cart");
        }

        // Check stock availability for the new quantity
        if (request.getQuantity() > cartItem.getQuantity()) {
            int additionalQuantity = request.getQuantity() - cartItem.getQuantity();
            if (!bookServiceClient.checkStock(cartItem.getBookId(), additionalQuantity)) {
                throw new CartOperationException("Insufficient stock for additional quantity");
            }
        }

        // Update quantity
        cartItem.setQuantity(request.getQuantity());
        cartItem.calculateTotalPrice();
        cartItemRepository.save(cartItem);

        // Update cart totals
        updateCartTotals(cart);

        log.info("Successfully updated cart item");
        return convertToDTO(cart);
    }

    @Override
    public CartDTO removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing cart item {} for user {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + cartItemId));

        // Verify cart item belongs to the cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartOperationException("Cart item does not belong to the user's cart");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        // Update cart totals
        updateCartTotals(cart);

        log.info("Successfully removed cart item");
        return convertToDTO(cart);
    }

    @Override
    public CartDTO clearCart(Long userId) {
        log.info("Clearing cart for user {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));

        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.getItems().clear();

        // Reset totals
        cart.setTotalItems(0);
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        log.info("Successfully cleared cart");
        return convertToDTO(cart);
    }

    @Override
    public void deleteCart(Long userId) {
        log.info("Deleting cart for user {}", userId);
        cartRepository.deleteByUserId(userId);
    }

    @Override
    public Integer getCartItemCount(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cartItemRepository.countItemsByCartId(cart.getId()))
                .orElse(0);
    }

    @Override
    public Double getCartTotal(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cartItemRepository.sumTotalPriceByCartId(cart.getId()))
                .orElse(0.0);
    }

    private Cart createNewCart(Long userId) {
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setTotalPrice(0.0);
        newCart.setTotalItems(0);
        return cartRepository.save(newCart);
    }

    private void updateCartTotals(Cart cart) {
        int totalItems = cartItemRepository.countItemsByCartId(cart.getId());
        Double totalPrice = cartItemRepository.sumTotalPriceByCartId(cart.getId());

        cart.setTotalItems(totalItems);
        cart.setTotalPrice(totalPrice != null ? totalPrice : 0.0);
        cartRepository.save(cart);
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setTotalItems(cart.getTotalItems());
        dto.setCreatedAt(cart.getCreatedAt() != null ? cart.getCreatedAt().toString() : null);
        dto.setUpdatedAt(cart.getUpdatedAt() != null ? cart.getUpdatedAt().toString() : null);

        if (cart.getItems() != null) {
            dto.setItems(cart.getItems().stream()
                    .map(this::convertToItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private CartItemDTO convertToItemDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setBookId(item.getBookId());
        dto.setBookTitle(item.getBookTitle());
        dto.setBookAuthor(item.getBookAuthor());
        dto.setBookCoverImage(item.getBookCoverImage());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setAddedAt(item.getAddedAt() != null ? item.getAddedAt().toString() : null);
        return dto;
    }
}