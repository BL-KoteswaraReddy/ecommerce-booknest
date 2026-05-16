import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Book, BookApiResponse, BookRequest, BookResponse } from '../models/book.models';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private apiUrl = `${environment.apiUrl}/api/books`;

  constructor(private http: HttpClient) {}

  // ======================
  // Customer / Catalog APIs
  // ======================

  getAllBooks(page: number = 0, size: number = 12, sortBy: string = 'id', sortDir: string = 'asc'): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<BookApiResponse<BookResponse>>(this.apiUrl, { params });
  }

  getBookById(id: number): Observable<BookApiResponse<Book>> {
    return this.http.get<BookApiResponse<Book>>(`${this.apiUrl}/${id}`);
  }

  searchBooks(keyword: string, page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page)
      .set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/search`, { params });
  }

  getBooksByGenre(genre: string, page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/genre/${genre}`, { params });
  }

  getBooksByAuthor(author: string, page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/author/${author}`, { params });
  }

  getFeaturedBooks(page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/featured`, { params });
  }

  getBestsellers(page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/bestsellers`, { params });
  }

  getNewArrivals(page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/new-arrivals`, { params });
  }

  getBooksByPriceRange(minPrice: number, maxPrice: number, page: number = 0, size: number = 12): Observable<BookApiResponse<BookResponse>> {
    let params = new HttpParams()
      .set('minPrice', minPrice)
      .set('maxPrice', maxPrice)
      .set('page', page)
      .set('size', size);
    return this.http.get<BookApiResponse<BookResponse>>(`${this.apiUrl}/price-range`, { params });
  }

  getAllGenres(): Observable<BookApiResponse<string[]>> {
    return this.http.get<BookApiResponse<string[]>>(`${this.apiUrl}/genres`);
  }

  checkStock(id: number, quantity: number): Observable<BookApiResponse<{ available: boolean }>> {
    let params = new HttpParams().set('quantity', quantity);
    return this.http.get<BookApiResponse<{ available: boolean }>>(`${this.apiUrl}/${id}/check-stock`, { params });
  }

  // ======================
  // Admin APIs
  // ======================

  createBook(request: BookRequest): Observable<BookApiResponse<Book>> {
    return this.http.post<BookApiResponse<Book>>(this.apiUrl, request);
  }

  updateBook(id: number, request: BookRequest): Observable<BookApiResponse<Book>> {
    return this.http.put<BookApiResponse<Book>>(`${this.apiUrl}/${id}`, request);
  }

  deleteBook(id: number): Observable<BookApiResponse<void>> {
    return this.http.delete<BookApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  updateStock(id: number, quantity: number): Observable<BookApiResponse<void>> {
    return this.http.patch<BookApiResponse<void>>(`${this.apiUrl}/${id}/stock`, { quantity });
  }
}
