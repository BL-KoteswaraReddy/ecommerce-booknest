export interface CartApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface CartItem {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  bookCoverImage: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  addedAt: string;
}

export interface Cart {
  id: number;
  userId: number;
  totalPrice: number;
  totalItems: number;
  items: CartItem[];
  createdAt: string;
  updatedAt: string;
}

export interface AddToCartRequest {
  userId: number;
  bookId: number;
  quantity: number;
}

export interface UpdateCartItemRequest {
  userId: number;
  cartItemId: number;
  quantity: number;
}
