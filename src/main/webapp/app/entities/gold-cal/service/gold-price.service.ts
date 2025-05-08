import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class GoldPriceService {
  private apiUrl = '/api/gold-price';

  constructor(private http: HttpClient) {}

  getGoldPrice(zone: string): Observable<any> {
    return this.http.get(`${this.apiUrl}?zone=${zone}`).pipe(
      catchError(error => {
        console.error('Error fetching gold prices:', error);
        return of(null);
      }),
    );
  }
}
