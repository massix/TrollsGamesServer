import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Quote } from '../data/quote';
import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';

@Injectable()
export class QuotesService {
    constructor(private httpClient: HttpClient) {}

    getAllQuotes(): Observable<Quote[]> {
        return this.httpClient.get<Quote[]>(environment.apiBase + '/v1/quotes/get');
    }

    removeQuote(quote: Quote): Observable<Quote> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        const params = new HttpParams().set('quote', quote.quote);
        return this.httpClient.delete<Quote>(environment.apiBase + '/v1/quotes/remove',
            {
                headers: headers,
                params: params
            }
        );
    }

    addQuote(quote: Quote): Observable<Quote> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<Quote>(environment.apiBase + '/v1/quotes/add', quote, {headers: headers});
    }
}
