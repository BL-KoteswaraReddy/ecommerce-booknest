import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { 
  Review, 
  ReviewRequest, 
  UpdateReviewRequest, 
  ReviewResponse, 
  RatingSummary,
  ReviewApiResponse
} from '../models/review.models';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiUrl = `${environment.apiUrl}/api/reviews`;

  constructor(private http: HttpClient) {}

  // Create review
  addReview(request: ReviewRequest): Observable<ReviewApiResponse<Review>> {
    return this.http.post<ReviewApiResponse<Review>>(this.apiUrl, request);
  }

  // Update review
  updateReview(reviewId: number, request: UpdateReviewRequest): Observable<ReviewApiResponse<Review>> {
    return this.http.put<ReviewApiResponse<Review>>(`${this.apiUrl}/${reviewId}`, request);
  }

  // Delete review
  deleteReview(reviewId: number, userId: number): Observable<ReviewApiResponse<void>> {
    return this.http.delete<ReviewApiResponse<void>>(`${this.apiUrl}/${reviewId}?userId=${userId}`);
  }

  // Get review by ID
  getReviewById(reviewId: number): Observable<ReviewApiResponse<Review>> {
    return this.http.get<ReviewApiResponse<Review>>(`${this.apiUrl}/${reviewId}`);
  }

  // Get reviews for a book
  getReviewsByBookId(bookId: number, page: number = 0, size: number = 10): Observable<ReviewApiResponse<ReviewResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ReviewApiResponse<ReviewResponse>>(`${this.apiUrl}/book/${bookId}`, { params });
  }

  // Get reviews by user
  getReviewsByUserId(userId: number, page: number = 0, size: number = 10): Observable<ReviewApiResponse<ReviewResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ReviewApiResponse<ReviewResponse>>(`${this.apiUrl}/user/${userId}`, { params });
  }

  // Get rating summary for a book
  getRatingSummary(bookId: number): Observable<ReviewApiResponse<RatingSummary>> {
    return this.http.get<ReviewApiResponse<RatingSummary>>(`${this.apiUrl}/rating/${bookId}`);
  }

  // Mark review as helpful
  markReviewHelpful(reviewId: number, userId: number): Observable<ReviewApiResponse<void>> {
    return this.http.post<ReviewApiResponse<void>>(`${this.apiUrl}/${reviewId}/helpful?userId=${userId}`, {});
  }

  // Check if user has reviewed
  hasUserReviewed(bookId: number, userId: number): Observable<ReviewApiResponse<boolean>> {
    const params = new HttpParams()
      .set('bookId', bookId.toString())
      .set('userId', userId.toString());
    return this.http.get<ReviewApiResponse<boolean>>(`${this.apiUrl}/check`, { params });
  }

  // Admin: Get pending reviews
  getPendingReviews(page: number = 0, size: number = 10): Observable<ReviewApiResponse<ReviewResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ReviewApiResponse<ReviewResponse>>(`${this.apiUrl}/pending`, { params });
  }

  // Admin: Moderate review
  moderateReview(reviewId: number, approve: boolean): Observable<ReviewApiResponse<void>> {
    return this.http.put<ReviewApiResponse<void>>(`${this.apiUrl}/${reviewId}/moderate?approve=${approve}`, {});
  }
}
