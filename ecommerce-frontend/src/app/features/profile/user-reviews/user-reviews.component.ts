import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { Review } from '../../../core/models/review.models';

@Component({
  selector: 'app-user-reviews',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-reviews.component.html',
  styleUrl: './user-reviews.component.css'
})
export class UserReviewsComponent implements OnInit {
  @Input() userId: number | null = null;
  reviews: Review[] = [];
  isLoading = false;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const userId = this.userId ?? this.authService.currentUserValue?.userId;
    if (userId && userId > 0) {
      this.loadUserReviews(userId);
    }
  }

  loadUserReviews(userId: number) {
    this.isLoading = true;
    this.reviewService.getReviewsByUserId(userId).subscribe({
      next: (res) => {
        if (res.success) {
          this.reviews = res.data.content;
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  deleteReview(reviewId: number) {
    const user = this.authService.currentUserValue;
    if (!user) return;

    if (confirm('Are you sure you want to delete this review?')) {
      this.reviewService.deleteReview(reviewId, user.userId).subscribe({
        next: (res) => {
          if (res.success) {
            this.reviews = this.reviews.filter(r => r.id !== reviewId);
          }
        }
      });
    }
  }
}
