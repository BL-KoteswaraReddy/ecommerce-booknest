export interface Address {
  id?: number;
  fullName: string;
  mobile: string;
  alternateMobile?: string;
  address: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  addressType?: string;
  isDefault?: boolean;
}

export interface OrderItem {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  bookIsbn: string;
  bookCoverImage: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  userId: number;
  orderDate: string;
  totalAmount: number;
  shippingCharge: number;
  discountAmount: number;
  finalAmount: number;
  status: string; // PENDING, CONFIRMED, PROCESSING, DISPATCHED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED
  paymentMode: string; // CASH_ON_DELIVERY, WALLET, etc.
  paymentStatus: string;
  paymentId: string;
  orderNotes?: string;
  cancellationReason?: string;
  items: OrderItem[];
  shippingAddress: Address;
  createdAt: string;
}

export interface PlaceOrderRequest {
  userId: number;
  paymentMode: string;
  shippingAddress: Address;
  orderNotes?: string;
}

export interface OrderStatusUpdateRequest {
  orderId: number;
  status: string;
  cancellationReason?: string;
}

export interface OrderResponse {
  content: Order[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface OrderApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
