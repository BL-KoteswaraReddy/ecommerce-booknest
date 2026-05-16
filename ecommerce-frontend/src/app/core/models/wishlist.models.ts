export interface WishlistApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface WishlistItem {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  bookCoverImage: string;
  bookPrice: number;
  addedAt: string;
}

export interface Wishlist {
  id: number;
  userId: number;
  totalItems: number;
  items: WishlistItem[];
  createdAt: string;
  updatedAt: string;
}

export interface AddToWishlistRequest {
  userId: number;
  bookId: number;
}

export interface MoveToCartRequest {
  userId: number;
  wishlistItemId: number;
  quantity: number;
}
