export interface ReviewApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

export interface Review {
  id: number;
  bookId: number;
  userId: number;
  userName: string;
  rating: number;
  title: string;
  comment: string;
  isVerifiedPurchase: boolean;
  helpfulCount: number;
  reviewDate: string;
  updatedAt: string;
  isApproved: boolean;
}

export interface ReviewRequest {
  bookId: number;
  userId: number;
  userName: string;
  rating: number;
  title?: string;
  comment?: string;
}

export interface UpdateReviewRequest {
  rating?: number;
  title?: string;
  comment?: string;
}

export interface RatingSummary {
  bookId: number;
  averageRating: number;
  totalReviews: number;
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
}

export interface ReviewResponse {
  content: Review[];
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
