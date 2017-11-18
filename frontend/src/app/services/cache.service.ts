import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Cache } from '../data/cache';
import { environment } from '../../environments/environment';

@Injectable()
export class CacheService {
    constructor(private httpClient: HttpClient) {}

    getMemoryCache(): Observable<Cache> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.get<Cache>(environment.apiBase + '/v1/cache/get', {headers: headers});
    }

    purgeExpired(): Observable<Cache> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete<Cache>(environment.apiBase + '/v1/cache/expired', {headers: headers});
    }

    purgeAll(): Observable<Cache> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete<Cache>(environment.apiBase + '/v1/cache/purge', {headers: headers});
    }
}