import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AlertService } from './alert.service';
import { Observable } from 'rxjs/Observable';
import { environment } from '../../environments/environment';
import { CrawlerStatus } from '../data/crawlerstatus';
import { HttpHeaderResponse } from '@angular/common/http/src/response';

@Injectable()
export class CrawlService {
    constructor(private httpClient: HttpClient, private alertService: AlertService) {}

    getStatus(): Observable<CrawlerStatus> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.get<CrawlerStatus>(environment.apiBase + '/v1/crawler/status', {headers: headers});
    }

    crawlCollection(user: string): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.post<any>(environment.apiBase + '/v1/crawler/collection/' + user, null, {headers: headers});
    }

    wake(): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<any>(environment.apiBase + '/v1/crawler/wake', null, {headers: headers});
    }

    stop(): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<any>(environment.apiBase + '/v1/crawler/stop', null, {headers: headers});
    }
}