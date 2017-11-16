import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AlertService } from './alert.service';
import { Observable } from 'rxjs/Observable';
import { Queue } from '../data/queue';
import { Server } from '../constants/server';

@Injectable()
export class CrawlService {
    constructor(private httpClient: HttpClient, private alertService: AlertService) {}

    getQueues(): Observable<Queue[]> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.get<Queue[]>(Server.apiBase + '/v1/crawler/queues', {headers: headers});
    }

    deleteQueue(queueId: number): Observable<Queue> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete<Queue>(Server.apiBase + '/v1/crawler/queue/' + queueId, {headers: headers});
    }

    getQueue(queueId: number): Observable<Queue> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.get<Queue>(Server.apiBase + '/v1/crawler/queue/' + queueId, {headers: headers});
    }

    crawlCollection(user: string): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.post<any>(Server.apiBase + '/v1/crawler/collection/' + user, null, {headers: headers});
    }
}