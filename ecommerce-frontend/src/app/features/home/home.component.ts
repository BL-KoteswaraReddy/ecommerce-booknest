import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookService } from '../../core/services/book.service';
import { Book } from '../../core/models/book.models';
import { RouterModule } from '@angular/router';

import { ImageUrlPipe } from '../../core/pipes/image-url.pipe';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, ImageUrlPipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  featuredBooks: Book[] = [];
  bestsellers: Book[] = [];
  newArrivals: Book[] = [];
  genres: string[] = [];
  isLoading = true;

  constructor(private bookService: BookService) {}

  ngOnInit() {
    this.loadData();
  }
  loadData() {
    this.bookService.getFeaturedBooks(0, 4).subscribe((res: any) => {
      if (res && (res.success || res.status === 200)) {
        this.featuredBooks = res.data.content || res.data;
      }
    });

    this.bookService.getBestsellers(0, 4).subscribe((res: any) => {
      if (res && (res.success || res.status === 200)) {
        this.bestsellers = res.data.content || res.data;
      }
    });

    this.bookService.getNewArrivals(0, 4).subscribe((res: any) => {
      if (res && (res.success || res.status === 200)) {
        this.newArrivals = res.data.content || res.data;
      }
      this.isLoading = false;
    });

    this.bookService.getAllGenres().subscribe((res: any) => {
      if (res && (res.success || res.status === 200)) {
        this.genres = res.data.slice(0, 6);
      }
    });
  }
}
