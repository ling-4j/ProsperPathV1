import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AiService {
  private apiUrl = '/api/ai/ask';

  constructor(private http: HttpClient) {}

  ask(prompt: string): Observable<string> {
    return this.http.post(this.apiUrl, { prompt }, { responseType: 'text' });
  }
}
