import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookService } from '../../core/services/book.service';
import { Book } from '../../core/models/book.models';

import { ImageUrlPipe } from '../../core/pipes/image-url.pipe';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, RouterModule, ImageUrlPipe],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.css'
})
export class CatalogComponent implements OnInit {
  books: Book[] = [];
  genres: string[] = [];
  
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 12;

  currentKeyword: string = '';
  currentGenre: string = '';
  currentFilter: string = ''; // 'featured', 'bestsellers', etc.
  isLoading = true;

  constructor(
    private bookService: BookService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadGenres();

    // Listen to query param changes to re-fetch books
    this.route.queryParams.subscribe(params => {
      this.currentKeyword = params['search'] || '';
      this.currentGenre = params['genre'] || '';
      this.currentFilter = params['filter'] || '';
      this.currentPage = params['page'] ? +params['page'] : 0;
      
      this.loadBooks();
    });
  }

  loadGenres() {
    this.bookService.getAllGenres().subscribe(res => {
      if (res.success) this.genres = res.data;
    });
  }

  loadBooks() {
    this.isLoading = true;

    let request;
    if (this.currentKeyword) {
      request = this.bookService.searchBooks(this.currentKeyword, this.currentPage, this.pageSize);
    } else if (this.currentGenre) {
      request = this.bookService.getBooksByGenre(this.currentGenre, this.currentPage, this.pageSize);
    } else if (this.currentFilter === 'featured') {
      request = this.bookService.getFeaturedBooks(this.currentPage, this.pageSize);
    } else if (this.currentFilter === 'bestsellers') {
      request = this.bookService.getBestsellers(this.currentPage, this.pageSize);
    } else {
      request = this.bookService.getAllBooks(this.currentPage, this.pageSize);
    }

    request.subscribe({
      next: (res: any) => {
        if (res && (res.success || res.status === 200 || res.status === 'OK')) {
          const data = res.data;
          // Handle both paginated response { content: [] } and direct array []
          if (data && data.content) {
            this.books = data.content;
            this.totalPages = data.totalPages;
            this.totalElements = data.totalElements;
          } else if (Array.isArray(data)) {
            this.books = data;
            this.totalPages = 1;
            this.totalElements = data.length;
          } else {
            this.books = [];
          }
        } else {
          this.books = [];
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading books:', err);
        this.books = [];
        this.isLoading = false;
      }
    });
  }

  filterByGenre(genre: string) {
    this.router.navigate(['/catalog'], { queryParams: { genre: genre } });
  }

  clearFilters() {
    this.router.navigate(['/catalog']);
  }

  changePage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { page: page },
        queryParamsHandling: 'merge'
      });
    }
  }
}
