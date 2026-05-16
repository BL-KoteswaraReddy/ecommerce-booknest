import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookService } from '../../../../core/services/book.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './book-form.component.html',
  styleUrl: './book-form.component.css'
})
export class BookFormComponent implements OnInit {
  bookForm: FormGroup;
  isEditMode = false;
  bookId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private bookService: BookService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.bookForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      author: ['', [Validators.required, Validators.maxLength(100)]],
      isbn: ['', [Validators.pattern('^(97(8|9))?\\d{9}(\\d|X)$')]],
      genre: ['', Validators.required],
      publisher: [''],
      price: [0, [Validators.required, Validators.min(0.01)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      description: [''],
      coverImageUrl: [''],
      publishedDate: [''],
      isFeatured: [false],
      isBestseller: [false]
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.bookId = +params['id'];
        this.loadBook(this.bookId);
      }
    });
  }

  loadBook(id: number) {
    this.isLoading = true;
    this.bookService.getBookById(id).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          // Format date if needed, slicing out time for date input
          let pDate = res.data.publishedDate;
          if (pDate && pDate.includes('T')) pDate = pDate.split('T')[0];
          
          this.bookForm.patchValue({
            ...res.data,
            publishedDate: pDate
          });
        }
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load book data.';
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.bookForm.invalid) {
      this.bookForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    const reqData = this.bookForm.value;
    
    // Formatting date to LocalDateTime format expected by backend if provided
    if (reqData.publishedDate && !reqData.publishedDate.includes('T')) {
        reqData.publishedDate = `${reqData.publishedDate}T00:00:00`;
    }

    const request = this.isEditMode && this.bookId
      ? this.bookService.updateBook(this.bookId, reqData)
      : this.bookService.createBook(reqData);

    request.subscribe({
      next: (res) => {
        if (res.success) {
          this.router.navigate(['/admin/books']);
        } else {
          this.errorMessage = res.message || 'Operation failed.';
          this.isSaving = false;
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while saving the book.';
        this.isSaving = false;
      }
    });
  }
}
