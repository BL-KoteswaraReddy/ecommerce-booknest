import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Order, OrderApiResponse, OrderResponse, OrderStatusUpdateRequest, PlaceOrderRequest } from '../models/order.models';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${environment.apiUrl}/api/orders`;

  constructor(private http: HttpClient) {}

  placeOrder(request: PlaceOrderRequest): Observable<OrderApiResponse<Order>> {
    return this.http.post<OrderApiResponse<Order>>(`${this.apiUrl}/place`, request);
  }

  getOrderById(orderId: number): Observable<OrderApiResponse<Order>> {
    return this.http.get<OrderApiResponse<Order>>(`${this.apiUrl}/${orderId}`);
  }

  getOrderByNumber(orderNumber: string): Observable<OrderApiResponse<Order>> {
    return this.http.get<OrderApiResponse<Order>>(`${this.apiUrl}/number/${orderNumber}`);
  }

  getUserOrders(userId: number, page: number = 0, size: number = 10, sortBy: string = 'orderDate', sortDir: string = 'desc'): Observable<OrderApiResponse<OrderResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<OrderApiResponse<OrderResponse>>(`${this.apiUrl}/user/${userId}`, { params });
  }

  getAllOrders(page: number = 0, size: number = 10): Observable<OrderApiResponse<OrderResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<OrderApiResponse<OrderResponse>>(`${this.apiUrl}/all`, { params });
  }

  getOrdersByStatus(status: string, page: number = 0, size: number = 10): Observable<OrderApiResponse<OrderResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<OrderApiResponse<OrderResponse>>(`${this.apiUrl}/status/${status}`, { params });
  }

  updateOrderStatus(request: OrderStatusUpdateRequest): Observable<OrderApiResponse<Order>> {
    return this.http.put<OrderApiResponse<Order>>(`${this.apiUrl}/status`, request);
  }

  cancelOrder(orderId: number, reason: string = 'Customer requested cancellation'): Observable<OrderApiResponse<void>> {
    let params = new HttpParams().set('reason', reason);
    return this.http.put<OrderApiResponse<void>>(`${this.apiUrl}/${orderId}/cancel`, null, { params });
  }

  getUserOrderTotal(userId: number): Observable<OrderApiResponse<number>> {
    return this.http.get<OrderApiResponse<number>>(`${this.apiUrl}/user/${userId}/total`);
  }

  getTotalSales(): Observable<OrderApiResponse<number>> {
    return this.http.get<OrderApiResponse<number>>(`${this.apiUrl}/sales/total`);
  }
}
