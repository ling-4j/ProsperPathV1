import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GoldPrice, GoldPriceResponse } from '../../gold-cal/gold-price.model';

@Injectable({
  providedIn: 'root',
})
export class GoldPriceService {
  private apiUrl = 'https://edge-api.pnj.io/ecom-frontend/v1/get-gold-price?zone=11';

  constructor(private http: HttpClient) {}

  getGoldPrices(): Observable<GoldPrice[]> {
    return this.http.get<GoldPriceResponse>(this.apiUrl).pipe(map(response => response.data));
  }
}
