import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReviewService } from '../../../../core/services/review.service';
import { AuthService } from '../../../../core/services/auth.service';
import { 
  Review, 
  RatingSummary, 
  ReviewRequest 
} from '../../../../core/models/review.models';
import { Users } from '../../../../core/models/auth.models';

@Component({
  selector: 'app-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './reviews.component.html',
  styleUrl: './reviews.component.css'
})
export class ReviewsComponent implements OnInit {
  @Input() bookId!: number;
  
  reviews: Review[] = [];
  summary: RatingSummary | null = null;
  currentUser: Users | null = null;
  hasUserReviewed: boolean = false;
  isLoading: boolean = false;
  
  reviewForm: FormGroup;
  isSubmitting: boolean = false;
  showForm: boolean = false;
  
  ratingHover: number = 0;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.reviewForm = this.fb.group({
      rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      title: ['', [Validators.maxLength(200)]],
      comment: ['', [Validators.required, Validators.maxLength(2000)]]
    });
  }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (this.currentUser && this.bookId) {
        this.checkIfUserReviewed();
      }
    });

    if (this.bookId) {
      this.loadReviews();
      this.loadSummary();
    }
  }

  loadReviews() {
    this.isLoading = true;
    this.reviewService.getReviewsByBookId(this.bookId).subscribe({
      next: (res) => {
        if (res.success) {
          this.reviews = res.data.content;
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  loadSummary() {
    this.reviewService.getRatingSummary(this.bookId).subscribe({
      next: (res) => {
        if (res.success) {
          this.summary = res.data;
        }
      }
    });
  }

  checkIfUserReviewed() {
    if (!this.currentUser) return;
    this.reviewService.hasUserReviewed(this.bookId, this.currentUser.userId).subscribe({
      next: (res) => {
        if (res.success) {
          this.hasUserReviewed = res.data;
        }
      }
    });
  }

  setRating(rating: number) {
    this.reviewForm.patchValue({ rating });
  }

  submitReview() {
    if (this.reviewForm.invalid || !this.currentUser) return;

    this.isSubmitting = true;
    const request: ReviewRequest = {
      bookId: this.bookId,
      userId: this.currentUser.userId,
      userName: this.currentUser.fullName,
      ...this.reviewForm.value
    };

    this.reviewService.addReview(request).subscribe({
      next: (res) => {
        if (res.success) {
          this.loadReviews();
          this.loadSummary();
          this.hasUserReviewed = true;
          this.showForm = false;
          this.reviewForm.reset({ rating: 5 });
        }
        this.isSubmitting = false;
      },
      error: () => this.isSubmitting = false
    });
  }

  markHelpful(reviewId: number) {
    if (!this.currentUser) {
      // Redirect to login or show message
      return;
    }

    this.reviewService.markReviewHelpful(reviewId, this.currentUser.userId).subscribe({
      next: (res) => {
        if (res.success) {
          const review = this.reviews.find(r => r.id === reviewId);
          if (review) review.helpfulCount++;
        }
      }
    });
  }

  deleteReview(reviewId: number) {
    if (!this.currentUser) return;

    if (confirm('Are you sure you want to delete your review?')) {
      this.reviewService.deleteReview(reviewId, this.currentUser.userId).subscribe({
        next: (res) => {
          if (res.success) {
            this.loadReviews();
            this.loadSummary();
            this.hasUserReviewed = false;
          }
        }
      });
    }
  }
}
