export interface BookApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  genre: string;
  publisher: string;
  price: number;
  stock: number;
  rating: number;
  description: string;
  coverImageUrl: string;
  publishedDate: string;
  isFeatured: boolean;
  isBestseller: boolean;
  reviewCount: number;
}

export interface BookRequest {
  title: string;
  author: string;
  isbn: string;
  genre: string;
  publisher?: string;
  price: number;
  stock: number;
  description?: string;
  coverImageUrl?: string;
  publishedDate?: string;
  isFeatured?: boolean;
  isBestseller?: boolean;
}

export interface BookResponse {
  content: Book[];
  pageNo: int;
  pageSize: int;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Angular TS types
type int = number;
