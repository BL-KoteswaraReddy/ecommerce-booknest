import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookService } from '../../../../core/services/book.service';
import { Book } from '../../../../core/models/book.models';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-book-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.css'
})
export class BookListComponent implements OnInit {
  books: Book[] = [];
  isLoading = true;

  // Pagination simplified for admin view
  currentPage = 0;
  totalPages = 0;
  pageSize = 20;

  constructor(private bookService: BookService) {}

  ngOnInit() {
    this.loadBooks();
  }

  loadBooks() {
    this.isLoading = true;
    this.bookService.getAllBooks(this.currentPage, this.pageSize, 'id', 'desc').subscribe(res => {
      if (res.success && res.data) {
        this.books = res.data.content;
        this.totalPages = res.data.totalPages;
      }
      this.isLoading = false;
    });
  }

  deleteBook(id: number) {
    if (confirm('Are you sure you want to delete this book? This action cannot be undone.')) {
      this.bookService.deleteBook(id).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Book deleted successfully');
            this.loadBooks();
          }
        },
        error: () => alert('Failed to delete book.')
      });
    }
  }

  changePage(dir: number) {
    if (this.currentPage + dir >= 0 && this.currentPage + dir < this.totalPages) {
      this.currentPage += dir;
      this.loadBooks();
    }
  }
}
