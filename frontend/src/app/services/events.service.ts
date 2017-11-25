import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Event } from '../data/event';
import { environment } from '../../environments/environment';

@Injectable()
export class EventsService {
    constructor(private httpClient: HttpClient) {}

    getAllEvents(): Observable<Event[]> {
        return this.httpClient.get<Event[]>(environment.apiBase + '/v1/events/get');
    }

    storeEvent(event: Event): Observable<Event> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.put<Event>(environment.apiBase + '/v1/events/create', event, {headers: headers});
    }

    removeEvent(event: number): Observable<Event> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.delete<Event>(environment.apiBase + '/v1/events/remove/' + event, {headers: headers});
    }
}